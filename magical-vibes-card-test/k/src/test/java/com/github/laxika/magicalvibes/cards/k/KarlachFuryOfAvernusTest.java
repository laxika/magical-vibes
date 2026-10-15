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

    @Test
    @DisplayName("Karlach need not attack, and nonattackers do not untap or gain first strike")
    void onlyAttackersBenefitWhenKarlachStaysBack() {
        Permanent karlach = addCreatureReady(player1, new KarlachFuryOfAvernus());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonattacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());
        nonattacker.tap();
        opponent.tap();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(1), 1);
            assertThat(gd.stack).hasSize(1);
            harness.passBothPriorities();
        });

        assertThat(attacker.isTapped()).isFalse();
        assertThat(attacker.isAttacking()).isTrue();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, karlach, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(nonattacker.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, nonattacker, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(opponent.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gd.additionalCombatPhasesOnly).isEqualTo(1);
        assertThat(gd.combatPhasesThisTurn).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponents attacking do not trigger Karlach")
    void opponentAttacksDoNotTrigger() {
        addCreatureReady(player1, new KarlachFuryOfAvernus());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player2, List.of(0), 1));

        assertThat(gd.stack).isEmpty();
        assertThat(attacker.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gd.additionalCombatPhasesOnly).isZero();
    }

    @Test
    @DisplayName("The second combat does not untap attackers again, and first strike expires at end of turn")
    void secondCombatRetainsFirstStrikeWithoutAnotherUntap() {
        Permanent karlach = addCreatureReady(player1, new KarlachFuryOfAvernus());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        declareAttackers(player1, List.of(0, 1), 1);
        harness.passBothPriorities();
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0, 1)));

        assertThat(gd.stack).isEmpty();
        assertThat(karlach.isTapped()).isTrue();
        assertThat(bear.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, karlach, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gd.additionalCombatPhasesOnly).isZero();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, karlach, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FIRST_STRIKE)).isFalse();
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices, int combatPhaseNumber) {
        gd.combatPhasesThisTurn = combatPhaseNumber;
        declareAttackers(player, attackerIndices);
    }
}
