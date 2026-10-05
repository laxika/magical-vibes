package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.ConjurersBauble;
import com.github.laxika.magicalvibes.cards.s.SerumVisions;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LanternOfInsight.class, ConjurersBauble.class, SerumVisions.class})
class LanternOfInsightTest extends BaseCardTest {

    @Test
    @DisplayName("A second Lantern keeps both libraries revealed after the first is sacrificed")
    void anotherLanternKeepsLibrariesRevealed() {
        harness.addToBattlefield(player1, new LanternOfInsight());
        harness.addToBattlefield(player2, new LanternOfInsight());
        harness.setLibrary(player1, List.of(new ConjurersBauble()));
        harness.setLibrary(player2, List.of(new SerumVisions()));

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.clearMessages();
        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\"")
                        && message.contains("Conjurer's Bauble") && message.contains("Serum Visions"));
        assertThat(harness.getConn2().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\"")
                        && message.contains("Conjurer's Bauble") && message.contains("Serum Visions"));
    }

    @Test
    @DisplayName("An empty library can be targeted and shuffled")
    void shufflesEmptyLibrary() {
        harness.addToBattlefield(player1, new LanternOfInsight());
        harness.setLibrary(player2, List.of());

        harness.publishState();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("shuffles their library")).isTrue();
        harness.assertInGraveyard(player1, "Lantern of Insight");
    }

    @Test
    @DisplayName("A tapped Lantern cannot activate or sacrifice itself")
    void tappedLanternCannotActivate() {
        Permanent lantern = harness.addToBattlefieldAndReturn(player1, new LanternOfInsight());
        lantern.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Lantern of Insight");
        harness.assertNotInGraveyard(player1, "Lantern of Insight");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A permanent cannot be targeted and an invalid activation pays no costs")
    void cannotTargetPermanent() {
        Permanent lantern = harness.addToBattlefieldAndReturn(player1, new LanternOfInsight());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, lantern.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(lantern.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Lantern of Insight");
        harness.assertNotInGraveyard(player1, "Lantern of Insight");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Reveals the controller's top library card to both players")
    void revealsTopLibraryCardToBothPlayers() {
        harness.addToBattlefield(player1, new LanternOfInsight());
        harness.setLibrary(player1, List.of(new ConjurersBauble()));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Conjurer's Bauble"));
        assertThat(harness.getConn2().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Conjurer's Bauble"));
    }

    @Test
    @DisplayName("Reveals both players' top library cards to both players")
    void revealsBothPlayersTopLibraryCardsToBothPlayers() {
        harness.addToBattlefield(player1, new LanternOfInsight());
        harness.setLibrary(player1, List.of(new ConjurersBauble()));
        harness.setLibrary(player2, List.of(new SerumVisions()));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\"")
                        && message.contains("Conjurer's Bauble")
                        && message.contains("Serum Visions"));
        assertThat(harness.getConn2().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\"")
                        && message.contains("Conjurer's Bauble")
                        && message.contains("Serum Visions"));
    }

    @Test
    @DisplayName("Stops revealing library top cards after it is sacrificed")
    void stopsRevealingAfterItIsSacrificed() {
        harness.addToBattlefield(player1, new LanternOfInsight());
        harness.setLibrary(player1, List.of(new ConjurersBauble()));

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.clearMessages();
        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .noneMatch(message -> message.contains("\"revealedLibraryTopCards\"")
                        && message.contains("Conjurer's Bauble"));
        assertThat(harness.getConn2().getSentMessages())
                .noneMatch(message -> message.contains("\"revealedLibraryTopCards\"")
                        && message.contains("Conjurer's Bauble"));
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
        assertThat(gameLogContains("shuffles their library")).isTrue();
    }
}
