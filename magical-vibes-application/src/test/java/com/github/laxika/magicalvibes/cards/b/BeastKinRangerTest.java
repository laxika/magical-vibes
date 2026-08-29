package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BeastKinRangerTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 until end of turn when another creature you control enters")
    void getsBoostWhenAllyCreatureEnters() {
        Permanent ranger = harness.addToBattlefieldAndReturn(player1, new BeastKinRanger());

        castGrizzlyBears(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ranger)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ranger)).isEqualTo(3);
    }

    @Test
    @DisplayName("Boosts cumulatively for multiple creatures entering")
    void boostStacksForMultipleCreatures() {
        Permanent ranger = harness.addToBattlefieldAndReturn(player1, new BeastKinRanger());

        castGrizzlyBears(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        castGrizzlyBears(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ranger)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ranger)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not trigger for an opponent's creature")
    void noTriggerForOpponentCreature() {
        Permanent ranger = harness.addToBattlefieldAndReturn(player1, new BeastKinRanger());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FugitiveWizard()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ranger)).isEqualTo(3);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent ranger = harness.addToBattlefieldAndReturn(player1, new BeastKinRanger());

        castGrizzlyBears(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ranger)).isEqualTo(3);
    }

    private void castGrizzlyBears(Player player) {
        harness.setHand(player, List.of(new GrizzlyBears()));
        harness.addMana(player, ManaColor.GREEN, 2);
        harness.castCreature(player, 0);
    }
}
