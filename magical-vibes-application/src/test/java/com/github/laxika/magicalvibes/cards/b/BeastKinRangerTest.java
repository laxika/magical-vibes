package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BeastKinRanger.class, GrizzlyBears.class, FugitiveWizard.class})
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
        harness.castFromHand(player2, new FugitiveWizard(), "{U}");
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
        harness.castFromHand(player, new GrizzlyBears(), "{1}{G}");
    }

    @Test
    @DisplayName("Does not trigger for its own entry")
    void doesNotBoostItselfOnEntry() {
        harness.castFromHand(player1, new BeastKinRanger(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent ranger = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, ranger)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ranger)).isEqualTo(3);
    }

    @Test
    @DisplayName("The boost waits for the triggered ability to resolve")
    void boostUsesTheStack() {
        Permanent ranger = harness.addToBattlefieldAndReturn(player1, new BeastKinRanger());

        castGrizzlyBears(player1);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, ranger)).isEqualTo(3);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, ranger)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ranger)).isEqualTo(3);
    }

    @Test
    @DisplayName("Each Ranger boosts itself when another Ranger enters")
    void multipleRangersKeepTheirBoostsSeparate() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BeastKinRanger());
        Permanent second = harness.enterBattlefieldAndReturn(player1, new BeastKinRanger());

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);

        castGrizzlyBears(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
    }
}
