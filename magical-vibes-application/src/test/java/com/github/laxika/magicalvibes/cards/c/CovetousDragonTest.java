package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BraidwoodCup;
import com.github.laxika.magicalvibes.cards.y.YgraEaterOfAll;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CovetousDragon.class, BraidwoodCup.class, YgraEaterOfAll.class})
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
        harness.setHand(player1, List.of(new CovetousDragon()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castCreature(player1, 0);
    }
}
