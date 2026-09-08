package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LanternOfInsightTest extends BaseCardTest {

    @Test
    @DisplayName("Reveals the controller's top library card to both players")
    void revealsTopLibraryCardToBothPlayers() {
        harness.addToBattlefield(player1, new LanternOfInsight());
        harness.setLibrary(player1, List.of(new Shock()));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Shock"));
        assertThat(harness.getConn2().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Shock"));
    }

    @Test
    @DisplayName("Taps and sacrifices itself when activating")
    void tapsAndSacrificesItselfWhenActivating() {
        Permanent lantern = harness.addToBattlefieldAndReturn(player1, new LanternOfInsight());

        harness.activateAbility(player1, 0, null, player1.getId());

        assertThat(lantern.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Lantern of Insight");
        harness.assertInGraveyard(player1, "Lantern of Insight");
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getTargetId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Resolves by shuffling the targeted player's library")
    void resolvesByShufflingTargetLibrary() {
        harness.addToBattlefield(player1, new LanternOfInsight());
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("shuffles their library"));
    }
}
