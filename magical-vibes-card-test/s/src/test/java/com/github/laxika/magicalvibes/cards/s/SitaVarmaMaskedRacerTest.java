package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SitaVarmaMaskedRacer.class, GrizzlyBears.class})
@DisplayName("Sita Varma, Masked Racer")
class SitaVarmaMaskedRacerTest extends BaseCardTest {

    @Test
    @DisplayName("Exhaust puts X counters on Sita and optionally sets other own creatures to her power")
    void exhaustPutsCountersAndSetsOtherCreaturesBasePowerToughness() {
        Permanent sita = addCreatureReady(player1, new SitaVarmaMaskedRacer());
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addExhaustMana();

        harness.activateAbility(player1, 0, 0, 2, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(sita.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, sita)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, sita)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining the optional effect leaves other creatures unchanged")
    void mayEffectCanBeDeclined() {
        Permanent sita = addCreatureReady(player1, new SitaVarmaMaskedRacer());
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        addManaForX(1);

        harness.activateAbility(player1, 0, 0, 1, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(sita.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, sita)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("The base P/T set wears off at end of turn, but counters remain")
    void basePowerToughnessSetWearsOffAtEndOfTurn() {
        Permanent sita = addCreatureReady(player1, new SitaVarmaMaskedRacer());
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        addManaForX(1);

        harness.activateAbility(player1, 0, 0, 1, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(2);
        assertThat(sita.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Exhaust can be activated only once")
    void exhaustCanBeActivatedOnlyOnce() {
        addCreatureReady(player1, new SitaVarmaMaskedRacer());
        addExhaustMana();
        harness.activateAbility(player1, 0, 0, 2, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 1, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    @DisplayName("Sita's last known power is used if she leaves before exhaust resolves")
    void usesLastKnownPowerWhenSourceLeaves() {
        Permanent sita = addCreatureReady(player1, new SitaVarmaMaskedRacer());
        sita.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addManaForX(2);

        harness.activateAbility(player1, 0, 0, 2, null);
        harness.getPermanentRemovalService().removePermanentToHand(gd, sita);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(6);
    }

    @Test
    @DisplayName("Negative Sita power sets a negative base value rather than zero")
    void negativePowerIsPreserved() {
        Permanent sita = addCreatureReady(player1, new SitaVarmaMaskedRacer());
        sita.setPowerModifier(-3);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        addManaForX(0);

        harness.activateAbility(player1, 0, 0, 0, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
    }

    @Test
    @DisplayName("The effect fixes existing creatures' base stats when it resolves")
    void resolutionLocksPowerAndAffectedCreatures() {
        Permanent sita = addCreatureReady(player1, new SitaVarmaMaskedRacer());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addManaForX(1);

        harness.activateAbility(player1, 0, 0, 1, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        sita.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        Permanent laterBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, laterBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, laterBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exhaust does not require Sita to be untapped or free of summoning sickness")
    void exhaustWorksWhileTappedAndSummoningSick() {
        Permanent sita = harness.addToBattlefieldAndReturn(player1, new SitaVarmaMaskedRacer());
        sita.setSummoningSick(true);
        sita.tap();
        addManaForX(1);

        harness.activateAbility(player1, 0, 0, 1, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(sita.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(sita.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A returned Sita is a new object that may exhaust again")
    void newPermanentCanExhaustAgain() {
        Permanent sita = addCreatureReady(player1, new SitaVarmaMaskedRacer());
        addManaForX(0);
        harness.activateAbility(player1, 0, 0, 0, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.getPermanentRemovalService().removePermanentToHand(gd, sita);
        gd.playerHands.get(player1.getId()).remove(sita.getCard());
        Permanent returnedSita = harness.addToBattlefieldAndReturn(player1, sita.getCard());
        addManaForX(1);

        harness.activateAbility(player1, 0, 0, 1, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(returnedSita.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void addExhaustMana() {
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }

    private void addManaForX(int x) {
        harness.addMana(player1, ManaColor.GREEN, x + 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}
