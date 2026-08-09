package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DiviningWitchTest extends BaseCardTest {

    @Test
    @DisplayName("Activating prompts for a card name and pays the discard cost")
    void activatingPromptsForCardNameAndPaysDiscardCost() {
        Permanent witch = addReadyWitch();
        Card discarded = named("Discarded Card", "{B}");
        harness.setHand(player1, List.of(discarded));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(witch.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(discarded.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    @Test
    @DisplayName("Finds the named card after six cards and exiles the other revealed cards")
    void findsNamedCardAfterSixCards() {
        addReadyWitch();
        Card discarded = named("Discarded Card", "{B}");
        harness.setHand(player1, List.of(discarded));

        UUID playerId = player1.getId();
        List<Card> deck = new ArrayList<>();
        List<Card> topSix = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            Card card = named("Chaff " + i, "{B}");
            topSix.add(card);
            deck.add(card);
        }
        Card miss = named("Miss", "{1}{B}");
        Card hit = named("Hit Card", "{2}{B}");
        Card leftover = named("Leftover", "{3}{B}");
        deck.add(miss);
        deck.add(hit);
        deck.add(leftover);
        gd.playerDecks.put(playerId, deck);

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Hit Card");

        assertThat(gd.playerHands.get(playerId))
                .extracting(Card::getId)
                .contains(hit.getId())
                .doesNotContain(discarded.getId());
        assertThat(gd.getPlayerExiledCards(playerId))
                .extracting(Card::getId)
                .containsAll(topSix.stream().map(Card::getId).toList())
                .contains(miss.getId())
                .doesNotContain(hit.getId(), leftover.getId());
        assertThat(gd.playerDecks.get(playerId)).containsExactly(leftover);
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        addReadyWitch();
        harness.setHand(player1, new ArrayList<>());
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyWitch() {
        Permanent witch = harness.addToBattlefieldAndReturn(player1, new DiviningWitch());
        witch.setSummoningSick(false);
        return witch;
    }

    private static Card named(String name, String manaCost) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost(manaCost);
        card.setColor(CardColor.BLACK);
        return card;
    }
}
