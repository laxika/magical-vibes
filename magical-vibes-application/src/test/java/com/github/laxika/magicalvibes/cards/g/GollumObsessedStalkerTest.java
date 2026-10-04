package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GollumObsessedStalker.class})
class GollumObsessedStalkerTest extends BaseCardTest {

    @Test
    @DisplayName("At your end step, opponents dealt combat damage by Gollum lose the life you gained")
    void opponentsDealtCombatDamageByGollumLoseLifeGainedThisTurn() {
        Permanent gollum = addCreatureReady(player1, new GollumObsessedStalker());
        gollum.setAttacking(true);
        harness.setLife(player2, 20);
        gd.lifeGainedThisTurn.put(player1.getId(), 4);

        resolveCombat();
        advanceToEndStepAndResolve(player1);

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Does nothing when no opponent was dealt combat damage by Gollum")
    void doesNothingWithoutCombatDamageByGollum() {
        addCreatureReady(player1, new GollumObsessedStalker());
        harness.setLife(player2, 20);
        gd.lifeGainedThisTurn.put(player1.getId(), 3);

        advanceToEndStepAndResolve(player1);

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Combat damage history is retained after the turn changes")
    void combatDamageHistoryIsNotLimitedToThisTurn() {
        Permanent gollum = addCreatureReady(player1, new GollumObsessedStalker());
        gollum.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();
        gd.lifeGainedThisTurn.put(player1.getId(), 2);
        advanceToEndStepAndResolve(player1);

        harness.forceStep(TurnStep.UNTAP);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gd.combatDamageToPlayersThisTurn.clear();
        gd.lifeGainedThisTurn.clear();
        gd.lifeGainedThisTurn.put(player1.getId(), 5);
        advanceToEndStepAndResolve(player1);

        assertThat(gd.getLife(player2.getId())).isEqualTo(12);
    }

    @Test
    void skulkRejectsGreaterPowerBlocker() {
        Permanent attacker = addCreatureReady(player1, new GollumObsessedStalker());
        Permanent blocker = addCreatureReady(player2, new GollumObsessedStalker());
        blocker.setPowerModifier(1);
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("skulk");
    }

    @Test
    void skulkAllowsEqualPowerBlockerAndCreatureDamageDoesNotMarkOpponent() {
        Permanent attacker = addCreatureReady(player1, new GollumObsessedStalker());
        addCreatureReady(player2, new GollumObsessedStalker());
        harness.setLife(player2, 20);
        attacker.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        addCreatureReady(player1, new GollumObsessedStalker());
        harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3);

        advanceToEndStepAndResolve(player1);

        harness.assertLife(player2, 20);
    }

    @Test
    void newGollumUsesCombatDamageDealtByPreviousGollum() {
        Permanent original = addCreatureReady(player1, new GollumObsessedStalker());
        original.setAttacking(true);
        harness.setLife(player2, 20);
        resolveCombat();
        gd.playerBattlefields.get(player1.getId()).remove(original);
        gd.playerGraveyards.get(player1.getId()).add(original.getCard());
        addCreatureReady(player1, new GollumObsessedStalker());
        harness.getLifeSupport().applyGainLife(gd, player1.getId(), 4);

        advanceToEndStepAndResolve(player1);

        harness.assertLife(player2, 15);
    }

    @Test
    void lifeLossDoesNotSubtractFromTotalLifeGained() {
        Permanent gollum = addCreatureReady(player1, new GollumObsessedStalker());
        gollum.setAttacking(true);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        resolveCombat();
        harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3);
        harness.getLifeSupport().applyLifeLoss(gd, player1.getId(), 3, "Test");
        harness.getLifeSupport().applyGainLife(gd, player1.getId(), 2);

        advanceToEndStepAndResolve(player1);

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 14);
    }

    @Test
    void countsLifeGainedAfterTriggerAndResolvesWithoutSource() {
        Permanent gollum = addCreatureReady(player1, new GollumObsessedStalker());
        gollum.setAttacking(true);
        harness.setLife(player2, 20);
        resolveCombat();
        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(gollum);
        gd.playerGraveyards.get(player1.getId()).add(gollum.getCard());
        harness.getLifeSupport().applyGainLife(gd, player1.getId(), 4);

        resolveAllTriggers();

        harness.assertLife(player2, 15);
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        Permanent gollum = addCreatureReady(player1, new GollumObsessedStalker());
        gollum.setAttacking(true);
        harness.setLife(player2, 20);
        resolveCombat();
        harness.getLifeSupport().applyGainLife(gd, player1.getId(), 4);

        advanceToEndStepAndResolve(player2);

        harness.assertLife(player2, 19);
    }

    @Test
    void opponentLifeGainDoesNotCountForController() {
        Permanent gollum = addCreatureReady(player1, new GollumObsessedStalker());
        gollum.setAttacking(true);
        harness.setLife(player2, 20);
        resolveCombat();
        harness.getLifeSupport().applyGainLife(gd, player2.getId(), 4);

        advanceToEndStepAndResolve(player1);

        harness.assertLife(player2, 23);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }

    private void advanceToEndStepAndResolve(Player activePlayer) {
        advanceToEndStep(activePlayer);
        resolveAllTriggers();
    }
}
