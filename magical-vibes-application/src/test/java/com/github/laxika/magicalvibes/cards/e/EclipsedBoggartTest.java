package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GoblinArsonist;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EclipsedBoggartTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers Goblin, Swamp, and Mountain cards among the top four")
    void etbOffersMatchingCards() {
        GoblinArsonist goblin = new GoblinArsonist();
        Swamp swamp = new Swamp();
        Mountain mountain = new Mountain();
        setupTopCards(List.of(goblin, new Plains(), swamp, mountain));
        castAndResolveEtb();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).hasSize(4);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(goblin.getId(), swamp.getId(), mountain.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Choosing a matching card puts it into hand and bottoms the rest randomly")
    void choosingMatchingCardPutsItIntoHand() {
        GoblinArsonist goblin = new GoblinArsonist();
        setupTopCards(List.of(goblin, new Plains(), new GrizzlyBears(), new Plains()));
        castAndResolveEtb();

        harness.handleMultipleCardsChosen(player1, List.of(goblin.getId()));

        harness.assertInHand(player1, "Goblin Arsonist");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3).doesNotContain(goblin);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the optional reveal bottoms all four cards")
    void decliningBottomsEverything() {
        GoblinArsonist goblin = new GoblinArsonist();
        setupTopCards(List.of(goblin, new Plains(), new GrizzlyBears(), new Plains()));
        castAndResolveEtb();

        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertNotInHand(player1, "Goblin Arsonist");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With no Goblin, Swamp, or Mountain among the top four, no choice is needed")
    void noMatchingCardNeedsNoChoice() {
        setupTopCards(List.of(new GrizzlyBears(), new Plains(), new GrizzlyBears(), new Plains()));
        castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
    }

    private void setupTopCards(List<Card> cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(cards);
    }

    private void castAndResolveEtb() {
        harness.setHand(player1, List.of(new EclipsedBoggart()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
