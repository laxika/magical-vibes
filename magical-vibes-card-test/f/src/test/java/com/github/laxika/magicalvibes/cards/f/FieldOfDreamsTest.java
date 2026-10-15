package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FieldOfDreams.class, FlashCounter.class, ForceSpike.class, FutureSight.class})
class FieldOfDreamsTest extends BaseCardTest {

    @Test
    @DisplayName("Reveals both libraries only after the enchantment resolves")
    void revealsLibrariesOnlyAfterResolution() {
        harness.setLibrary(player1, List.of(new FlashCounter()));
        harness.setLibrary(player2, List.of(new ForceSpike()));
        harness.castFromHand(player1, new FieldOfDreams(), "{U}");
        harness.clearMessages();
        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[],[]]"));
        assertThat(harness.getConn2().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[],[]]"));

        harness.passBothPriorities();
        harness.clearMessages();
        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Flash Counter") && message.contains("Force Spike"));
        assertThat(harness.getConn2().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Flash Counter") && message.contains("Force Spike"));
    }

    @Test
    @DisplayName("Reveals the new top card after drawing and hides the drawn card from the opponent")
    void updatesRevealedTopAfterDraw() {
        harness.addToBattlefield(player2, new FieldOfDreams());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new FlashCounter(), new ForceSpike()));
        harness.setLibrary(player2, List.of());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.clearMessages();
        harness.publishState();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Force Spike"));
        assertThat(harness.getConn2().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Force Spike") && !message.contains("Flash Counter"));
    }

    @Test
    @DisplayName("Stops revealing both libraries after leaving the battlefield")
    void stopsRevealingAfterLeavingBattlefield() {
        harness.addToBattlefield(player1, new FieldOfDreams());
        harness.setLibrary(player1, List.of(new FlashCounter()));
        harness.setLibrary(player2, List.of(new ForceSpike()));
        harness.publishState();

        gd.playerBattlefields.get(player1.getId()).clear();
        harness.clearMessages();
        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[],[]]"));
        assertThat(harness.getConn2().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[],[]]"));
    }

    @Test
    @DisplayName("Reveals the opponent's top card even when an earlier permanent reveals the controller's top card")
    void stillRevealsOpponentWithEarlierControllerRevealEffect() {
        harness.addToBattlefield(player1, new FutureSight());
        harness.addToBattlefield(player1, new FieldOfDreams());
        harness.setLibrary(player1, List.of(new FlashCounter()));
        harness.setLibrary(player2, List.of(new ForceSpike()));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Flash Counter") && message.contains("Force Spike"));
        assertThat(harness.getConn2().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Flash Counter") && message.contains("Force Spike"));
    }

    @Test
    @DisplayName("Empty libraries have no card to reveal")
    void handlesEmptyLibraries() {
        harness.addToBattlefield(player1, new FieldOfDreams());
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[],[]]"));
        assertThat(harness.getConn2().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[],[]]"));
    }

    @Test
    @DisplayName("Reveals every player's top library card to both players")
    void revealsEveryPlayersTopLibraryCardToBothPlayers() {
        harness.addToBattlefield(player1, new FieldOfDreams());
        harness.setLibrary(player1, List.of(new FlashCounter()));
        harness.setLibrary(player2, List.of(new ForceSpike()));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Flash Counter")
                        && message.contains("Force Spike"));
        assertThat(harness.getConn2().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Flash Counter")
                        && message.contains("Force Spike"));
    }
}
