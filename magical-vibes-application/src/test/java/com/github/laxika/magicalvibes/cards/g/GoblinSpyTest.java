package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinSpy.class, Forest.class, Island.class})
class GoblinSpyTest extends BaseCardTest {

    @Test
    @DisplayName("Reveals the controller's top library card to both players")
    void revealsTopLibraryCardToBothPlayers() {
        harness.addToBattlefield(player1, new GoblinSpy());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Island()));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Forest"))
                .noneMatch(message -> message.contains("\"revealedLibraryTopCards\"")
                        && message.contains("Island"));
        assertThat(harness.getConn2().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Forest"))
                .noneMatch(message -> message.contains("\"revealedLibraryTopCards\"")
                        && message.contains("Island"));
    }

    @Test
    @DisplayName("Stops revealing the top library card when Goblin Spy leaves")
    void stopsRevealingWhenItLeavesBattlefield() {
        harness.addToBattlefield(player1, new GoblinSpy());
        harness.setLibrary(player1, List.of(new Forest()));
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[],[]]"));
        assertThat(harness.getConn2().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[],[]]"));
    }
}
