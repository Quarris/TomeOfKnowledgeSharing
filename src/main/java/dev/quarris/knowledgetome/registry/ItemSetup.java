package dev.quarris.knowledgetome.registry;

import cpw.mods.fml.common.registry.GameRegistry;
import dev.quarris.knowledgetome.items.SharingTomeItem;
import moze_intel.projecte.gameObjs.ObjHandler;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;

public class ItemSetup {

    public static Item SHARING_TOME = new SharingTomeItem()
        .setUnlocalizedName("knowledgetome.tome_of_knowledge_sharing")
        .setTextureName("knowledgetome:tome_of_knowledge_sharing")
        .setCreativeTab(CreativeTabs.tabMisc)
        .setMaxStackSize(1);

    public static void init() {
        GameRegistry.registerItem(SHARING_TOME, "tome_of_knowledge_sharing");

        GameRegistry.addRecipe(new ShapedRecipes(3, 3, new ItemStack[]{
            null, new ItemStack(ObjHandler.philosStone), null,
            new ItemStack(Items.redstone), new ItemStack(Items.writable_book), new ItemStack(Items.redstone),
            new ItemStack(ObjHandler.covalence, 1, 2), new ItemStack(ObjHandler.covalence, 1, 2), new ItemStack(ObjHandler.covalence, 1, 2)
        }, new ItemStack(SHARING_TOME)));
    }

}
