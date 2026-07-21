package com.github.laxika.magicalvibes.cards;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public enum CardSet {

    SET_4ED("4ED"),
    SET_5ED("5ED"),
    SET_6ED("6ED"),
    SET_7ED("7ED"),
    SET_8ED("8ED"),
    SET_9ED("9ED"),
    SET_10E("10E"),
    SET_M10("M10"),
    SET_M11("M11"),
    SET_LRW("LRW"),
    SET_MOR("MOR"),
    SET_SHM("SHM"),
    SET_EVE("EVE"),
    SET_ECL("ECL"),
    SET_SOM("SOM"),
    SET_MBS("MBS"),
    SET_NPH("NPH"),
    SET_ISD("ISD"),
    SET_DKA("DKA"),
    SET_ALA("ALA"),
    SET_CON("CON"),
    SET_AKH("AKH"),
    SET_ARB("ARB"),
    SET_HOU("HOU"),
    SET_XLN("XLN"),
    SET_DOM("DOM"),
    SET_SOS("SOS"),
    SET_POR("POR"),
    SET_P02("P02"),
    SET_PTK("PTK"),
    SET_DRB("DRB");

    private static final Map<String, String> setNameRegistry = new ConcurrentHashMap<>();
    private static final Map<String, Integer> setCardTotalRegistry = new ConcurrentHashMap<>();
    private static volatile Map<CardSet, List<CardPrinting>> scannedPrintings;

    @Getter
    private final String code;

    public static void registerSetName(String code, String name) {
        setNameRegistry.put(code, name);
    }

    public static void clearSetNameRegistry() {
        setNameRegistry.clear();
    }

    /**
     * Records how many cards the set actually contains per the loaded oracle data source — the
     * denominator for {@link #getImplementedFraction()}. Populated by the oracle loaders at startup.
     */
    public static void registerSetCardTotal(String code, int total) {
        setCardTotalRegistry.put(code, total);
    }

    public static void clearSetCardTotalRegistry() {
        setCardTotalRegistry.clear();
    }

    /** Total number of cards in this set per the loaded oracle data, or 0 if not yet loaded. */
    public int getSetCardTotal() {
        return setCardTotalRegistry.getOrDefault(code, 0);
    }

    /**
     * Fraction (0..1) of this set's real card pool that is implemented: implemented printings over
     * the set's total card count. Returns 0 when the total is unknown (oracle data not yet loaded).
     */
    public double getImplementedFraction() {
        int total = getSetCardTotal();
        if (total <= 0) {
            return 0.0;
        }
        List<CardPrinting> printings = getPrintings();
        int implemented = printings == null ? 0 : printings.size();
        return Math.min(1.0, (double) implemented / total);
    }

    public List<CardPrinting> getPrintings() {
        ensureScanned();
        return scannedPrintings.get(this);
    }

    public String getName() {
        return setNameRegistry.getOrDefault(code, code);
    }

    public CardPrinting findByCollectorNumber(String collectorNumber) {
        return getPrintings().stream()
                .filter(p -> p.collectorNumber().equals(collectorNumber))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No printing with collector number " + collectorNumber + " in set " + code));
    }

    static void clearPrintingsCache() {
        scannedPrintings = null;
    }

    private static void ensureScanned() {
        if (scannedPrintings == null) {
            synchronized (CardSet.class) {
                if (scannedPrintings == null) {
                    scannedPrintings = CardScanner.scan();
                }
            }
        }
    }
}
