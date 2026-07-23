package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DemonicConsultationTest extends BaseCardTest {

    private void cast() {
        harness.setHand(player1, List.of(new DemonicConsultation()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Resolving prompts the controller to name a card")
    void promptsControllerToNameCard() {
        cast();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        var choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.context()).isInstanceOf(ChoiceContext.ChooseNameExileTopRevealUntilNamedChoice.class);
    }

    @Test
    @DisplayName("Exiles top six, puts the named card into hand, and exiles other revealed cards")
    void findsNamedCardAfterTopSix() {
        UUID p1 = player1.getId();
        List<Card> deck = new ArrayList<>();
        List<Card> topSix = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            Card c = named("Chaff " + i, "{B}");
            topSix.add(c);
            deck.add(c);
        }
        Card miss = named("Miss", "{1}{B}");
        Card hit = named("Hit Card", "{2}{B}");
        Card leftover = named("Leftover", "{3}{B}");
        deck.add(miss);
        deck.add(hit);
        deck.add(leftover);
        gd.playerDecks.put(p1, deck);

        cast();
        harness.handleListChoice(player1, "Hit Card");

        assertThat(gd.playerHands.get(p1)).anyMatch(c -> c.getId().equals(hit.getId()));
        assertThat(gd.getPlayerExiledCards(p1))
                .extracting(Card::getId)
                .containsAll(topSix.stream().map(Card::getId).toList())
                .contains(miss.getId())
                .doesNotContain(hit.getId(), leftover.getId());
        assertThat(gd.playerDecks.get(p1)).containsExactly(leftover);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("If the named card is never revealed, the entire remaining library is exiled")
    void missesAndExilesLibrary() {
        UUID p1 = player1.getId();
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            deck.add(named("Top " + i, "{B}"));
        }
        Card onlyCopy = named("Only Copy", "{B}");
        // Put the only copy among the top six so the dig cannot find it.
        deck.set(2, onlyCopy);
        Card restA = named("Rest A", "{1}");
        Card restB = named("Rest B", "{2}");
        deck.add(restA);
        deck.add(restB);
        gd.playerDecks.put(p1, deck);

        cast();
        harness.handleListChoice(player1, "Only Copy");

        assertThat(gd.playerHands.get(p1)).noneMatch(c -> c.getId().equals(onlyCopy.getId()));
        assertThat(gd.playerDecks.get(p1)).isEmpty();
        assertThat(gd.getPlayerExiledCards(p1))
                .extracting(Card::getId)
                .contains(onlyCopy.getId(), restA.getId(), restB.getId());
    }

    @Test
    @DisplayName("Named card immediately after the top six goes to hand with no other dig exile")
    void namedCardIsNextAfterTopSix() {
        UUID p1 = player1.getId();
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            deck.add(named("Skip " + i, "{B}"));
        }
        Card hit = named("Immediate Hit", "{B}");
        deck.add(hit);
        gd.playerDecks.put(p1, deck);

        cast();
        harness.handleListChoice(player1, "Immediate Hit");

        assertThat(gd.playerHands.get(p1)).anyMatch(c -> c.getId().equals(hit.getId()));
        assertThat(gd.playerDecks.get(p1)).isEmpty();
        assertThat(gd.getPlayerExiledCards(p1)).hasSize(6)
                .noneMatch(c -> c.getId().equals(hit.getId()));
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
