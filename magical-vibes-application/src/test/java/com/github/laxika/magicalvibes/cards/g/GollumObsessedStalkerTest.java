package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(GollumObsessedStalker.class)
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
        Permanent gollum = addCreatureReady(player1, new GollumObsessedStalker());
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

    private void advanceToEndStepAndResolve(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
