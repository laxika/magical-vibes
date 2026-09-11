package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hurricane;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ParadoxSurveyorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers lands and cards with X in their mana cost")
    void etbOffersLandsAndXCards() {
        Card hurricane = new Hurricane();
        Card forest = new Forest();
        setupTopCards(List.of(hurricane, forest, new GrizzlyBears(), new Shock(), new Divination()));
        castAndResolveEtb();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).hasSize(5);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(hurricane.getId(), forest.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Revealing an eligible card puts it into hand and bottoms the rest")
    void revealingEligibleCardPutsItIntoHand() {
        Card hurricane = new Hurricane();
        setupTopCards(List.of(hurricane, new Forest(), new GrizzlyBears(), new Shock(), new Divination()));
        castAndResolveEtb();

        harness.handleMultipleCardsChosen(player1, List.of(hurricane.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(hurricane);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4).doesNotContain(hurricane);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the optional reveal bottoms all five cards")
    void decliningRevealBottomsEverything() {
        Card forest = new Forest();
        setupTopCards(List.of(forest, new Hurricane(), new GrizzlyBears(), new Shock(), new Divination()));
        castAndResolveEtb();

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(forest);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With no eligible card, the top five go straight to the bottom")
    void noEligibleCardNeedsNoChoice() {
        setupTopCards(List.of(new GrizzlyBears(), new Shock(), new Divination(),
                new GrizzlyBears(), new Shock()));
        castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
    }

    private void setupTopCards(List<Card> cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(cards);
    }

    private void castAndResolveEtb() {
        harness.setHand(player1, List.of(new ParadoxSurveyor()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
