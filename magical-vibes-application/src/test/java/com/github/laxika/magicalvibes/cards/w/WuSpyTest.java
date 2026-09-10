package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WuSpy.class, Island.class, Forest.class})
class WuSpyTest extends BaseCardTest {

    private void castWuSpy(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new WuSpy()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0, targetPlayerId);
    }

    @Test
    @DisplayName("ETB puts the chosen card into target player's graveyard; the other stays on top")
    void putsChosenIntoGraveyardOtherStaysOnTop() {
        Card top = new Island();
        Card second = new Forest();
        harness.setLibrary(player2, List.of(top, second));

        castWuSpy(player2.getId());
        harness.passBothPriorities(); // resolve creature spell -> ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);

        // Put the top card (Island) into the graveyard; Forest stays on top.
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(top.getId()));

        List<Card> deckAfter = gd.playerDecks.get(player2.getId());
        assertThat(deckAfter).hasSize(1);
        assertThat(deckAfter.get(0).getId()).isEqualTo(second.getId());
    }

    @Test
    @DisplayName("Can target itself (any player): controller mills one of their own top two")
    void canTargetSelf() {
        Card top = new Island();
        Card second = new Forest();
        harness.setLibrary(player1, List.of(top, second));

        castWuSpy(player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(second.getId()));
    }

    @Test
    @DisplayName("Looks at only the available card when the library has fewer than two")
    void looksAtOnlyAvailableCard() {
        Card only = new Island();
        harness.setLibrary(player2, List.of(only));

        castWuSpy(player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(only.getId()));
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Empty library does nothing")
    void emptyLibraryDoesNothing() {
        harness.setLibrary(player2, List.of());

        castWuSpy(player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }
}
