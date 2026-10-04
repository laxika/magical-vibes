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

    @Test
    @DisplayName("Granted first strike persists after a creature stops attacking")
    void firstStrikePersistsAfterLeavingCombat() {
        Permanent attacker = addCreatureReady(player1, new GorillaWarrior());
        attacker.setAttacking(true);

        castHeadlongRush();
        attacker.setAttacking(false);

        assertThat(attacker.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Only creatures attacking when Headlong Rush resolves gain first strike")
    void checksAttackingStatusAtResolution() {
        Permanent removedAttacker = addCreatureReady(player1, new GorillaWarrior());
        removedAttacker.setAttacking(true);
        Permanent newAttacker = addCreatureReady(player1, new GorillaWarrior());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.castFromHand(player1, new HeadlongRush(), "{1}{R}");

        removedAttacker.setAttacking(false);
        newAttacker.setAttacking(true);
        harness.passBothPriorities();

        assertThat(removedAttacker.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        assertThat(newAttacker.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Headlong Rush resolves with no attackers and does not affect later attackers")
    void resolvesWithoutAttackers() {
        Permanent creature = addCreatureReady(player1, new GorillaWarrior());

        castHeadlongRush();
        creature.setAttacking(true);

        assertThat(creature.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Headlong Rush");
    }

    private void castHeadlongRush() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.castFromHand(player1, new HeadlongRush(), "{1}{R}");
        harness.passBothPriorities();
    }
}
