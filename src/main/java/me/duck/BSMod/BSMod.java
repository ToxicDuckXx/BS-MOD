package me.duck.BSMod;


import com.google.gson.*;
import net.minecraft.client.Minecraft;

import net.minecraft.event.ClickEvent;

import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;


@Mod(modid = BSMod.MODID, version = BSMod.VERSION, clientSideOnly = true)
public class BSMod
{
    public static final String MODID = "bsmod";
    public static final String VERSION = "1.0";
    public volatile ArrayList<ArrayList<String>> responseF = null;
    private String apikey;
    public static Configuration config;

    @EventHandler
    public void preInnit(FMLPreInitializationEvent event) {

        config = new Configuration(event.getSuggestedConfigurationFile());
        syncConfig();
    }


    @EventHandler
    public void init(FMLInitializationEvent event)
    {
		// some example code
        MinecraftForge.EVENT_BUS.register(this);
        ClientCommandHandler.instance.registerCommand(new LookupCommand());
        ClientCommandHandler.instance.registerCommand(new ApiSetCommand());

    }

    public void onPlayerTick() {

        System.out.println("Command Working");
        new Thread(new Runnable() {
            public void run() {
                try {
                    responseF = Scan();
                    for (int i = 0; i < responseF.size(); i++) {
                        printStuff(responseF.get(i));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public void printStuff(ArrayList<String> l) {

        ChatStyle link = new ChatStyle();
        link.setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/viewauction " + l.get(2)));
        link.setColor(EnumChatFormatting.GOLD);
        IChatComponent text = new ChatComponentText(l.get(0) + " " + l.get(1) + " > " + l.get(3) + "   [BUY]").setChatStyle(link);

        Minecraft.getMinecraft().thePlayer.addChatMessage(text);

    }

    public static ArrayList<ArrayList<String>> Scan() throws IOException {
        ArrayList<ArrayList<String>> finalResponse = new ArrayList<ArrayList<String>>();
        String page = "0";

        int pageNum = GetAmountPages();
        for (int i = 0; i < pageNum; i++){

            ArrayList<ArrayList<String>> r =parseJson(GetPage(page));
            ArrayList<ArrayList<String>> al= new ArrayList<ArrayList<String>>();
            al.addAll(r);
            al.addAll(finalResponse);
            finalResponse = al;
            int integer = Integer.parseInt(page);
            integer += 1;
            page = String.valueOf(integer);

        }
        ArrayList<ArrayList<String>> r = GetGoodFlips(finalResponse);


        return r;

    }

    public static String GetPage(String page) throws IOException {
        BSMod m = new BSMod();

        String response = "";
        URL url = new URL("https://api.hypixel.net/skyblock/auctions?key=" + apiKey + "&page=" + page);
        URLConnection yc = url.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(
                yc.getInputStream()));
        String inputLine;
        while ((inputLine = in.readLine()) != null)
            response = inputLine;
        in.close();
        return response;

    }


    public static int GetAmountPages() throws IOException {

        String response = "";
        URL url = new URL("https://api.hypixel.net/skyblock/auctions?key=" + apiKey + "&page=0");
        URLConnection yc = url.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(
                yc.getInputStream()));
        String inputLine;
        while ((inputLine = in.readLine()) != null)
            response = inputLine;
        in.close();
        JsonObject obj = new JsonParser().parse(response).getAsJsonObject();
        int pages = obj.get("totalPages").getAsInt();

        return pages;
    }

    public static ArrayList<String> getSbItems() throws  IOException{

        ArrayList<String> response = new ArrayList<String>();
        URL url = new URL("https://textbin.net/raw/ebay5b5fer");
        URLConnection yc = url.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(
                yc.getInputStream()));
        String inputLine;
        while ((inputLine = in.readLine()) != null)
            response.add(inputLine);
        in.close();

    return response;
    }

    public static ArrayList<ArrayList<String>> parseJson(String response) throws IOException {

        ArrayList<ArrayList<String>> responseList = new ArrayList<ArrayList<String>>();

        JsonObject object = new JsonParser().parse(response.toString()).getAsJsonObject();

        JsonArray getArray = object.getAsJsonArray("auctions");

        for (int i = 0; i < getArray.size(); i++){
            JsonObject objInArray = getArray.get(i).getAsJsonObject();

            if (objInArray.has("bin")){
                String name = objInArray.get("item_name").getAsString();
                int price = objInArray.get("starting_bid").getAsInt();
                String uuid = objInArray.get("uuid").getAsString();
                ArrayList<String> l = new ArrayList<String>();
                l.add(name);
                l.add(Integer.toString(price));
                l.add(uuid);
                responseList.add(l);

            }
        }


        return responseList;
    }

    public static ArrayList<ArrayList<String>> GetGoodFlips(ArrayList<ArrayList<String>> responseList) throws IOException {

        ArrayList<String> sbItemList = getSbItems();
        ArrayList<ArrayList<String>> goodFlips = new ArrayList<ArrayList<String>>();

        for (int i = 0; i < sbItemList.size(); i++){
            int price = Integer.MAX_VALUE;
            int priceSecond = Integer.MAX_VALUE;
            int value = 0;
            int valueTwo = 0;
            ArrayList<ArrayList<String>> itemArray = new ArrayList<ArrayList<String>>();
            for (int e = 0; e < responseList.size(); e++){
                ArrayList<String> s = responseList.get(e);
                if (s.get(0).contains(sbItemList.get(i))){

                    itemArray.add(s);
                }

            }



            if (itemArray.size() > 1){
                for (int f = 0; f < itemArray.size(); f++) {
                    ArrayList<String> s = itemArray.get(f);
                    int priceOfItem = Integer.parseInt(s.get(1));

                    if (priceOfItem < price) {
                        price = priceOfItem;
                        value = f;
                    }
                }
                ArrayList<String> lowestPrice = itemArray.get(value);
                itemArray.remove(value);
                for (int e = 0; e < itemArray.size(); e++){
                    ArrayList<String> s1 = itemArray.get(e);
                    int priceOfItem1 = Integer.parseInt(s1.get(1));
                    if (priceOfItem1 < priceSecond) {
                        priceSecond = priceOfItem1;
                        valueTwo = e;
                    }
                }
                ArrayList<String> secondPrice = itemArray.get(valueTwo);
                lowestPrice.add(secondPrice.get(1));
                if (((priceSecond - price) > hMargin)||((priceSecond < 10000000)&&((priceSecond - price) > bMargin))) {
                    goodFlips.add(lowestPrice);
                }
            }



        }

        return goodFlips;
    }

    public static String apiKey = null;
    public static int bMargin = 0;
    public static int hMargin = 0;
    public static void syncConfig() {

        try{
            config.load();

            Property APIKEY = config.get(Configuration.CATEGORY_CLIENT, "APIKEY", "None", "API key");
            Property BMARGIN = config.get(Configuration.CATEGORY_CLIENT, "BMARGIN", 500000, "Margin below 10M");
            Property HMATGIN = config.get(Configuration.CATEGORY_CLIENT, "HMARGIN", 1000000, "Margin above 10M");
            apiKey = APIKEY.getString();
            bMargin = BMARGIN.getInt();
            hMargin = HMATGIN.getInt();
        } catch (Exception e) {
            System.out.println("Error writing to config");
        } finally {
            if (config.hasChanged()) {
                config.save();
            }
        }

    }



}

