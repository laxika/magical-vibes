package com.github.laxika.magicalvibes.cards;

import com.github.laxika.magicalvibes.model.Card;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public final class CardScanner {

    private CardScanner() {
    }

    public static Map<CardSet, List<CardPrinting>> scan() {
        Map<String, CardSet> codeToSet = new HashMap<>();
        for (CardSet cs : CardSet.values()) {
            codeToSet.put(cs.getCode(), cs);
        }

        Map<CardSet, List<CardPrinting>> result = new EnumMap<>(CardSet.class);
        for (CardSet cs : CardSet.values()) {
            result.put(cs, new ArrayList<>());
        }

        try (ScanResult scanResult = new ClassGraph()
                .acceptPackages("com.github.laxika.magicalvibes.cards")
                .enableClassInfo()
                .enableAnnotationInfo()
                .scan()) {

            for (ClassInfo classInfo : scanResult.getClassesWithAnnotation(CardRegistration.class)) {
                processClass(classInfo, codeToSet, result);
            }
            for (ClassInfo classInfo : scanResult.getClassesWithAnnotation(CardRegistrations.class)) {
                processClass(classInfo, codeToSet, result);
            }
        }

        for (List<CardPrinting> printings : result.values()) {
            // Collector numbers may be alphanumeric (meld results like "14b").
            printings.sort(Comparator
                    .comparingInt((CardPrinting p) -> leadingCollectorNumber(p.collectorNumber()))
                    .thenComparing(CardPrinting::collectorNumber));
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private static void processClass(ClassInfo classInfo, Map<String, CardSet> codeToSet,
                                     Map<CardSet, List<CardPrinting>> result) {
        Class<?> clazz = classInfo.loadClass();
        if (!Card.class.isAssignableFrom(clazz)) {
            return;
        }

        Supplier<Card> factory = createFactory((Class<? extends Card>) clazz);

        for (CardRegistration reg : clazz.getAnnotationsByType(CardRegistration.class)) {
            CardSet cardSet = codeToSet.get(reg.set());
            if (cardSet == null) {
                throw new IllegalStateException(
                        "Unknown set code '" + reg.set() + "' on " + clazz.getSimpleName());
            }
            result.get(cardSet).add(new CardPrinting(reg.set(), reg.collectorNumber(), factory));
        }
    }

    /**
     * The collector number {@code cardClass} is registered under in {@code setCode}, empty if it
     * has no printing there.
     *
     * <p>For the cards the engine builds outside the printing list. A meld result is its own
     * printing — Brisela, Voice of Nightmares is INR 14b, not Gisela's INR 24 — and melding is the
     * only way it reaches the battlefield, so nothing ever hands the engine its {@link
     * CardPrinting} and it would otherwise carry no collector number at all.
     */
    public static Optional<String> collectorNumberOf(Class<? extends Card> cardClass, String setCode) {
        for (CardRegistration reg : cardClass.getAnnotationsByType(CardRegistration.class)) {
            if (reg.set().equals(setCode)) {
                return Optional.of(reg.collectorNumber());
            }
        }
        return Optional.empty();
    }

    /** Leading integer of a collector number ("14b" → 14, "24" → 24). */
    private static int leadingCollectorNumber(String collectorNumber) {
        int i = 0;
        while (i < collectorNumber.length() && Character.isDigit(collectorNumber.charAt(i))) {
            i++;
        }
        if (i == 0) {
            return 0;
        }
        return Integer.parseInt(collectorNumber.substring(0, i));
    }

    private static Supplier<Card> createFactory(Class<? extends Card> clazz) {
        try {
            Constructor<? extends Card> ctor = clazz.getDeclaredConstructor();
            ctor.setAccessible(true);
            return () -> {
                try {
                    return ctor.newInstance();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to instantiate " + clazz.getSimpleName(), e);
                }
            };
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("No no-arg constructor on " + clazz.getSimpleName(), e);
        }
    }
}
