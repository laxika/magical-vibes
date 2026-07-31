package com.github.laxika.magicalvibes.carddata;

import com.github.laxika.magicalvibes.model.CardColor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-printing data that is not part of a card's oracle text: rarity and token art.
 *
 * <p>Provider-neutral on purpose. Whichever loader the {@code oracle.data-provider} property
 * selects — see {@link OracleDataProvider} — populates this registry on startup, and everything
 * downstream reads from here. Modules outside {@code carddata} must never name a concrete loader
 * to get at this: an engine that asks {@code ScryfallOracleLoader} for token art reads as though
 * it breaks under MTGJSON, when in truth both loaders fill the same maps.
 */
public final class CardPrintingRegistry {

    private CardPrintingRegistry() {
    }

    /** "SET:collectorNumber" -> rarity ("common", "uncommon", "rare", "mythic"). */
    private static final Map<String, String> RARITY = new HashMap<>();

    /** Set code -> token key (see {@link #buildTokenKey}) -> the token card printing to draw. */
    private static final Map<String, Map<String, TokenImageData>> TOKEN_IMAGES = new ConcurrentHashMap<>();

    /** The printing a token's art comes from. */
    public record TokenImageData(String setCode, String collectorNumber) {}

    /** Returns the rarity for a card in a set, e.g. "common", "uncommon", "rare", "mythic". */
    public static String getRarity(String setCode, String collectorNumber) {
        return RARITY.get(setCode + ":" + collectorNumber);
    }

    public static void registerRarity(String setCode, String collectorNumber, String rarity) {
        RARITY.put(setCode + ":" + collectorNumber, rarity);
    }

    public static void registerTokenImages(String setCode, Map<String, TokenImageData> tokenMap) {
        TOKEN_IMAGES.put(setCode, tokenMap);
    }

    /** Drops every registered token image map. Used by unit tests that seed their own fixtures. */
    public static void clearTokenImages() {
        TOKEN_IMAGES.clear();
    }

    /**
     * Looks up the token art for a token created by a card from the given set. Prefers a printing
     * from that set; if the set has no matching token, falls back to any other registered set that
     * does (stable set-code order). Also tries stripping a "Phyrexian " prefix when there is no
     * exact match, since token cards use the pre-errata names (e.g. "Golem" instead of
     * "Phyrexian Golem"). Returns null when no registered set has the token, and for a null
     * {@code setCode} — callers that no longer know which card created the token get the artless
     * fallback rather than an exception.
     */
    public static TokenImageData getTokenImage(String setCode, String tokenName, int power, int toughness, CardColor color) {
        if (setCode == null) return null;
        TokenImageData preferred = lookupInSet(setCode, tokenName, power, toughness, color);
        if (preferred != null) return preferred;
        return findInOtherSets(setCode, tokenName, power, toughness, color);
    }

    /** Looks up the token art for a non-creature token (no power/toughness). */
    public static TokenImageData getTokenImage(String setCode, String tokenName, CardColor color) {
        if (setCode == null) return null;
        TokenImageData preferred = lookupInSet(setCode, tokenName, null, null, color);
        if (preferred != null) return preferred;
        return findInOtherSets(setCode, tokenName, null, null, color);
    }

    private static TokenImageData lookupInSet(String setCode, String tokenName,
                                              Integer power, Integer toughness, CardColor color) {
        Map<String, TokenImageData> tokenMap = TOKEN_IMAGES.get(setCode);
        if (tokenMap == null) return null;
        TokenImageData result = tokenMap.get(buildTokenKey(tokenName, power, toughness, color));
        if (result == null && tokenName.startsWith("Phyrexian ")) {
            result = tokenMap.get(buildTokenKey(
                    tokenName.substring("Phyrexian ".length()), power, toughness, color));
        }
        return result;
    }

    private static TokenImageData findInOtherSets(String preferredSetCode, String tokenName,
                                                  Integer power, Integer toughness, CardColor color) {
        return TOKEN_IMAGES.keySet().stream()
                .filter(other -> !other.equals(preferredSetCode))
                .sorted()
                .map(other -> lookupInSet(other, tokenName, power, toughness, color))
                .filter(data -> data != null)
                .findFirst()
                .orElse(null);
    }

    /** The key format of {@link #TOKEN_IMAGES}; both loaders build keys with it when registering. */
    public static String buildTokenKey(String name, Integer power, Integer toughness, CardColor color) {
        String colorKey = color != null ? color.name() : "COLORLESS";
        String p = power != null ? String.valueOf(power) : "*";
        String t = toughness != null ? String.valueOf(toughness) : "*";
        return name + ":" + p + ":" + t + ":" + colorKey;
    }
}
