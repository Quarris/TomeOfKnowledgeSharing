package dev.quarris.knowledgetome.items;

import com.mojang.authlib.GameProfile;
import com.mojang.realmsclient.gui.ChatFormatting;
import cpw.mods.fml.common.FMLCommonHandler;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.item.IPedestalItem;
import moze_intel.projecte.api.proxy.ITransmutationProxy;
import moze_intel.projecte.gameObjs.tiles.DMPedestalTile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class SharingTomeItem extends Item implements IPedestalItem {

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
        UUID knowledgePlayerId = getBoundPlayerId(pStack);
        if (knowledgePlayerId == null) {
            // Shouldn't happen, but good to be same.
            return pStack;
        }

        // You cannot learn your own knowledge
        if (knowledgePlayerId.equals(pPlayer.getUniqueID())) {
            pPlayer.addChatComponentMessage(new ChatComponentText("You read through your notes; but you won't learn anything from them.").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_AQUA)));
            return pStack;
        }

        List<ItemStack> knownItems = ProjectEAPI.getTransmutationProxy().getKnowledge(knowledgePlayerId);
        boolean learned = false;
        for (ItemStack item : knownItems) {
            if (tryAddKnowledge(pPlayer.getUniqueID(), item)) {
                learned = true;
            }
        }
        String text = "You read through the notes; ";
        if (learned) {
            text += "and you learn many things from them.";
        } else {
            text += "but you have already learned all you can from them.";
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

    public static UUID getBoundPlayerId(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || !tag.hasKey("SharingKnowledge")) {
            return null;
        }

        NBTTagCompound knowledgeTag = tag.getCompoundTag("SharingKnowledge");
        UUID knowledgePlayerId = new UUID(knowledgeTag.getLong("UUIDMost"), knowledgeTag.getLong("UUIDLeast"));
        // Try update the name if the player is online
        GameProfile profile = FMLCommonHandler.instance().getMinecraftServerInstance().func_152358_ax().func_152652_a(knowledgePlayerId);
        knowledgeTag.setString("Name", profile.getName());

        return knowledgePlayerId;
    }

    private static boolean tryAddKnowledge(UUID uuid, ItemStack item) {
        ITransmutationProxy transmutation = ProjectEAPI.getTransmutationProxy();
        if (!transmutation.hasKnowledgeFor(uuid, item)) {
            transmutation.addKnowledge(uuid, item);
            return true;
        }

        return false;
    }

    @Override
    public void updateInPedestal(World world, int x, int y, int z) {
        if (world.isRemote) {
            return;
        }

        DMPedestalTile tile = (DMPedestalTile) world.getTileEntity(x, y, z);
        ItemStack tome = tile.getItemStack();
        UUID boundId = getBoundPlayerId(tome);
        if (boundId == null || tile.getActivityCooldown() > 0) {
            tile.decrementActivityCooldown();
            return;
        }

        List<EntityPlayerMP> players = world.getEntitiesWithinAABB(EntityPlayerMP.class, tile.getEffectBounds().expand(64, 64, 64));
        Iterator<EntityPlayerMP> ite = players.iterator();

        while (ite.hasNext()) {
            EntityPlayerMP player = ite.next();
            boolean learned = false;
            for (ItemStack item : ProjectEAPI.getTransmutationProxy().getKnowledge(boundId)) {
                if (tryAddKnowledge(player.getUniqueID(), item)) {
                    learned = true;
                }
            }

            if (learned) {
                player.addChatComponentMessage(new ChatComponentText("You have learned something new."));
            }
        }

        tile.setActivityCooldown(60*20);
    }

    @Override
    public List<String> getPedestalDescription() {
        List<String> desc = new ArrayList<>();
        desc.add(ChatFormatting.LIGHT_PURPLE + "Teaches new knowledge of the tome to players within large cube radius.");
        return desc;
    }
}
