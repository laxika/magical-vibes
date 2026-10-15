package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SaddledRimestag.class, GrizzlyBears.class, SnowCoveredForest.class})
class SaddledRimestagTest extends BaseCardTest {

    @Test
    @DisplayName("Another Rimestag entering earlier in the turn qualifies for both creatures")
    void earlierEntryOfAnotherRimestagQualifies() {
        Permanent first = harness.enterBattlefieldAndReturn(player1, new SaddledRimestag());
        Permanent second = harness.enterBattlefieldAndReturn(player1, new SaddledRimestag());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
    }

    @Test
    @DisplayName("A qualifying creature may leave before Rimestag enters")
    void earlierCreatureLeavingDoesNotEraseEntry() {
        Permanent earlier = harness.enterBattlefieldAndReturn(player1, new SaddledRimestag());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, earlier));
        harness.assertInGraveyard(player1, "Saddled Rimestag");
        Permanent rimestag = harness.enterBattlefieldAndReturn(player1, new SaddledRimestag());

        assertThat(gqs.getEffectivePower(gd, rimestag)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, rimestag)).isEqualTo(4);
    }

    @Test
    @DisplayName("Multiple qualifying entries still grant only +2/+2")
    void multipleEntriesDoNotIncreaseBoost() {
        Permanent rimestag = harness.enterBattlefieldAndReturn(player1, new SaddledRimestag());
        harness.enterBattlefieldAndReturn(player1, new SaddledRimestag());
        harness.enterBattlefieldAndReturn(player1, new SaddledRimestag());

        assertThat(gqs.getEffectivePower(gd, rimestag)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, rimestag)).isEqualTo(4);
    }

    @Test
    @DisplayName("A preexisting creature does not qualify without entering this turn")
    void preexistingCreatureDoesNotQualify() {
        harness.addToBattlefield(player1, new SaddledRimestag());
        Permanent rimestag = harness.enterBattlefieldAndReturn(player1, new SaddledRimestag());

        assertThat(gqs.getEffectivePower(gd, rimestag)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rimestag)).isEqualTo(2);
    }

    @Test
    @DisplayName("A noncreature entering does not qualify")
    void noncreatureEntryDoesNotQualify() {
        Permanent rimestag = harness.enterBattlefieldAndReturn(player1, new SaddledRimestag());
        harness.enterBattlefieldAndReturn(player1, new SnowCoveredForest());

        assertThat(gqs.getEffectivePower(gd, rimestag)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rimestag)).isEqualTo(2);
    }

    @Test
    @DisplayName("The condition can become true on a later opponent turn")
    void qualifiesOnLaterOpponentTurn() {
        Permanent rimestag = harness.enterBattlefieldAndReturn(player1, new SaddledRimestag());
        harness.forceStep(TurnStep.CLEANUP);
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gqs.getEffectivePower(gd, rimestag)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rimestag)).isEqualTo(2);

        harness.enterBattlefieldAndReturn(player1, new SaddledRimestag());

        assertThat(gqs.getEffectivePower(gd, rimestag)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, rimestag)).isEqualTo(4);
    }

    @Test
    @DisplayName("Gets +2/+2 after another creature enters under its controller's control")
    void getsBoostAfterAnotherCreatureEntersUnderItsControllersControl() {
        Permanent rimestag = harness.enterBattlefieldAndReturn(player1, new SaddledRimestag());

        assertThat(gqs.getEffectivePower(gd, rimestag)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rimestag)).isEqualTo(2);

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, rimestag)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, rimestag)).isEqualTo(4);
    }

    @Test
    @DisplayName("An opponent's creature entering does not grant the boost")
    void opponentCreatureEnteringDoesNotGrantBoost() {
        Permanent rimestag = harness.enterBattlefieldAndReturn(player1, new SaddledRimestag());
        harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, rimestag)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rimestag)).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost ends when the turn ends")
    void boostEndsWhenTurnEnds() {
        Permanent rimestag = harness.enterBattlefieldAndReturn(player1, new SaddledRimestag());
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, rimestag)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, rimestag)).isEqualTo(4);

        harness.forceStep(TurnStep.CLEANUP);
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.getEffectivePower(gd, rimestag)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rimestag)).isEqualTo(2);
    }
}
