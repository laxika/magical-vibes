package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ManaPoolConvergeTest {

    @Test
    @DisplayName("Counts distinct colored mana spent from pool snapshots")
    void countsDistinctColoredManaSpent() {
        EnumMap<ManaColor, Integer> before = new EnumMap<>(ManaColor.class);
        before.put(ManaColor.WHITE, 2);
        before.put(ManaColor.BLACK, 3);

        EnumMap<ManaColor, Integer> after = new EnumMap<>(ManaColor.class);
        after.put(ManaColor.WHITE, 1);
        after.put(ManaColor.BLACK, 2);

        int converge = ManaPool.countDistinctColoredManaSpent(before, after, List.of());

        assertThat(converge).isEqualTo(2);
    }

    @Test
    @DisplayName("Includes convoke contributions in Converge count")
    void includesConvokeContributions() {
        EnumMap<ManaColor, Integer> totals = new EnumMap<>(ManaColor.class);
        totals.put(ManaColor.BLACK, 5);

        int converge = ManaPool.countDistinctColoredManaSpent(
                totals, totals, List.of(ManaColor.RED, ManaColor.GREEN));

        assertThat(converge).isEqualTo(2);
    }

    @Test
    @DisplayName("Colorless mana spent does not increase Converge")
    void ignoresColorlessMana() {
        ManaPool pool = new ManaPool();
        pool.add(ManaColor.COLORLESS, 5);
        pool.add(ManaColor.BLACK, 1);

        EnumMap<ManaColor, Integer> before = pool.getColoredManaTotals();
        pool.remove(ManaColor.BLACK);
        EnumMap<ManaColor, Integer> after = pool.getColoredManaTotals();

        int converge = ManaPool.countDistinctColoredManaSpent(before, after, List.of());

        assertThat(converge).isEqualTo(1);
    }
}
