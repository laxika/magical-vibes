package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Humility;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DefilingDaemogoth.class, GrizzlyBears.class, Humility.class, SwordsToPlowshares.class})
class DefilingDaemogothTest extends BaseCardTest {

    @Test
    @DisplayName("Gains one life for each creature that deals combat damage, then drains opponents by that amount")
    void gainsLifeAndDrainsOpponentsAtEndStep() {
        harness.addToBattlefield(player1, new DefilingDaemogoth());
        addReadyAttacker(new GrizzlyBears());
        addReadyAttacker(new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);

        resolveEndStepTrigger();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    private void addReadyAttacker(com.github.laxika.magicalvibes.model.Card card) {
        var attacker = addCreatureReady(player1, card);
        attacker.setAttacking(true);
    }

    @Test
    void gainsLifeWhenDaemogothItselfDealsCombatDamage() {
        addReadyAttacker(new DefilingDaemogoth());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 15);
        resolveEndStepTrigger();
        harness.assertLife(player2, 14);
    }

    @Test
    void doesNotDrainWhenNoLifeWasGained() {
        harness.addToBattlefield(player1, new DefilingDaemogoth());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveEndStepTrigger();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void countsLifeGainedBeforeEnteringBattlefield() {
        var creature = harness.addToBattlefieldAndReturn(player1, new DefilingDaemogoth());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SwordsToPlowshares()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.addToBattlefield(player1, new DefilingDaemogoth());

        resolveEndStepTrigger();

        harness.assertLife(player1, 25);
        harness.assertLife(player2, 15);
    }

    @Test
    void endStepAmountIncludesLifeGainedInResponseAndResolvesAfterSourceLeaves() {
        var daemon = harness.addToBattlefieldAndReturn(player1, new DefilingDaemogoth());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of(new SwordsToPlowshares()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, daemon.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 25);
        harness.assertLife(player2, 15);
        harness.assertNotOnBattlefield(player1, "Defiling Daemogoth");
    }

    @Test
    void doesNotTriggerAtOpponentsEndStep() {
        harness.addToBattlefield(player1, new DefilingDaemogoth());
        addReadyAttacker(new DefilingDaemogoth());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        resolveCombat();
        resolveAllTriggers();
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 15);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertLife(player2, 15);
    }

    @Test
    void opponentsCombatDamageDoesNotTriggerControllersLifeGain() {
        harness.addToBattlefield(player1, new DefilingDaemogoth());
        var attacker = addCreatureReady(player2, new DefilingDaemogoth());
        attacker.setAttacking(true);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveCombat(player2);
        resolveAllTriggers();

        harness.assertLife(player1, 15);
        harness.assertLife(player2, 21);
    }

    @Test
    void lifeLossDoesNotReduceTheAmountGainedThisTurn() {
        var creature = harness.addToBattlefieldAndReturn(player1, new DefilingDaemogoth());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SwordsToPlowshares()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.addToBattlefield(player1, new DefilingDaemogoth());
        var attacker = addCreatureReady(player2, new DefilingDaemogoth());
        attacker.setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 21);
        resolveEndStepTrigger();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 16);
    }

    @Test
    void doesNotGainLifeWhileHumilityRemovesItsAbilities() {
        harness.addToBattlefield(player1, new DefilingDaemogoth());
        harness.addToBattlefield(player1, new Humility());
        addReadyAttacker(new DefilingDaemogoth());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    private void resolveEndStepTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
