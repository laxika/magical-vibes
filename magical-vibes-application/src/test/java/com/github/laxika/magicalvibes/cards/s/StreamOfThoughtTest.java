package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.MotherBear;
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

@CardUsed({StreamOfThought.class, MotherBear.class})
class StreamOfThoughtTest extends BaseCardTest {

    @Test
    @DisplayName("Mills the target player and shuffles up to four cards from your graveyard")
    void millsAndShufflesOwnGraveyard() {
        List<Card> ownGraveyard = List.of(
                new MotherBear(), new MotherBear(), new MotherBear(),
                new MotherBear(), new MotherBear());
        harness.setGraveyard(player1, ownGraveyard);
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(
                new MotherBear(), new MotherBear(), new MotherBear(), new MotherBear()));
        harness.setHand(player1, List.of(new StreamOfThought()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(player2.getId()));

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(4);
        assertThat(choice.validCardIds()).containsExactlyElementsOf(
                ownGraveyard.stream().map(Card::getId).toList());

        harness.handleMultipleCardsChosen(player1,
                ownGraveyard.subList(0, 4).stream().map(Card::getId).toList());
        resolveAllTriggers();

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
                new MotherBear(), new MotherBear(), new MotherBear(),
                new MotherBear(), new MotherBear(), new MotherBear(),
                new MotherBear(), new MotherBear()));
        harness.setHand(player1, List.of(new StreamOfThought()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castSorceryWithRepeatedCosts(player1, 0,
                List.of("{2}{U}{U}"), List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(entry -> entry.isCopy())).hasSize(1);

        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(8);
    }

    @Test
    @DisplayName("Self-mill allows choosing freshly milled cards, but not the resolving spell")
    void choosesFreshlyMilledCardsDuringResolution() {
        Card spell = new StreamOfThought();
        List<Card> milledCards = List.of(
                new StreamOfThought(), new StreamOfThought(),
                new StreamOfThought(), new StreamOfThought());
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, milledCards);
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(player1.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrderElementsOf(milledCards.stream().map(Card::getId).toList());
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(4);
        assertThat(choice.validCardIds())
                .containsExactlyInAnyOrderElementsOf(milledCards.stream().map(Card::getId).toList())
                .doesNotContain(spell.getId());

        harness.handleMultipleCardsChosen(player1,
                milledCards.subList(0, 2).stream().map(Card::getId).toList());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(milledCards.get(0).getId(), milledCards.get(1).getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(milledCards.get(2).getId(), milledCards.get(3).getId(), spell.getId());
    }

    @Test
    @DisplayName("Choosing zero cards still mills a short library")
    void mayShuffleZeroCards() {
        Card graveyardCard = new StreamOfThought();
        Card spell = new StreamOfThought();
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setLibrary(player2, List.of(new StreamOfThought(), new StreamOfThought()));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(player2.getId()));

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(graveyardCard.getId(), spell.getId());
    }

    @Test
    @DisplayName("Two replicate payments create two copies without casting them")
    void replicateTwiceMillsTwelveCards() {
        Card spell = new StreamOfThought();
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player2, List.of(
                new StreamOfThought(), new StreamOfThought(), new StreamOfThought(),
                new StreamOfThought(), new StreamOfThought(), new StreamOfThought(),
                new StreamOfThought(), new StreamOfThought(), new StreamOfThought(),
                new StreamOfThought(), new StreamOfThought(), new StreamOfThought()));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 9);

        harness.castSorceryWithRepeatedCosts(player1, 0,
                List.of("{2}{U}{U}", "{2}{U}{U}"), List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(entry -> entry.isCopy())).hasSize(2);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(12);
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .containsExactly(spell.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a permanent instead of a player")
    void cannotTargetPermanent() {
        harness.addToBattlefield(player2, new MotherBear());
        harness.setHand(player1, List.of(new StreamOfThought()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, harness.getPermanentId(player2, "Mother Bear")))
                .isInstanceOf(IllegalStateException.class);
    }

}
