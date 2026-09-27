package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KarlachFuryOfAvernus.class, GrizzlyBears.class})
class KarlachFuryOfAvernusTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking in the first combat phase untaps attackers, grants first strike, and adds a combat phase")
    void firstCombatAttackUntapsGrantsFirstStrikeAndAddsCombat() {
        Permanent karlach = addCreatureReady(player1, new KarlachFuryOfAvernus());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1), 1);
        assertThat(karlach.isTapped()).isTrue();
        assertThat(bear.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(karlach.isTapped()).isFalse();
        assertThat(bear.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, karlach, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);
    }

    @Test
    @DisplayName("Attacking in a later combat phase does not trigger Karlach")
    void laterCombatAttackDoesNothing() {
        Permanent karlach = addCreatureReady(player1, new KarlachFuryOfAvernus());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1), 2);
        harness.passBothPriorities();

        assertThat(karlach.isTapped()).isTrue();
        assertThat(bear.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, karlach, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gd.currentStep).isNotEqualTo(TurnStep.DECLARE_ATTACKERS);
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices, int combatPhaseNumber) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        gd.combatPhasesThisTurn = combatPhaseNumber;
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices);
    }
}
