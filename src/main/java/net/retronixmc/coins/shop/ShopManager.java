package net.retronixmc.coins.shop;

import net.retronixmc.coins.Main;
import net.retronixmc.coins.RetronixCoinsAPI;
import net.retronixmc.coins.config.ConfigData;
import net.retronixmc.coins.gui.GUIItem;
import net.retronixmc.coins.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShopManager {
    private Main instance;
    private File categoryFile = new File("plugins/RetronixCoins/", "categories.yml");
    private FileConfiguration categoryData = YamlConfiguration.loadConfiguration(categoryFile);
    private File itemsFile = new File("plugins/RetronixCoins/", "shopitems.yml");
    private FileConfiguration itemsData = YamlConfiguration.loadConfiguration(itemsFile);
    private ArrayList<Category> categories;

    public ShopManager(Main instance) {
        this.instance = instance;
        categories = new ArrayList<>();
        loadCategories();
        loadItems();
    }

    public void saveDefaultCategoryData() {

        categoryData.set("categories", new ArrayList<String>());
        try {
            categoryData.save(categoryFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (Category category : MiscUtils.getDefaultCategories())
        {
            categoryData.set("categories." + category.getName() + ".material", category.getIcon().getType().name());
            categoryData.set("categories." + category.getName() + ".quantity", category.getIcon().getAmount());
            categoryData.set("categories." + category.getName() + ".damage", category.getIcon().getDurability());
            categoryData.set("categories." + category.getName() + ".name", category.getIcon().getItemMeta().getDisplayName());
            categoryData.set("categories." + category.getName() + ".lore", category.getIcon().getItemMeta().getLore());
            categoryData.set("categories." + category.getName() + ".slot", category.getSlot());
        }

        try {
            categoryData.save(categoryFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveDefaultItemData() {

        itemsData.set("shops", new ArrayList<String>());
        try {
            itemsData.save(itemsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (Category category : MiscUtils.getDefaultCategories())
        {
            itemsData.set("shops." + category.getName() + ".dirt.material", category.getIcon().getType().name());
            itemsData.set("shops." + category.getName() + ".dirt.quantity", category.getIcon().getAmount());
            itemsData.set("shops." + category.getName() + ".dirt.damage", category.getIcon().getDurability());
            itemsData.set("shops." + category.getName() + ".dirt.name", category.getIcon().getItemMeta().getDisplayName());
            itemsData.set("shops." + category.getName() + ".dirt.lore", category.getIcon().getItemMeta().getLore());
            itemsData.set("shops." + category.getName() + ".dirt.slot", category.getSlot());
            itemsData.set("shops." + category.getName() + ".dirt.price", 1);
            itemsData.set("shops." + category.getName() + ".dirt.type", "COMMAND");
            itemsData.set("shops." + category.getName() + ".dirt.commands", Arrays.asList(new String[] {"/say hi", "/kick %player%"}));
        }

        try {
            itemsData.save(itemsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadCategories() {
        if (categoryData.getConfigurationSection("categories") == null) {
            saveDefaultCategoryData();
            reloadCategories();
            return;
        }
        for (String s : categoryData.getConfigurationSection("categories").getKeys(false)) {
            Material material = Material.DIRT;
            short data = 0;
            int amount = 1;
            int slot = 0;
            int invSize = 54;
            String name = "";
            List<String> lore = new ArrayList<>();
            // load itemstack based on data
            material = Material.getMaterial(categoryData.getString("categories." + s + ".material"));
            amount = categoryData.getInt("categories." + s + ".quantity");
            if (categoryData.getString("categories." + s + ".damage") != null)
                data = Short.parseShort(categoryData.getString("categories." + s + ".damage"));
            name = categoryData.getString("categories." + s + ".name");
            if (categoryData.getStringList("categories." + s + ".lore") != null)
                lore = categoryData.getStringList("categories." + s + ".lore");
            slot = categoryData.getInt("categories." + s + ".slot");
            invSize = categoryData.getInt("categories." + s + ".rows") * 9;
            ItemStack itemStack = new ItemStack(material, amount, data);
            ItemMeta meta = itemStack.getItemMeta();
            meta.setDisplayName(ChatUtils.chat(name));
            meta.setLore(lore);
            itemStack.setItemMeta(meta);

            categories.add(new Category(itemStack, s, slot, invSize));
        }
    }

    private void loadItems() {
        if (itemsData.getConfigurationSection("shops") == null) {
            saveDefaultItemData();
            reloadItems();
            return;
        }
        for (String s : itemsData.getConfigurationSection("shops").getKeys(false)) {
            Category c = getCategory(s);
            Inventory category = Bukkit.createInventory(null, c.getInvSize(), c.getIcon().getItemMeta().getDisplayName());
            ArrayList<ShopItem> shopItems = new ArrayList<>();
            for (String j : itemsData.getConfigurationSection("shops." + s).getKeys(false)) {
                Material material;
                short data = 0;
                int amount = 1;
                int randMin = -1;
                int randMax = -1;
                String name = "";
                String type = "ITEM";
                String enchant = "";
                List<String> lore = new ArrayList<>();
                List<String> commands = new ArrayList<>();
                List<String> enchants = new ArrayList<>();

                // load itemstack based on data
                material = Material.getMaterial(itemsData.getString("shops." + s + "." + j + ".material"));
                if (material == null) material = Material.DIRT;
                amount = itemsData.getInt("shops." + s + "." + j + ".quantity");
                if (itemsData.getString("shops." + s + "." + j + ".damage") != null)
                    data = Short.parseShort(itemsData.getString("shops." + s + "." + j + ".damage"));
                name = itemsData.getString("shops." + s + "." + j + ".name");
                if (itemsData.getStringList("shops." + s + "." + j + ".lore") != null)
                    for (String l : itemsData.getStringList("shops." + s + "." + j + ".lore"))
                        lore.add(ChatUtils.chat(l));

                if (itemsData.getStringList("shops." + s + "." + j + ".enchants") != null)
                    enchants = itemsData.getStringList("shops." + s + "." + j + ".enchants");

                int slot = itemsData.getInt("shops." + s + "." + j + ".slot");
                int price = itemsData.getInt("shops." + s + "." + j + ".price");

                if (itemsData.getString("shops." + s + "." + j + ".type") != null)
                {
                    type = itemsData.getString("shops." + s + "." + j + ".type");
                }

                if (type.equals("COMMAND"))
                {
                    commands = itemsData.getStringList("shops." + s + "." + j + ".commands");
                } else if (type.equals("RANDOMENCHANTBOOK"))
                {
                    randMin = itemsData.getInt("shops." + s + "." + j + ".randomMin");
                    randMax = itemsData.getInt("shops." + s + "." + j + ".randomMax");
                    enchant = itemsData.getString("shops." + s + "." + j + ".bookenchant");
                }

                ItemStack itemStack = new ItemStack(material, amount, data);
                if (enchants != null)
                {
                    for (String en : enchants)
                    {
                        itemStack.addUnsafeEnchantment(EnchantNames.getEnchantment(en.split(":")[0]), Integer.parseInt(en.split(":")[1]));
                    }
                }
                ItemMeta meta = itemStack.getItemMeta();
                if (name != null) meta.setDisplayName(ChatUtils.chat(name));
                if (lore != null) meta.setLore(lore);
                itemStack.setItemMeta(meta);

                ShopItem shopItem = new ShopItem(itemStack, price, slot);
                shopItem.setType(type);
                if (type.equals("COMMAND"))
                {
                    shopItem.setCommands(commands);
                } else if (type.equals("RANDOMENCHANTBOOK"))
                {
                    shopItem.setRandMin(randMin);
                    shopItem.setRandMax(randMax);
                    shopItem.setEnchantment(EnchantNames.getEnchantment(enchant));
                }
                shopItems.add(shopItem);

                ItemStack guiItem = ItemBuilder.getItemStack(material, amount, data, name, lore);
                if (enchants != null)
                {
                    for (String en : enchants)
                    {
                        guiItem.addUnsafeEnchantment(EnchantNames.getEnchantment(en.split(":")[0]), Integer.parseInt(en.split(":")[1]));
                    }
                }
                lore.add(ChatUtils.chat("&f&lPRICE: &f" + price + " coins"));
                if (lore != null) meta.setLore(lore);
                guiItem.setItemMeta(meta);
                category.setItem(slot, guiItem);
            }


            ItemStack lightfillerItem = ItemBuilder.getItemStack(UMaterial.GRAY_STAINED_GLASS_PANE.getItemStack(), " ");
            ItemStack backButton = ItemBuilder.getItemStack(Material.BARRIER, 1, (short) 0, ChatUtils.chat("&c&lBACK"));
            ItemStack darkFillerItem = ItemBuilder.getItemStack(UMaterial.BLACK_STAINED_GLASS_PANE.getItemStack(), " ");

            category.setItem(category.getSize() - 9, backButton);

            int i = 0;

            for (ItemStack itemStack : category.getContents()) {
                if (itemStack == null) {
                    i++;
                }
            }
            for (int j = 0; j < i; j++) {
                category.setItem(category.firstEmpty(), (category.firstEmpty() % 2 == 1) ? lightfillerItem : darkFillerItem);
            }
            if (c != null)
            {
                c.setInventory(category);
                c.setCategoryItems(shopItems);
            }
        }
    }

    public void reloadCategories() {
        categories.clear();
        loadCategories();
    }

    public void reloadItems() {
        reloadCategories();
        loadItems();
    }


    public ArrayList<Category> getCategories() {
        return categories;
    }

    public Category getCategory(String name)
    {
        for (Category category : categories)
        {
            if (category.getName().equalsIgnoreCase(name)) return category;
        }
        return null;
    }

    public Inventory getCategoryInventory(Player player) {
        loadCategories();
        Inventory categoryInventory = Bukkit.createInventory(null, ConfigData.rows * 9, ChatUtils.chat("&8&nCoin Shop"));

        for (GUIItem item : ConfigData.shopFillerItems)
        {
            for (int slot : item.getSlots()) {
                categoryInventory.setItem(slot, item.getItemStack());
            }
        }

        for (Category category : categories)
        {
            categoryInventory.setItem(category.getSlot(), category.getIcon());
        }

        ///convert = ItemBuilder.getItemStack(UMaterial.EXPERIENCE_BOTTLE.getItemStack(), ChatUtils.chat("&3&l[!] Convert XP TO COINS"));
        //categoryInventory.setItem(18, convert);
        //ItemStack playerSkull = ItemBuilder.getSkullFromName(player.getName());
        //categoryInventory.setItem(26, ItemBuilder.getItemStack(playerSkull, ChatUtils.chat("&3&l"+player.getName()), Arrays.asList(new String[]{ChatUtils.chat("&7Stats:"), ChatUtils.chat("&7Coins: " + RetronixCoinsAPI.getDataHandler().getProfile(player).getCoins())})));
        return categoryInventory;
    }

}
