package dev.quarris.knowledgetome.items;

import com.mojang.authlib.GameProfile;
import com.mojang.realmsclient.gui.ChatFormatting;
import cpw.mods.fml.common.FMLCommonHandler;
import moze_intel.projecte.api.ProjectEAPI;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import java.util.List;
import java.util.UUID;

public class SharingTomeItem extends Item {

    public SharingTomeItem() {
    }

    @Override
    public ItemStack onItemRightClick(ItemStack pStack, World pLevel, EntityPlayer pPlayer) {
        if (pLevel.isRemote) return pStack;

        NBTTagCompound tag = pStack.getTagCompound();
        if (tag == null || !tag.hasKey("SharingKnowledge")) {
            // Is empty, set as out knowledge
            NBTTagCompound knowledgeTag = new NBTTagCompound();
            knowledgeTag.setLong("UUIDMost", pPlayer.getUniqueID().getMostSignificantBits());
            knowledgeTag.setLong("UUIDLeast", pPlayer.getUniqueID().getLeastSignificantBits());
            knowledgeTag.setString("Name", pPlayer.getGameProfile().getName());
            pStack.setTagInfo("SharingKnowledge", knowledgeTag);
            pPlayer.addChatComponentMessage(new ChatComponentText("Your eternal knowledge has been bound to this book.").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.LIGHT_PURPLE)));
            return pStack;
        }

        // Contains knowledge, add it
        NBTTagCompound knowledgeTag = tag.getCompoundTag("SharingKnowledge");
        UUID knowledgePlayerId = new UUID(knowledgeTag.getLong("UUIDMost"), knowledgeTag.getLong("UUIDLeast"));
        GameProfile profile = FMLCommonHandler.instance().getMinecraftServerInstance().func_152358_ax().func_152652_a(knowledgePlayerId);
        knowledgeTag.setString("Name", profile.getName());

        // You cannot learn your own knowledge
        if (knowledgePlayerId.equals(pPlayer.getUniqueID())) {
            pPlayer.addChatComponentMessage(new ChatComponentText("You read through your notes; but you won't learn anything from them.").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_AQUA)));
            return pStack;
        }

        List<ItemStack> knownItems = ProjectEAPI.getTransmutationProxy().getKnowledge(knowledgePlayerId);
        boolean learned = false;
        for (ItemStack item : knownItems) {
            ProjectEAPI.getTransmutationProxy().addKnowledge(pPlayer.getUniqueID(), item);
            learned = true;
        }
        String text = "You read through the notes; ";
        if (learned) {
            text += "but you have already learned all you can from them.";
        } else {
            text += "and you learn many things from them.";
        }
        pPlayer.addChatComponentMessage(new ChatComponentText(text).setChatStyle(new ChatStyle().setColor(learned ? EnumChatFormatting.DARK_AQUA : EnumChatFormatting.GREEN)));
        return pStack;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addInformation(ItemStack pStack, EntityPlayer pPlayer, List pTooltips, boolean pAdvanced) {
        super.addInformation(pStack, pPlayer, pTooltips, pAdvanced);
        String tooltip = ChatFormatting.GRAY + "Unbound";
        NBTTagCompound tag = pStack.getTagCompound();
        if (tag != null && tag.hasKey("SharingKnowledge")) {
            NBTTagCompound knowledgeTag = tag.getCompoundTag("SharingKnowledge");
            tooltip = ChatFormatting.GOLD + "Bound to " + knowledgeTag.getString("Name");
        }
        pTooltips.add(tooltip);
    }
}
