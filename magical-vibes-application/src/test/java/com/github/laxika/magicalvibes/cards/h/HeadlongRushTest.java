package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HeadlongRushTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creatures controlled by either player gain first strike")
    void grantsFirstStrikeToAllAttackingCreatures() {
        Permanent ownAttacker = addCreatureReady(player1, new GrizzlyBears());
        ownAttacker.setAttacking(true);
        Permanent opponentAttacker = addCreatureReady(player2, new GrizzlyBears());
        opponentAttacker.setAttacking(true);
        Permanent ownNonAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentNonAttacker = addCreatureReady(player2, new GrizzlyBears());

        castHeadlongRush();

        assertThat(ownAttacker.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(opponentAttacker.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(ownNonAttacker.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        assertThat(opponentNonAttacker.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("First strike granted by Headlong Rush wears off at end of turn")
    void firstStrikeWearsOffAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        castHeadlongRush();

        assertThat(attacker.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    private void castHeadlongRush() {
        harness.setHand(player1, List.of(new HeadlongRush()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.castAndResolveInstant(player1, 0);
    }
}
