package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({BlossomPrancer.class, Forest.class, GloriousAnthem.class, GrizzlyBears.class, Shock.class})
class BlossomPrancerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers creature and enchantment cards from the top five")
    void etbOffersCreatureAndEnchantmentCards() {
        Card creature = new GrizzlyBears();
        Card enchantment = new GloriousAnthem();
        setupTopCards(creature, new Shock(), enchantment, new Forest(), new Shock());

        castAndResolveEtb();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(creature.getId(), enchantment.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Taking a card puts it into hand and does not gain life")
    void takingCardPutsItIntoHandWithoutLifeGain() {
        Card creature = new GrizzlyBears();
        setupTopCards(creature, new Shock(), new Forest(), new Shock(), new Forest());

        castAndResolveEtb();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4).doesNotContain(creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Taking no card gains four life")
    void takingNoCardGainsFourLife() {
        setupTopCards(new Shock(), new Forest(), new Shock(), new Forest(), new Shock());

        castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
    }

    private void setupTopCards(Card... cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(cards));
    }

    private void castAndResolveEtb() {
        harness.setHand(player1, List.of(new BlossomPrancer()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
