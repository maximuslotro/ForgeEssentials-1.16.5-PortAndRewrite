package com.forgeessentials.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;

import com.forgeessentials.core.ForgeEssentials;
import com.forgeessentials.core.config.ConfigData;
import com.forgeessentials.core.config.ConfigLoaderBase;
import com.forgeessentials.util.output.LoggingHandler;
import com.google.common.base.Strings;

public class Censor extends ConfigLoaderBase
{

    private static final String CONFIG_CATEGORY = "Censor";

    private static final String[] DEFAULT_WORDS = new String[] { "fuck\\S*", "bastard", "moron", "ass", "asshole", "bitch", "shit" };

    private static final String CENSOR_HELP = "Words to be censored. Prepend with ! to disable word boundary check.";

    private static List<CensoredWord> filterList = new ArrayList<>();

    public static boolean enabled;

    public static String censorSymbol;

    public static int censorSlap;

    public static class CensoredWord
    {

        public String word;

        public String blank;

        public Pattern pattern;

        public CensoredWord(String word)
        {
            if (word.startsWith("!"))
                word = word.substring(1);
            else
                word = "\\b" + word + "\\b";
            pattern = Pattern.compile(word, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE);
        }

    }

    public Censor()
    {
        ForgeEssentials.getConfigManager().registerSpecs(ModuleChat.CONFIG_FILE, this);
    }

    static ForgeConfigSpec.BooleanValue FEenabled;
    static ForgeConfigSpec.IntValue FEcensorSlap;
    static ForgeConfigSpec.ConfigValue<String> FEcensorSymbol;
    static ForgeConfigSpec.ConfigValue<String[]> FEfilterList;

	@Override
	public void load(Builder BUILDER, boolean isReload)
	{
        BUILDER.push(CONFIG_CATEGORY);
        FEenabled = BUILDER.comment("Enable Chat Censor?").define("enable", true);
        FEcensorSlap = BUILDER.comment("Damage to a player when he uses a censored word").defineInRange("slapDamage", 1, 0, Integer.MAX_VALUE);
        FEcensorSymbol = BUILDER.comment("Replace censored words with this character").define("censorSymbol", "#");
        FEfilterList = BUILDER.comment(CENSOR_HELP).define("words", DEFAULT_WORDS);
        BUILDER.pop();
    }

	@Override
	public void bakeConfig(boolean reload)
	{
        enabled = FEenabled.get();
        censorSlap = FEcensorSlap.get();
        censorSymbol = FEcensorSymbol.get();
        if (censorSymbol.length() > 1)
        {
            LoggingHandler.felog.warn("Censor symbol is too long!");
            censorSymbol = censorSymbol.substring(1);
        }
        else if (censorSymbol.isEmpty())
        {
            LoggingHandler.felog.warn("Censor symbol is empty!");
            censorSymbol = "#";
        }
        filterList.clear();
        for (String word : FEfilterList.get())
            filterList.add(new CensoredWord(word));
    }

    public String filter(String message)
    {
        return filter(message, null);
    }

    public String filter(String message, PlayerEntity player)
    {
        if (!enabled)
            return message;
        for (CensoredWord filter : filterList)
        {
            Matcher m = filter.pattern.matcher(message);
            if (m.find())
            {
                if (filter.blank == null)
                    filter.blank = Strings.repeat(censorSymbol, m.end() - m.start());
                message = m.replaceAll(filter.blank);
                if (player != null && censorSlap != 0)
                    player.hurt(DamageSource.GENERIC, censorSlap);
            }
        }
        return message;
    }

    public String filterIRC(String message)
    {
        if (!enabled)
            return message;
        for (CensoredWord filter : filterList)
        {
            Matcher m = filter.pattern.matcher(message);
            if (m.find())
            {
                if (filter.blank == null)
                    filter.blank = Strings.repeat(censorSymbol, m.end() - m.start());
                message = m.replaceAll(filter.blank);
            }
        }
        return message;
    }

	@Override
	public ConfigData returnData() {
		return ModuleChat.data;
	}
}
