package net.retronixmc.mobcoins;

import net.retronixmc.mobcoins.chance.ChanceManager;
import net.retronixmc.mobcoins.objects.Profile;
import net.retronixmc.mobcoins.utils.DataHandler;

import java.util.List;

public class RetronixMobcoinsAPI {
    private static Main instance;

    public RetronixMobcoinsAPI(Main main)
    {
        instance = main;
    }

    public static List<Profile> getProfiles()
    {
        return Main.getInstance().getDataHandler().getProfiles();
    }

    public static DataHandler getDataHandler()
    {
        return Main.getInstance().getDataHandler();
    }

    public static ChanceManager getChanceManager() { return Main.getInstance().getChanceManager(); }
}