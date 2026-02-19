package com.github.laxika.magicalvibes.cards;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public enum CardSet {

    TENTH_EDITION("10E"),
    LORWYN_ECLIPSED("ECL");

    private static final Map<String, String> setNameRegistry = new ConcurrentHashMap<>();
    private static volatile Map<CardSet, List<CardPrinting>> scannedPrintings;

    @Getter
    private final String code;

    public static void registerSetName(String code, String name) {
        setNameRegistry.put(code, name);
    }

    public static void clearSetNameRegistry() {
        setNameRegistry.clear();
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
