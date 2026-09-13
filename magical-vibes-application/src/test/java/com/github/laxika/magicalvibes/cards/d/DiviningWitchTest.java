package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.m.Massacre;
import com.github.laxika.magicalvibes.cards.m.MindSlash;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DiviningWitch.class, Massacre.class, MindSlash.class})
class DiviningWitchTest extends BaseCardTest {

    @Test
    @DisplayName("Activating prompts for a card name and pays the discard cost")
    void activatingPromptsForCardNameAndPaysDiscardCost() {
        Permanent witch = addReadyWitch();
        Card discarded = new Massacre();
        activateWithDiscard(discarded);

        assertThat(witch.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(discarded.getId());
        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.context())
                .isInstanceOf(ChoiceContext.ChooseNameExileTopRevealUntilNamedChoice.class);
    }

    @Test
    @DisplayName("Finds the named card after six cards and exiles the other revealed cards")
    void findsNamedCardAfterSixCards() {
        addReadyWitch();
        Card discarded = new Massacre();

        UUID playerId = player1.getId();
        List<Card> deck = new ArrayList<>();
        List<Card> topSix = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            Card card = new Massacre();
            topSix.add(card);
            deck.add(card);
        }
        Card miss = new Massacre();
        Card hit = new MindSlash();
        Card secondHit = new MindSlash();
        Card leftover = new Massacre();
        deck.add(miss);
        deck.add(hit);
        deck.add(secondHit);
        deck.add(leftover);
        harness.setLibrary(player1, deck);

        activateWithDiscard(discarded);
        harness.handleListChoice(player1, "Mind Slash");

        assertThat(gd.playerHands.get(playerId))
                .extracting(Card::getId)
                .contains(hit.getId())
                .doesNotContain(discarded.getId());
        assertThat(gd.getPlayerExiledCards(playerId))
                .extracting(Card::getId)
                .containsAll(topSix.stream().map(Card::getId).toList())
                .contains(miss.getId())
                .doesNotContain(hit.getId(), secondHit.getId(), leftover.getId());
        assertThat(gd.playerDecks.get(playerId)).containsExactly(secondHit, leftover);
    }

    @Test
    @DisplayName("A card exiled among the top six is not found and the remaining library is exiled")
    void namedCardExiledAmongTopSixIsNotFound() {
        addReadyWitch();
        Card discarded = new Massacre();
        MindSlash namedCard = new MindSlash();
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            deck.add(i == 2 ? namedCard : new Massacre());
        }
        Card remaining = new Massacre();
        deck.add(remaining);
        harness.setLibrary(player1, deck);

        activateWithDiscard(discarded);
        harness.handleListChoice(player1, "Mind Slash");

        UUID playerId = player1.getId();
        assertThat(gd.playerHands.get(playerId)).doesNotContain(namedCard);
        assertThat(gd.playerDecks.get(playerId)).isEmpty();
        assertThat(gd.getPlayerExiledCards(playerId))
                .extracting(Card::getId)
                .hasSize(7)
                .containsAll(deck.stream().map(Card::getId).toList());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Choosing a name with an empty library completes without exiling or finding a card")
    void emptyLibraryCompletesAfterChoosingName() {
        addReadyWitch();
        Card discarded = new Massacre();
        harness.setLibrary(player1, List.of());

        activateWithDiscard(discarded);
        harness.handleListChoice(player1, "Divining Witch");

        UUID playerId = player1.getId();
        assertThat(gd.playerHands.get(playerId)).isEmpty();
        assertThat(gd.playerDecks.get(playerId)).isEmpty();
        assertThat(gd.getPlayerExiledCards(playerId)).isEmpty();
        assertThat(gd.playerGraveyards.get(playerId)).contains(discarded);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        addReadyWitch();
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without enough mana and does not pay any part of the cost")
    void cannotActivateWithoutEnoughMana() {
        Permanent witch = addReadyWitch();
        Card discarded = new Massacre();
        harness.setHand(player1, List.of(discarded));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(witch.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(discarded);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addReadyWitch() {
        return addCreatureReady(player1, new DiviningWitch());
    }

    private void activateWithDiscard(Card discarded) {
        harness.setHand(player1, List.of(discarded));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
    }
}
