package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(DreambornMuse.class)
class DreambornMuseTest extends BaseCardTest {

    // ===== Triggering =====

    @Test
    @DisplayName("Triggers during controller's upkeep and mills by hand size")
    void triggersDuringControllersUpkeep() {
        harness.addToBattlefield(player1, new DreambornMuse());
        harness.setHand(player1, List.of(new DreambornMuse(), new DreambornMuse(), new DreambornMuse()));
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        int deckSizeAfter = gd.playerDecks.get(player1.getId()).size();
        assertThat(deckSizeBefore - deckSizeAfter).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Triggers during opponent's upkeep and mills opponent by their hand size")
    void triggersDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new DreambornMuse());
        harness.setHand(player2, List.of(new DreambornMuse(), new DreambornMuse()));
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        int deckSizeAfter = gd.playerDecks.get(player2.getId()).size();
        assertThat(deckSizeBefore - deckSizeAfter).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Mills nothing when active player has empty hand")
    void millsNothingWithEmptyHand() {
        harness.addToBattlefield(player1, new DreambornMuse());
        harness.setHand(player1, List.of());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        int deckSizeAfter = gd.playerDecks.get(player1.getId()).size();
        assertThat(deckSizeBefore - deckSizeAfter).isEqualTo(0);
    }

    @Test
    @DisplayName("Mills only as many cards as remain in library")
    void millsOnlyRemainingCards() {
        harness.addToBattlefield(player1, new DreambornMuse());
        harness.setHand(player1, List.of(new DreambornMuse(), new DreambornMuse(), new DreambornMuse(),
                new DreambornMuse(), new DreambornMuse()));
        harness.setLibrary(player1, List.of(new DreambornMuse(), new DreambornMuse()));

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Does not mill controller when it is opponent's upkeep")
    void doesNotMillControllerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new DreambornMuse());
        harness.setHand(player1, List.of(new DreambornMuse(), new DreambornMuse()));
        harness.setHand(player2, List.of(new DreambornMuse()));
        int p1DeckBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        // Player1's deck should be untouched — the trigger targets player2
        assertThat(gd.playerDecks.get(player1.getId()).size()).isEqualTo(p1DeckBefore);
        // Player2 milled 1 card (their hand size)
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Uses the active player's hand size when the trigger resolves")
    void usesHandSizeAtResolution() {
        harness.addToBattlefield(player1, new DreambornMuse());
        harness.setHand(player1, List.of(new DreambornMuse(), new DreambornMuse(), new DreambornMuse()));
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.setHand(player1, List.of(new DreambornMuse()));
        harness.passBothPriorities();

        int deckSizeAfter = gd.playerDecks.get(player1.getId()).size();
        assertThat(deckSizeBefore - deckSizeAfter).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }
}

