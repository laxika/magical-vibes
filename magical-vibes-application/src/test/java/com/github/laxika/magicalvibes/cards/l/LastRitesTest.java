package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LastRitesTest extends BaseCardTest {

    @Test
    @DisplayName("Discards one nonland card from the target's hand for each card discarded")
    void discardsOneNonlandCardPerCardDiscarded() {
        harness.setHand(player1, new ArrayList<>(List.of(
                new LastRites(), new GrizzlyBears(), new GrizzlyBears())));
        harness.setHand(player2, new ArrayList<>(List.of(
                new Peek(), new Forest(), new GrizzlyBears())));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 2);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.validIndices()).containsExactly(0, 2);
        assertThat(choice.remainingCount()).isEqualTo(2);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Forest");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Peek", "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Last Rites", "Grizzly Bears", "Grizzly Bears");
    }

    @Test
    @DisplayName("Discarding zero cards leaves the target hand unchanged")
    void discardingZeroCardsDoesNothingToTargetHand() {
        harness.setHand(player1, new ArrayList<>(List.of(new LastRites(), new GrizzlyBears())));
        harness.setHand(player2, new ArrayList<>(List.of(new Peek())));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Peek");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Last Rites");
    }

    @Test
    @DisplayName("Lands cannot be chosen for the target's discards")
    void landsAreExcludedFromTargetChoices() {
        harness.setHand(player1, new ArrayList<>(List.of(new LastRites(), new GrizzlyBears())));
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new Peek())));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 1);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class)
                .validIndices()).containsExactly(1);
    }
}
