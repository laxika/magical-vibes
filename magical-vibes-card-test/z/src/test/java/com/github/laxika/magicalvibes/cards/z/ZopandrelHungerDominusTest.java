package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.p.PredationSteward;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZopandrelHungerDominus.class, PredationSteward.class})
class ZopandrelHungerDominusTest extends BaseCardTest {

    private void advanceToCombatAndResolve(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Doubles the power and toughness of your creatures, but not an opponent's")
    void doublesOwnCreaturesOnly() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new PredationSteward());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new PredationSteward());
        harness.addToBattlefield(player1, new ZopandrelHungerDominus());

        advanceToCombatAndResolve(player1);

        assertThat(gqs.getEffectivePower(gd, own)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, own)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(2);
    }

    @Test
    @DisplayName("The combat boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new PredationSteward());
        harness.addToBattlefield(player1, new ZopandrelHungerDominus());

        advanceToCombatAndResolve(player1);
        assertThat(gqs.getEffectivePower(gd, own)).isEqualTo(4);

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, own)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, own)).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrificing two other creatures adds an indestructible counter")
    void sacrificesTwoOtherCreaturesForIndestructibleCounter() {
        Permanent zopandrel = harness.addToBattlefieldAndReturn(player1, new ZopandrelHungerDominus());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new PredationSteward());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new PredationSteward());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, first.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(first, second);
        assertThat(zopandrel.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot sacrifice Zopandrel itself")
    void cannotSacrificeSource() {
        harness.addToBattlefield(player1, new ZopandrelHungerDominus());
        harness.addToBattlefield(player1, new PredationSteward());
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }

    @Test
    void doublesCreaturesIncludingItselfDuringOpponentsCombat() {
        Permanent zopandrel = harness.addToBattlefieldAndReturn(player1, new ZopandrelHungerDominus());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new PredationSteward());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new PredationSteward());

        advanceToCombatAndResolve(player2);

        assertThat(gqs.getEffectivePower(gd, zopandrel)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, zopandrel)).isEqualTo(12);
        assertThat(gqs.getEffectivePower(gd, own)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, own)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(2);
    }

    @Test
    void doublesAgainInAnotherCombatBeforeCleanup() {
        Permanent zopandrel = harness.addToBattlefieldAndReturn(player1, new ZopandrelHungerDominus());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new PredationSteward());

        advanceToCombatAndResolve(player1);
        advanceToCombatAndResolve(player1);

        assertThat(gqs.getEffectivePower(gd, own)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, own)).isEqualTo(8);
        assertThat(gqs.getEffectivePower(gd, zopandrel)).isEqualTo(16);
        assertThat(gqs.getEffectiveToughness(gd, zopandrel)).isEqualTo(24);
    }

    @Test
    void doublesExistingCountersButDoesNotDoubleCountersAddedLater() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new PredationSteward());
        own.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addToBattlefield(player1, new ZopandrelHungerDominus());

        advanceToCombatAndResolve(player1);

        assertThat(gqs.getEffectivePower(gd, own)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, own)).isEqualTo(6);
        own.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        assertThat(gqs.getEffectivePower(gd, own)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, own)).isEqualTo(7);
    }

    @Test
    void doublesNegativePowerRatherThanClampingItToZero() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new PredationSteward());
        own.setPowerModifier(-4);
        harness.addToBattlefield(player1, new ZopandrelHungerDominus());

        advanceToCombatAndResolve(player1);

        assertThat(gqs.getEffectivePower(gd, own)).isEqualTo(-4);
        assertThat(gqs.getEffectiveToughness(gd, own)).isEqualTo(4);
    }

    @Test
    void affectsCreaturesPresentAtResolutionButNotCreaturesEnteringLater() {
        harness.addToBattlefield(player1, new ZopandrelHungerDominus());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.stack).hasSize(1);
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new PredationSteward());

        resolveAllTriggers();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new PredationSteward());

        assertThat(gqs.getEffectivePower(gd, beforeResolution)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, beforeResolution)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, afterResolution)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, afterResolution)).isEqualTo(2);
    }

    @Test
    void paysBothPhyrexianSymbolsWithLife() {
        Permanent zopandrel = harness.addToBattlefieldAndReturn(player1, new ZopandrelHungerDominus());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new PredationSteward());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new PredationSteward());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, first.getId());

        harness.assertLife(player1, 16);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(first, second);
        resolveAllTriggers();
        assertThat(zopandrel.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
    }

    @Test
    void paysOnePhyrexianSymbolWithManaAndOneWithLife() {
        Permanent zopandrel = harness.addToBattlefieldAndReturn(player1, new ZopandrelHungerDominus());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new PredationSteward());
        harness.addToBattlefield(player1, new PredationSteward());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, first.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        assertThat(zopandrel.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
    }

    @Test
    void cannotUseOpponentsCreaturesToPaySacrificeCost() {
        harness.addToBattlefield(player1, new ZopandrelHungerDominus());
        harness.addToBattlefield(player1, new PredationSteward());
        harness.addToBattlefield(player2, new PredationSteward());
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }
}
