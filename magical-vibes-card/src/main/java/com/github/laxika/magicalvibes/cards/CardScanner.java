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
            printings.sort(Comparator.comparingInt(p -> Integer.parseInt(p.collectorNumber())));
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
