package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StreamOfThought.class, GrizzlyBears.class})
class StreamOfThoughtTest extends BaseCardTest {

    @Test
    @DisplayName("Mills the target player and shuffles up to four cards from your graveyard")
    void millsAndShufflesOwnGraveyard() {
        List<Card> ownGraveyard = List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears());
        harness.setGraveyard(player1, ownGraveyard);
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new StreamOfThought()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, List.of(player2.getId()));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(4);
        assertThat(choice.validCardIds()).containsExactlyElementsOf(
                ownGraveyard.stream().map(Card::getId).toList());

        harness.handleMultipleCardsChosen(player1,
                ownGraveyard.subList(0, 4).stream().map(Card::getId).toList());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(ownGraveyard.get(4).getId());
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrderElementsOf(
                        ownGraveyard.subList(0, 4).stream().map(Card::getId).toList());
    }

    @Test
    @DisplayName("Replicate creates one copy for each replicate payment")
    void replicateCreatesCopiesForEachPayment() {
        harness.setLibrary(player2, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new StreamOfThought()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castSorceryWithRepeatedCosts(player1, 0,
                List.of("{2}{U}{U}"), List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(entry -> entry.isCopy())).hasSize(1);

        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(8);
    }

    @Test
    @DisplayName("Cannot target a permanent instead of a player")
    void cannotTargetPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new StreamOfThought()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);
    }

}
