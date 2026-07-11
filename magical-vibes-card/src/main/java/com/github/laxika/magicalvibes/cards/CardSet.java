package com.github.laxika.magicalvibes.cards;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public enum CardSet {

    SET_9ED("9ED"),
    SET_10E("10E"),
    SET_M10("M10"),
    SET_M11("M11"),
    SET_LRW("LRW"),
    SET_ECL("ECL"),
    SET_SOM("SOM"),
    SET_MBS("MBS"),
    SET_NPH("NPH"),
    SET_ISD("ISD"),
    SET_DKA("DKA"),
    SET_XLN("XLN"),
    SET_DOM("DOM"),
    SET_SOS("SOS"),
    SET_POR("POR");

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
