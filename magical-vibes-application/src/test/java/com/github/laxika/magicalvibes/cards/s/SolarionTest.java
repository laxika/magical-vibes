package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(Solarion.class)
class SolarionTest extends BaseCardTest {

    @Test
    @DisplayName("Sunburst counts each color of mana spent to cast Solarion once")
    void sunburstCountsDistinctColors() {
        harness.setHand(player1, List.of(new Solarion()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent solarion = findPermanent(player1, "Solarion");

        assertThat(solarion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(solarion.getEffectivePower()).isEqualTo(5);
        assertThat(solarion.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Sunburst counts each color only once and ignores colorless mana")
    void sunburstCountsEachColorOnlyOnce() {
        harness.setHand(player1, List.of(new Solarion()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent solarion = findPermanent(player1, "Solarion");

        assertThat(solarion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping Solarion doubles its +1/+1 counters")
    void tappingDoublesCounters() {
        Permanent solarion = addCreatureReady(player1, new Solarion());
        solarion.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(solarion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(solarion.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Solarion cast with only colorless mana enters without counters and dies")
    void colorlessCastingAddsNoCounters() {
        harness.setHand(player1, List.of(new Solarion()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Solarion");
        harness.assertInGraveyard(player1, "Solarion");
    }

    @Test
    @DisplayName("Solarion doubles the counter count at resolution rather than activation")
    void doublingUsesCurrentCounterCount() {
        Permanent solarion = addCreatureReady(player1, new Solarion());
        solarion.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.activateAbility(player1, 0, null, null);
        assertThat(solarion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        solarion.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        harness.passBothPriorities();

        assertThat(solarion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(8);
    }

    @Test
    @DisplayName("Solarion doubles only +1/+1 counters")
    void doublingLeavesOtherCountersUnchanged() {
        Permanent solarion = addCreatureReady(player1, new Solarion());
        solarion.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        solarion.setCounterCount(CounterType.CHARGE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(solarion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(solarion.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Solarion cannot pay its tap cost while summoning sick")
    void summoningSicknessPreventsActivation() {
        harness.setHand(player1, List.of(new Solarion()));
        harness.addMana(player1, ManaColor.GREEN, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanent(player1, "Solarion").isTapped()).isFalse();
        assertThat(findPermanent(player1, "Solarion").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
    }
}
