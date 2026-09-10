package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.ShuGeneral;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ControlOfTheCourt.class, ShuGeneral.class})
class ControlOfTheCourtTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new ControlOfTheCourt()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Resolving draws four cards then discards three at random")
    void resolvingDrawsFourThenDiscardsThreeAtRandom() {
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.setHand(player1, List.of(new ControlOfTheCourt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        // Spell left hand (-1), drew 4, discarded 3 at random = net gain of 1 card
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        // Deck lost 4 cards
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 4);
        // Graveyard has Control of the Court + 3 discarded
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        // Random discard does not prompt the player
        assertThat(gd.interaction.activeInteraction()).isNull();
        long randomDiscardLogs = gd.gameLog.stream().map(GameLogEntry::plainText)
                .filter(log -> log.contains("discards") && log.contains("at random"))
                .count();
        assertThat(randomDiscardLogs).isEqualTo(3);
    }

    @Test
    @DisplayName("Finishes discarding before losing for drawing from an empty library")
    void finishesResolvingBeforeEmptyLibraryLoss() {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new ShuGeneral());
        gd.playerDecks.get(player1.getId()).add(new ShuGeneral());

        harness.setHand(player1, List.of(new ControlOfTheCourt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        // State-based actions are checked after the spell finishes resolving (CR 704.4).
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .filteredOn(log -> log.contains("discards") && log.contains("at random"))
                .hasSize(2);
    }
}
