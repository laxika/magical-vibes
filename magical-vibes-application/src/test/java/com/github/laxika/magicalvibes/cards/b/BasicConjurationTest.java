package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
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

@CardUsed({BasicConjuration.class, GrizzlyBears.class, HillGiant.class, Shock.class, Forest.class})
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

    @Test
    @DisplayName("A single creature in a short library may still be declined")
    void singleCreatureMayBeDeclined() {
        GrizzlyBears bears = new GrizzlyBears();
        setupTopSix(bears);

        castBasicConjuration();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library still allows the three life gain")
    void emptyLibraryStillGainsLife() {
        setupTopSix();

        castBasicConjuration();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Only the top six are eligible and the rest are bottomed below untouched cards")
    void preservesUntouchedLibraryCards() {
        GrizzlyBears bears = new GrizzlyBears();
        List<Card> lookedAt = List.of(bears, new Shock(), new Forest(),
                new Shock(), new Forest(), new Shock());
        HillGiant seventh = new HillGiant();
        Forest eighth = new Forest();
        harness.setLibrary(player1, List.of(lookedAt.get(0), lookedAt.get(1), lookedAt.get(2),
                lookedAt.get(3), lookedAt.get(4), lookedAt.get(5), seventh, eighth));

        castBasicConjuration();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).containsExactlyElementsOf(lookedAt);
        assertThat(choice.validCardIds()).containsExactly(bears.getId());
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bears);
        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(7);
        assertThat(library.subList(0, 2)).containsExactly(seventh, eighth);
        assertThat(library.subList(2, 7)).containsExactlyInAnyOrderElementsOf(lookedAt.subList(1, 6));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castBasicConjuration() {
        harness.setHand(player1, List.of(new BasicConjuration()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    private void setupTopSix(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
