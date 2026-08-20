package com.github.laxika.magicalvibes.carddata;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Vocabulary and plumbing shared by every oracle-data loader: the upstream symbol/keyword
 * spellings, JSON field parsing, and cache-file writing.
 *
 * <p>Provider-neutral. These lived on the Scryfall loader, which forced the MTGJSON loader to
 * depend on it for basics that were never Scryfall's.
 */
public final class CardDataSupport {

    private CardDataSupport() {
    }

    public static final Map<String, CardColor> COLOR_MAP = Map.of(
            "W", CardColor.WHITE,
            "U", CardColor.BLUE,
            "B", CardColor.BLACK,
            "R", CardColor.RED,
            "G", CardColor.GREEN
    );

    public static final Map<String, Keyword> KEYWORD_MAP = new HashMap<>();

    static {
        KEYWORD_MAP.put("Flying", Keyword.FLYING);
        KEYWORD_MAP.put("Banding", Keyword.BANDING);
        KEYWORD_MAP.put("Reach", Keyword.REACH);
        KEYWORD_MAP.put("Defender", Keyword.DEFENDER);
        KEYWORD_MAP.put("Double strike", Keyword.DOUBLE_STRIKE);
        KEYWORD_MAP.put("First strike", Keyword.FIRST_STRIKE);
        KEYWORD_MAP.put("Flash", Keyword.FLASH);
        KEYWORD_MAP.put("Fading", Keyword.FADING);
        KEYWORD_MAP.put("Vigilance", Keyword.VIGILANCE);
        KEYWORD_MAP.put("Shroud", Keyword.SHROUD);
        KEYWORD_MAP.put("Changeling", Keyword.CHANGELING);
        KEYWORD_MAP.put("Fear", Keyword.FEAR);
        KEYWORD_MAP.put("Menace", Keyword.MENACE);
        KEYWORD_MAP.put("Indestructible", Keyword.INDESTRUCTIBLE);
        KEYWORD_MAP.put("Convoke", Keyword.CONVOKE);
        KEYWORD_MAP.put("Improvise", Keyword.IMPROVISE);
        KEYWORD_MAP.put("Haste", Keyword.HASTE);
        KEYWORD_MAP.put("Lifelink", Keyword.LIFELINK);
        KEYWORD_MAP.put("Trample", Keyword.TRAMPLE);
        KEYWORD_MAP.put("Forestwalk", Keyword.FORESTWALK);
        KEYWORD_MAP.put("Mountainwalk", Keyword.MOUNTAINWALK);
        KEYWORD_MAP.put("Islandwalk", Keyword.ISLANDWALK);
        KEYWORD_MAP.put("Swampwalk", Keyword.SWAMPWALK);
        KEYWORD_MAP.put("Plainswalk", Keyword.PLAINSWALK);
        KEYWORD_MAP.put("Hexproof", Keyword.HEXPROOF);
        KEYWORD_MAP.put("Infect", Keyword.INFECT);
        KEYWORD_MAP.put("Wither", Keyword.WITHER);
        KEYWORD_MAP.put("Intimidate", Keyword.INTIMIDATE);
        KEYWORD_MAP.put("Battle Cry", Keyword.BATTLE_CRY);
        KEYWORD_MAP.put("Living weapon", Keyword.LIVING_WEAPON);
        KEYWORD_MAP.put("Deathtouch", Keyword.DEATHTOUCH);
        KEYWORD_MAP.put("Transform", Keyword.TRANSFORM);
        KEYWORD_MAP.put("Flashback", Keyword.FLASHBACK);
        KEYWORD_MAP.put("Aftermath", Keyword.AFTERMATH);
        KEYWORD_MAP.put("Kicker", Keyword.KICKER);
        KEYWORD_MAP.put("Converge", Keyword.CONVERGE);
        KEYWORD_MAP.put("Undying", Keyword.UNDYING);
        KEYWORD_MAP.put("Persist", Keyword.PERSIST);
        KEYWORD_MAP.put("Increment", Keyword.INCREMENT);
        KEYWORD_MAP.put("Paradigm", Keyword.PARADIGM);
        KEYWORD_MAP.put("Horsemanship", Keyword.HORSEMANSHIP);
        KEYWORD_MAP.put("Shadow", Keyword.SHADOW);
        KEYWORD_MAP.put("Flanking", Keyword.FLANKING);
        KEYWORD_MAP.put("Conspire", Keyword.CONSPIRE);
        KEYWORD_MAP.put("Retrace", Keyword.RETRACE);
        KEYWORD_MAP.put("Jump", Keyword.JUMP_START);
        KEYWORD_MAP.put("Jump-start", Keyword.JUMP_START);
        KEYWORD_MAP.put("Emerge", Keyword.EMERGE);
        KEYWORD_MAP.put("Coven", Keyword.COVEN);
        KEYWORD_MAP.put("Meld", Keyword.MELD);
        KEYWORD_MAP.put("Training", Keyword.TRAINING);
        KEYWORD_MAP.put("Disturb", Keyword.DISTURB);
        KEYWORD_MAP.put("Morph", Keyword.MORPH);
        KEYWORD_MAP.put("Skulk", Keyword.SKULK);
        KEYWORD_MAP.put("Soulbond", Keyword.SOULBOND);
        KEYWORD_MAP.put("Flashback", Keyword.FLASHBACK);
        KEYWORD_MAP.put("Exploit", Keyword.EXPLOIT);
        KEYWORD_MAP.put("Miracle", Keyword.MIRACLE);
        KEYWORD_MAP.put("Madness", Keyword.MADNESS);
        KEYWORD_MAP.put("Escalate", Keyword.ESCALATE);
        KEYWORD_MAP.put("Decayed", Keyword.DECAYED);
        KEYWORD_MAP.put("Blight", Keyword.BLIGHT);
        KEYWORD_MAP.put("Splice", Keyword.SPLICE);
        KEYWORD_MAP.put("Delirium", Keyword.DELIRIUM);
        KEYWORD_MAP.put("Prepared", Keyword.PREPARED);
        KEYWORD_MAP.put("Phasing", Keyword.PHASING);
        KEYWORD_MAP.put("Buyback", Keyword.BUYBACK);
        KEYWORD_MAP.put("Evolve", Keyword.EVOLVE);
        KEYWORD_MAP.put("Offering", Keyword.OFFERING);
        KEYWORD_MAP.put("Delve", Keyword.DELVE);
        KEYWORD_MAP.put("Modular", Keyword.MODULAR);
        KEYWORD_MAP.put("Sunburst", Keyword.SUNBURST);
        KEYWORD_MAP.put("Prototype", Keyword.PROTOTYPE);
        KEYWORD_MAP.put("Rebound", Keyword.REBOUND);
        KEYWORD_MAP.put("Recover", Keyword.RECOVER);
        KEYWORD_MAP.put("Start your engines!", Keyword.START_YOUR_ENGINES);
        KEYWORD_MAP.put("Max speed", Keyword.MAX_SPEED);
        KEYWORD_MAP.put("Epic", Keyword.EPIC);
        KEYWORD_MAP.put("Toxic", Keyword.TOXIC);
        KEYWORD_MAP.put("Compleated", Keyword.COMPLEATED);
    }

