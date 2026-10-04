package com.github.laxika.magicalvibes.service.cast;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Chooses convoke contributions considering available mana and each creature's colors. */
public final class ConvokePaymentSupport {

    private ConvokePaymentSupport() {
    }

    /** Returns one contribution per creature, or null when no assignment pays the cost. */
    public static List<ManaColor> choose(ManaCost cost, ManaPool pool, int additionalGenericCost,
                                        List<Set<ManaColor>> creatureColors) {
        List<ManaColor> optimistic = new ArrayList<>();
        for (Set<ManaColor> colors : creatureColors) {
            if (colors.isEmpty()) optimistic.add(null);
            else optimistic.addAll(colors);
        }
        if (!cost.canPayWithConvoke(pool, additionalGenericCost, optimistic)) return null;
        return choose(cost, pool, additionalGenericCost, creatureColors, 0,
                new ArrayList<>(), new EnumMap<>(ManaColor.class), new HashSet<>());
    }

    private static List<ManaColor> choose(ManaCost cost, ManaPool pool, int additionalGenericCost,
                                          List<Set<ManaColor>> colors, int index,
                                          List<ManaColor> contributions, Map<ManaColor, Integer> counts,
                                          Set<String> failed) {
        if (cost.canPayWithConvoke(pool, additionalGenericCost, contributions)) {
            List<ManaColor> chosen = new ArrayList<>(contributions);
            while (chosen.size() < colors.size()) chosen.add(null);
            return chosen;
        }
        if (index == colors.size()) {
            return null;
        }
        String state = index + ":" + counts;
        if (failed.contains(state)) return null;
        List<ManaColor> options = new ArrayList<>(colors.get(index));
        options.sort(java.util.Comparator.comparingInt(color ->
                -(cost.getColoredCosts().getOrDefault(color, 0)
                        - pool.get(color) - counts.getOrDefault(color, 0))));
        options.add(null);
        for (ManaColor color : options) {
            contributions.add(color);
            if (color != null) counts.merge(color, 1, Integer::sum);
            List<ManaColor> chosen = choose(cost, pool, additionalGenericCost, colors, index + 1,
                    contributions, counts, failed);
            contributions.removeLast();
            if (color != null) {
                counts.compute(color, (ignored, count) -> count == 1 ? null : count - 1);
            }
            if (chosen != null) return chosen;
        }
        failed.add(state);
        return null;
    }
}
