package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GorillaWarrior;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeadlongRush.class, GorillaWarrior.class})
class HeadlongRushTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creatures controlled by either player gain first strike")
    void grantsFirstStrikeToAllAttackingCreatures() {
        Permanent ownAttacker = addCreatureReady(player1, new GorillaWarrior());
        ownAttacker.setAttacking(true);
        Permanent opponentAttacker = addCreatureReady(player2, new GorillaWarrior());
        opponentAttacker.setAttacking(true);
        Permanent ownNonAttacker = addCreatureReady(player1, new GorillaWarrior());
        Permanent opponentNonAttacker = addCreatureReady(player2, new GorillaWarrior());

        castHeadlongRush();

        assertThat(ownAttacker.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(opponentAttacker.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(ownNonAttacker.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        assertThat(opponentNonAttacker.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();

        ownNonAttacker.setAttacking(true);
        opponentNonAttacker.setAttacking(true);

        assertThat(ownNonAttacker.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        assertThat(opponentNonAttacker.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("First strike granted by Headlong Rush wears off at end of turn")
    void firstStrikeWearsOffAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new GorillaWarrior());
        attacker.setAttacking(true);

        castHeadlongRush();

        assertThat(attacker.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    private void castHeadlongRush() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.castFromHand(player1, new HeadlongRush(), "{1}{R}");
        harness.passBothPriorities();
    }
}
