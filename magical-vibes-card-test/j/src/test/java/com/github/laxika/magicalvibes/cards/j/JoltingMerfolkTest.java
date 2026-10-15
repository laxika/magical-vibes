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
        Permanent merfolk = addCreatureReady(player1, new JoltingMerfolk());
        merfolk.setCounterCount(CounterType.FADE, 1);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new KorHaven());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(merfolk.getCounterCount(CounterType.FADE)).isEqualTo(1);
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Jolting Merfolk can pay a fade counter immediately to tap itself")
    void activatesWhileTappedAndSummoningSick() {
        harness.castFromHand(player1, new JoltingMerfolk(), "{2}{U}{U}");
        harness.passBothPriorities();
        Permanent merfolk = findPermanent(player1, "Jolting Merfolk");
        merfolk.tap();

        harness.activateAbility(player1, 0, null, merfolk.getId());

        assertThat(merfolk.getCounterCount(CounterType.FADE)).isEqualTo(3);
        harness.passBothPriorities();
        assertThat(merfolk.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Jolting Merfolk");
    }

    @Test
    @DisplayName("Spending the last fade counter in response to fading taps the target before sacrificing the source")
    void spendsLastCounterInResponseToFading() {
        Permanent merfolk = addCreatureReady(player1, new JoltingMerfolk());
        merfolk.setCounterCount(CounterType.FADE, 1);
        Permanent target = addCreatureReady(player2, new RootwaterCommando());
        advanceToUpkeep(player1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(merfolk.getCounterCount(CounterType.FADE)).isZero();
        assertThat(target.isTapped()).isFalse();
        harness.passBothPriorities();
        assertThat(target.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Jolting Merfolk");
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Jolting Merfolk");
        harness.assertInGraveyard(player1, "Jolting Merfolk");
    }

    @Test
    @DisplayName("Each activation pays a separate fade counter and can tap a friendly creature")
    void activatesMultipleTimesBeforeResolution() {
        Permanent merfolk = addCreatureReady(player1, new JoltingMerfolk());
        merfolk.setCounterCount(CounterType.FADE, 2);
        Permanent friendlyTarget = addCreatureReady(player1, new RootwaterCommando());
        Permanent opposingTarget = addCreatureReady(player2, new RootwaterCommando());

        harness.activateAbility(player1, 0, null, opposingTarget.getId());
        harness.activateAbility(player1, 0, null, friendlyTarget.getId());

        assertThat(merfolk.getCounterCount(CounterType.FADE)).isZero();
        assertThat(friendlyTarget.isTapped()).isFalse();
        assertThat(opposingTarget.isTapped()).isFalse();
        harness.passBothPriorities();
        assertThat(friendlyTarget.isTapped()).isTrue();
        assertThat(opposingTarget.isTapped()).isFalse();
        harness.passBothPriorities();
        assertThat(opposingTarget.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Jolting Merfolk");
    }
}
