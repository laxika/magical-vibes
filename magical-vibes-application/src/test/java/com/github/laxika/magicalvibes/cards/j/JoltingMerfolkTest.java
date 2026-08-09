package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JoltingMerfolkTest extends BaseCardTest {

    @Test
    @DisplayName("Jolting Merfolk enters with four fade counters")
    void entersWithFadeCounters() {
        harness.setHand(player1, List.of(new JoltingMerfolk()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent merfolk = findPermanent(player1, "Jolting Merfolk");
        assertThat(merfolk.getCounterCount(CounterType.FADE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Fading removes one fade counter during its controller's upkeep")
    void removesFadeCounterAtUpkeep() {
        Permanent merfolk = addCreatureReady(player1, new JoltingMerfolk());
        merfolk.setCounterCount(CounterType.FADE, 2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(merfolk.getCounterCount(CounterType.FADE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Jolting Merfolk");
    }

    @Test
    @DisplayName("Fading sacrifices Jolting Merfolk when it has no fade counters")
    void sacrificesWithoutFadeCounters() {
        addCreatureReady(player1, new JoltingMerfolk());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Jolting Merfolk");
    }

    @Test
    @DisplayName("Removing a fade counter taps target creature")
    void removesCounterAndTapsTargetCreature() {
        Permanent merfolk = addCreatureReady(player1, new JoltingMerfolk());
        merfolk.setCounterCount(CounterType.FADE, 1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(merfolk.getCounterCount(CounterType.FADE)).isZero();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The tapping ability cannot target a land")
    void cannotTargetLand() {
        addCreatureReady(player1, new JoltingMerfolk());
        Permanent forest = addCreatureReady(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
