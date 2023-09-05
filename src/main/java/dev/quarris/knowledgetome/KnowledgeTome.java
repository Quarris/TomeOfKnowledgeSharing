package dev.quarris.knowledgetome;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import dev.quarris.knowledgetome.registry.ItemSetup;

@Mod(modid = ModRef.ID, version = ModRef.VERSION)
public class KnowledgeTome {

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ItemSetup.init();
    }

}
