package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.k.KorHaven;
import com.github.laxika.magicalvibes.cards.r.RootwaterCommando;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JoltingMerfolk.class, RootwaterCommando.class, KorHaven.class})
class JoltingMerfolkTest extends BaseCardTest {

    @Test
    @DisplayName("Jolting Merfolk enters with four fade counters")
    void entersWithFadeCounters() {
        harness.castFromHand(player1, new JoltingMerfolk(), "{2}{U}{U}");
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
    @DisplayName("Fading leaves Jolting Merfolk on the battlefield after removing its last fade counter")
    void removesLastFadeCounterWithoutSacrificing() {
        Permanent merfolk = addCreatureReady(player1, new JoltingMerfolk());
        merfolk.setCounterCount(CounterType.FADE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(merfolk.getCounterCount(CounterType.FADE)).isZero();
        harness.assertOnBattlefield(player1, "Jolting Merfolk");
    }

    @Test
    @DisplayName("Fading does not remove a fade counter during an opponent's upkeep")
    void doesNotFadeDuringOpponentsUpkeep() {
        Permanent merfolk = addCreatureReady(player1, new JoltingMerfolk());
        merfolk.setCounterCount(CounterType.FADE, 2);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(merfolk.getCounterCount(CounterType.FADE)).isEqualTo(2);
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
        Permanent target = addCreatureReady(player2, new RootwaterCommando());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(merfolk.getCounterCount(CounterType.FADE)).isZero();
        assertThat(merfolk.isTapped()).isFalse();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The tapping ability cannot be activated without a fade counter")
    void cannotActivateWithoutFadeCounter() {
        Permanent merfolk = addCreatureReady(player1, new JoltingMerfolk());
        Permanent target = addCreatureReady(player2, new RootwaterCommando());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
        assertThat(merfolk.getCounterCount(CounterType.FADE)).isZero();
    }

    @Test
    @DisplayName("The tapping ability cannot target a land")
    void cannotTargetLand() {
        addCreatureReady(player1, new JoltingMerfolk());
        Permanent land = addCreatureReady(player2, new KorHaven());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
