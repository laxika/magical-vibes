package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GoblinDiplomatsTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping forces every creature, on both sides, to attack this turn")
    void forcesAllCreaturesToAttack() {
        Permanent diplomats = addCreatureReady(player1, new GoblinDiplomats());
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent enemyBear = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(diplomats.isTapped()).isTrue();
        assertThat(diplomats.isMustAttackThisTurn()).isTrue();
        assertThat(ownBear.isMustAttackThisTurn()).isTrue();
        assertThat(enemyBear.isMustAttackThisTurn()).isTrue();
    }

    @Test
    @DisplayName("The must-attack requirement wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        addCreatureReady(player1, new GoblinDiplomats());
        Permanent enemyBear = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(enemyBear.isMustAttackThisTurn()).isFalse();
    }
}
