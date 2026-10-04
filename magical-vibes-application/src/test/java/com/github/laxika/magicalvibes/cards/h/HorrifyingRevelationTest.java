package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.f.Flensermite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HorrifyingRevelation.class, Flensermite.class})
class HorrifyingRevelationTest extends BaseCardTest {


    @Test
    @DisplayName("Target opponent discards a card then mills a card")
    void targetOpponentDiscardsAndMills() {
        harness.setHand(player2, List.of(new Flensermite()));
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new HorrifyingRevelation()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Discard interaction is awaited
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        // Flensermite discarded + 1 card milled
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Flensermite");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Target opponent with empty hand still mills a card")
    void emptyHandStillMills() {
        harness.setHand(player2, List.of());
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new HorrifyingRevelation()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // No discard prompt since hand is empty, mill still happens
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 1);
    }


    @Test
    @DisplayName("Can target yourself to discard and mill")
    void canTargetSelf() {
        harness.setHand(player1, List.of(new HorrifyingRevelation(), new Flensermite()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        // Self-discard interaction
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player1.getId());

        harness.handleCardChosen(player1, 0);

        // Flensermite discarded (index 0 of remaining hand after spell was cast)
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }


    @Test
    @DisplayName("Goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player2, List.of(new Flensermite()));
        harness.setHand(player1, List.of(new HorrifyingRevelation()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player1, "Horrifying Revelation");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Target chooses exactly one discard before the top library card is milled")
    void discardChoiceFinishesBeforeMilling() {
        Flensermite retained = new Flensermite();
        HorrifyingRevelation discarded = new HorrifyingRevelation();
        Flensermite milled = new Flensermite();
        Flensermite remaining = new Flensermite();
        harness.setHand(player2, List.of(retained, discarded));
        harness.setLibrary(player2, List.of(milled, remaining));
        harness.setHand(player1, List.of(new HorrifyingRevelation()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(milled, remaining);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(discarded, milled);

        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(retained);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded, milled);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library does not prevent discarding")
    void emptyLibraryStillDiscards() {
        Flensermite discarded = new Flensermite();
        harness.setHand(player2, List.of(discarded));
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new HorrifyingRevelation()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Horrifying Revelation");
    }
}
