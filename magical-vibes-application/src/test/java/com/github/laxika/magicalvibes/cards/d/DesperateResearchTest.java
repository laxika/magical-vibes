package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DesperateResearchTest extends BaseCardTest {

    private void cast() {
        harness.setHand(player1, List.of(new DesperateResearch()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Resolving prompts the controller to name a non-basic-land card")
    void promptsForNonBasicLandName() {
        UUID p1 = player1.getId();
        gd.playerDecks.get(p1).addFirst(basicLand("Plains"));
        gd.playerDecks.get(p1).addFirst(named("Research Target"));

        cast();

        var choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.context()).isInstanceOf(ChoiceContext.ChooseNameRevealTopCardsToHandRestToExileChoice.class);
        assertThat(choice.options()).contains("Research Target").doesNotContain("Plains");
    }

    @Test
    @DisplayName("Puts all matching cards from the top seven into hand and exiles the rest")
    void matchingCardsGoToHandAndRestAreExiled() {
        UUID p1 = player1.getId();
        Card hit1 = named("Research Target");
        Card miss1 = named("Miss One");
        Card hit2 = named("Research Target");
        Card miss2 = named("Miss Two");
        Card miss3 = named("Miss Three");
        Card miss4 = named("Miss Four");
        Card miss5 = named("Miss Five");
        Card untouched = named("Untouched");
        gd.playerDecks.put(p1, new ArrayList<>(List.of(
                hit1, miss1, hit2, miss2, miss3, miss4, miss5, untouched)));

        cast();
        harness.handleListChoice(player1, "Research Target");

        assertThat(gd.playerHands.get(p1)).extracting(Card::getId)
                .contains(hit1.getId(), hit2.getId())
                .doesNotContain(miss1.getId(), miss2.getId());
        assertThat(gd.getPlayerExiledCards(p1)).extracting(Card::getId)
                .contains(miss1.getId(), miss2.getId(), miss3.getId(), miss4.getId(), miss5.getId())
                .doesNotContain(hit1.getId(), hit2.getId(), untouched.getId());
        assertThat(gd.playerDecks.get(p1)).containsExactly(untouched);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Exiles all revealed cards when the chosen name is absent")
    void noMatchExilesAllSeven() {
        UUID p1 = player1.getId();
        List<Card> revealed = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            revealed.add(named("Chaff " + i));
        }
        Card wanted = named("Wanted Card");
        List<Card> deck = new ArrayList<>(revealed);
        deck.add(wanted);
        gd.playerDecks.put(p1, deck);

        cast();
        harness.handleListChoice(player1, "Wanted Card");

        assertThat(gd.playerHands.get(p1)).extracting(Card::getId).doesNotContain(
                revealed.stream().map(Card::getId).toArray(UUID[]::new));
        assertThat(gd.getPlayerExiledCards(p1)).extracting(Card::getId)
                .containsExactlyInAnyOrderElementsOf(revealed.stream().map(Card::getId).toList());
        assertThat(gd.playerDecks.get(p1)).containsExactly(wanted);
    }

    private static Card named(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost("{1}");
        card.setColor(CardColor.BLACK);
        return card;
    }

    private static Card basicLand(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.LAND);
        card.setSupertypes(Set.of(CardSupertype.BASIC));
        return card;
    }
}
