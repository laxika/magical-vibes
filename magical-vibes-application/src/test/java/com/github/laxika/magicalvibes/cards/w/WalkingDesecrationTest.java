package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WalkingDesecration.class, AvianChangeling.class, GrizzlyBears.class, HillGiant.class})
class WalkingDesecrationTest extends BaseCardTest {

    @Test
    @DisplayName("Forces every creature of the chosen type on both sides to attack")
    void forcesChosenTypeToAttack() {
        addCreatureReady(player1, new WalkingDesecration());
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBear = addCreatureReady(player2, new GrizzlyBears());
        Permanent giant = addCreatureReady(player1, new HillGiant());

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BEAR");

        assertThat(ownBear.isMustAttackThisTurn()).isTrue();
        assertThat(opponentBear.isMustAttackThisTurn()).isTrue();
        assertThat(giant.isMustAttackThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Counts a Changeling as a creature of the chosen type")
    void changelingMatchesChosenType() {
        addCreatureReady(player1, new WalkingDesecration());
        Permanent changeling = addCreatureReady(player2, new AvianChangeling());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GOBLIN");

        assertThat(changeling.isMustAttackThisTurn()).isTrue();
        assertThat(bear.isMustAttackThisTurn()).isFalse();
    }

    @Test
    @DisplayName("The must-attack requirement wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        addCreatureReady(player1, new WalkingDesecration());
        Permanent opponentBear = addCreatureReady(player2, new GrizzlyBears());

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BEAR");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(opponentBear.isMustAttackThisTurn()).isFalse();
    }
}
