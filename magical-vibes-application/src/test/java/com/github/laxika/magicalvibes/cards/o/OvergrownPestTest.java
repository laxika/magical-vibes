package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AwakenedSkyclave;
import com.github.laxika.magicalvibes.cards.f.FarmMarket;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.m.Market;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AwakenedSkyclave.class, FarmMarket.class, Forest.class, InvasionOfZendikar.class,
        Market.class, OvergrownPest.class, Shock.class})
class OvergrownPestTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers a land or double-faced card from the top five")
    void offersLandOrDoubleFacedCard() {
        Card land = new Forest();
        Card doubleFaced = new InvasionOfZendikar();
        Card split = new FarmMarket();
        List<Card> topFive = List.of(new Shock(), doubleFaced, split, land, new Shock());
        setLibrary(topFive);

        castAndResolve();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(land.getId(), doubleFaced.getId())
                .doesNotContain(split.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(doubleFaced.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(doubleFaced);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4)
                .containsExactlyInAnyOrder(topFive.get(0), topFive.get(2), topFive.get(3), topFive.get(4));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With no land or double-faced card, all five cards go to the bottom")
    void noEligibleCardBottomsAllFive() {
        List<Card> topFive = List.of(new Shock(), new Shock(), new FarmMarket(), new Shock(), new Shock());
        setLibrary(topFive);

        castAndResolve();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(topFive);
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new OvergrownPest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void setLibrary(List<Card> cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(cards);
    }
}
