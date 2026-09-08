package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BasicConjurationTest extends BaseCardTest {

    @Test
    @DisplayName("Offers only a creature from the top six and gains three life")
    void offersCreatureAndGainsLife() {
        GrizzlyBears bears = new GrizzlyBears();
        HillGiant giant = new HillGiant();
        setupTopSix(bears, new Shock(), new Forest(), giant, new Shock(), new Forest());

        castBasicConjuration();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).hasSize(6);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(bears.getId(), giant.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the creature reveal bottoms all six cards and still gains life")
    void decliningRevealGainsLife() {
        setupTopSix(new GrizzlyBears(), new Shock(), new Forest(), new HillGiant(), new Shock(), new Forest());

        castBasicConjuration();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(6);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With no creature among the top six, the cards are bottomed without a choice")
    void noCreatureNeedsNoChoice() {
        setupTopSix(new Shock(), new Forest(), new Shock(), new Forest(), new Shock(), new Forest());

        castBasicConjuration();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(6);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castBasicConjuration() {
        harness.setHand(player1, List.of(new BasicConjuration()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void setupTopSix(Card... cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(cards));
    }
}