    /** {@link #KEYWORD_MAP} keyed by lowercase spelling — upstream casing differs between sources. */
    private static final Map<String, Keyword> KEYWORD_MAP_LOWERCASE = new HashMap<>();

    static {
        KEYWORD_MAP.forEach((name, keyword) ->
                KEYWORD_MAP_LOWERCASE.put(name.toLowerCase(Locale.ROOT), keyword));
    }

    /**
     * The keyword an upstream spelling names, or null when this game has no enum for it (which is
     * normal — {@link #KEYWORD_MAP} covers only what the engine implements). Matching is
     * case-insensitive: MTGJSON and Scryfall disagree on casing for some keywords.
     */
    public static Keyword keyword(String upstreamName) {
        return upstreamName == null ? null : KEYWORD_MAP_LOWERCASE.get(upstreamName.toLowerCase(Locale.ROOT));
    }

    public static Integer parseIntField(JsonNode node, String field) {
        if (!node.has(field)) return null;
        return parseInt(node.get(field).asText());
    }

    /**
     * @return null for an absent value, 0 for one that is present but not a number — power and
     * toughness are printed as "*" on characteristic-defining creatures
     */
    public static Integer parseInt(String value) {
        if (value == null) return null;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /** A string field's value, or null when the node does not carry it. */
    public static String text(JsonNode node, String field) {
        return node.has(field) ? node.get(field).asText() : null;
    }

    /** A string-array field's values, or an empty list when the node does not carry it. */
    public static List<String> strings(JsonNode node, String field) {
        if (!node.has(field) || !node.get(field).isArray()) {
            return List.of();
        }
        List<String> values = new ArrayList<>();
        for (JsonNode element : node.get(field)) {
            values.add(element.asText());
        }
        return values;
    }

    /**
     * Writes a cache file via temp file + atomic move, so concurrent loaders (parallel test JVMs
     * sharing one cache directory) either see a complete file or no file — never a partial write.
     */
    public static void writeCacheFile(Path cacheFile, String content) throws IOException {
        Path tempFile = Files.createTempFile(cacheFile.getParent(), cacheFile.getFileName().toString(), ".tmp");
        try {
            Files.writeString(tempFile, content);
            try {
                Files.move(tempFile, cacheFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tempFile, cacheFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }
}
