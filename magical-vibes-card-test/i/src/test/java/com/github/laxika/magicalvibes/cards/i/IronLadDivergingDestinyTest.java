package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IronLadDivergingDestiny.class, Forest.class})
class IronLadDivergingDestinyTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability draws the revealed artifact card")
    void drawsRevealedArtifactCard() {
        Permanent ironLad = addReadyIronLad();
        IronLadDivergingDestiny artifact = new IronLadDivergingDestiny();
        Forest nextCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(artifact, nextCard));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
        assertThat(ironLad.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tap ability does not draw when the revealed card is not an artifact")
    void doesNotDrawRevealedNonartifactCard() {
        Permanent ironLad = addReadyIronLad();
        Forest nonartifact = new Forest();
        IronLadDivergingDestiny nextCard = new IronLadDivergingDestiny();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(nonartifact, nextCard));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonartifact, nextCard);
        assertThat(ironLad.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Only the controller sees the top card, and visibility ends when Iron Lad leaves")
    void privateLookEndsWhenSourceLeaves() {
        Permanent ironLad = addReadyIronLad();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{"));
        assertThat(harness.getConn2().getSentMessages())
                .allMatch(message -> !message.contains("\"revealedLibraryTopCards\":[[{"));

        gd.playerBattlefields.get(player1.getId()).remove(ironLad);
        harness.clearMessages();
        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[],[]]"));
    }

    @Test
    @DisplayName("An empty library does not cause a draw or a loss")
    void emptyLibraryDoesNotDraw() {
        Permanent ironLad = addReadyIronLad();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        assertThat(ironLad.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability checks the top card on resolution even after its source leaves")
    void checksCurrentTopCardAfterSourceLeaves() {
        Permanent ironLad = addReadyIronLad();
        IronLadDivergingDestiny artifact = new IronLadDivergingDestiny();
        Forest nextCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(ironLad);
        harness.setLibrary(player1, List.of(artifact, nextCard));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
    }

    private Permanent addReadyIronLad() {
        return addCreatureReady(player1, new IronLadDivergingDestiny());
    }

    @Test
    @DisplayName("The ability publicly reveals a nonartifact without moving it")
    void revealsNonartifactToBothPlayers() {
        addReadyIronLad();
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.clearMessages();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gameLogContains("reveals Forest from the top of their library.")).isTrue();
        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("reveals ") && message.contains("Forest"));
        assertThat(harness.getConn2().getSentMessages())
                .anyMatch(message -> message.contains("reveals ") && message.contains("Forest"));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }
}
