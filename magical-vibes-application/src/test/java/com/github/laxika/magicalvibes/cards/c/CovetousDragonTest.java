package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BraidwoodCup;
import com.github.laxika.magicalvibes.cards.e.Extruder;
import com.github.laxika.magicalvibes.cards.y.YgraEaterOfAll;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CovetousDragon.class, BraidwoodCup.class, Extruder.class, YgraEaterOfAll.class})
class CovetousDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself when its controller controls no artifacts")
    void sacrificesWhenNoArtifacts() {
        castDragon();

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Covetous Dragon");
        harness.assertInGraveyard(player1, "Covetous Dragon");
    }

    @Test
    @DisplayName("Survives while its controller controls an artifact")
    void survivesWithArtifact() {
        harness.addToBattlefield(player1, new BraidwoodCup());
        castDragon();

        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Covetous Dragon");
        harness.assertOnBattlefield(player1, "Braidwood Cup");
    }

    @Test
    @DisplayName("An opponent's artifact does not satisfy the condition")
    void opponentArtifactDoesNotCount() {
        harness.addToBattlefield(player2, new BraidwoodCup());
        castDragon();

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Covetous Dragon");
        harness.assertInGraveyard(player1, "Covetous Dragon");
        harness.assertOnBattlefield(player2, "Braidwood Cup");
    }

    @Test
    @DisplayName("Counts an artifact type granted by a continuous effect")
    void countsEffectivelyArtifactPermanents() {
        harness.addToBattlefield(player1, new YgraEaterOfAll());
        castDragon();

        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Covetous Dragon");
        harness.assertOnBattlefield(player1, "Ygra, Eater of All");
    }

    private void castDragon() {
        harness.castFromHand(player1, new CovetousDragon(), "{4}{R}");
    }

    @Test
    @DisplayName("An artifact entering after the trigger does not prevent sacrifice")
    void gainingArtifactDoesNotCancelPendingSacrifice() {
        castDragon();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.assertOnBattlefield(player1, "Covetous Dragon");

        harness.enterBattlefieldAndReturn(player1, new BraidwoodCup());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Covetous Dragon");
        harness.assertNotOnBattlefield(player1, "Covetous Dragon");
        harness.assertOnBattlefield(player1, "Braidwood Cup");
    }

    @Test
    @DisplayName("Repeated state checks do not duplicate a pending sacrifice trigger")
    void onlyOneTriggerWhileAbilityIsOnStack() {
        castDragon();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.runStateBasedActions();
        harness.runStateBasedActions();

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        harness.assertInGraveyard(player1, "Covetous Dragon");
    }

    @Test
    @DisplayName("Sacrificing the last artifact triggers sacrifice after the Dragon has survived entry")
    void sacrificingLastArtifactTriggersSacrifice() {
        harness.addToBattlefield(player1, new Extruder());
        castDragon();
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Covetous Dragon");
        assertThat(gd.stack).isEmpty();

        harness.activateAbility(player1, 0, 0, null,
                findPermanent(player1, "Covetous Dragon").getId());
        harness.assertInGraveyard(player1, "Extruder");
        harness.assertOnBattlefield(player1, "Covetous Dragon");
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Covetous Dragon");
        harness.assertInGraveyard(player1, "Covetous Dragon");
    }
}
