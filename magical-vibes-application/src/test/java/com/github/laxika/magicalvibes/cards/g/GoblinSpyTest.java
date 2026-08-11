package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GoblinSpyTest extends BaseCardTest {

    @Test
    @DisplayName("Reveals the controller's top library card to both players")
    void revealsTopLibraryCardToBothPlayers() {
        harness.addToBattlefield(player1, new GoblinSpy());
        harness.setLibrary(player1, List.of(new Shock()));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{"));
        assertThat(harness.getConn2().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{"));
    }

    @Test
    @DisplayName("Stops revealing the top library card when Goblin Spy leaves")
    void stopsRevealingWhenItLeavesBattlefield() {
        harness.addToBattlefield(player1, new GoblinSpy());
        harness.setLibrary(player1, List.of(new Shock()));
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[],[]]"));
        assertThat(harness.getConn2().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[],[]]"));
    }
}
