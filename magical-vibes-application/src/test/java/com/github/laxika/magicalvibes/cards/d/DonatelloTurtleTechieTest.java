package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HardWonJitte;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DonatelloTurtleTechie.class, HardWonJitte.class, Forest.class})
class DonatelloTurtleTechieTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card on ETB when controlling an artifact")
    void drawsWithArtifact() {
        harness.addToBattlefield(player1, new HardWonJitte());
        prepareDeckAndHand();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Donatello, Turtle Techie");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Does not draw on ETB without an artifact")
    void noDrawWithoutArtifact() {
        prepareDeckAndHand();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Donatello, Turtle Techie");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Opponent's artifact does not satisfy the condition")
    void opponentArtifactDoesNotCount() {
        harness.addToBattlefield(player2, new HardWonJitte());
        prepareDeckAndHand();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger if the first artifact arrives after Donatello enters")
    void artifactArrivingAfterEntryDoesNotEnableTrigger() {
        prepareDeckAndHand();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.addToBattlefield(player1, new HardWonJitte());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not draw if the only artifact leaves before the trigger resolves")
    void rechecksArtifactAtResolution() {
        var artifact = harness.addToBattlefieldAndReturn(player1, new HardWonJitte());
        prepareDeckAndHand();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(artifact);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Donatello, Turtle Techie");
    }

    @Test
    @DisplayName("Multiple artifacts still cause only one card to be drawn")
    void drawsExactlyOneCardWithMultipleArtifacts() {
        harness.addToBattlefield(player1, new HardWonJitte());
        harness.addToBattlefield(player1, new HardWonJitte());
        prepareDeckAndHand();
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private void prepareDeckAndHand() {
        harness.setHand(player1, List.of(new DonatelloTurtleTechie()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);
    }
}
