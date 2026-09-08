package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LuminousBonds;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SumalaWoodshaperTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers a creature or enchantment from the top four cards")
    void etbOffersCreatureOrEnchantment() {
        Card creature = new GrizzlyBears();
        Card enchantment = new LuminousBonds();
        setupTopCards(List.of(creature, enchantment, new Shock(), new Plains()));
        castAndResolveEtb();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).hasSize(4);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(creature.getId(), enchantment.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Choosing an eligible card puts it into hand and bottoms the rest")
    void choosingEligibleCardPutsItIntoHand() {
        Card creature = new GrizzlyBears();
        setupTopCards(List.of(creature, new Shock(), new Plains(), new Shock()));
        castAndResolveEtb();

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3).doesNotContain(creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining leaves all looked-at cards on the bottom")
    void decliningBottomsAllLookedAtCards() {
        Card shock1 = new Shock();
        Card enchantment = new LuminousBonds();
        Card plains = new Plains();
        Card shock2 = new Shock();
        setupTopCards(List.of(shock1, enchantment, plains, shock2));
        castAndResolveEtb();

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(enchantment);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(
                shock1, enchantment, plains, shock2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void setupTopCards(List<Card> cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(cards);
    }

    private void castAndResolveEtb() {
        harness.setHand(player1, List.of(new SumalaWoodshaper()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
