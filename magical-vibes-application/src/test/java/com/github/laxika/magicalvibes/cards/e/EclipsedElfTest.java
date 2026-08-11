package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
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

class EclipsedElfTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers Elf, Swamp, and Forest cards among the top four")
    void etbOffersMatchingCards() {
        LlanowarElves elf = new LlanowarElves();
        Swamp swamp = new Swamp();
        Forest forest = new Forest();
        setupTopCards(List.of(elf, new Plains(), swamp, forest));
        castAndResolveEtb();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).hasSize(4);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(elf.getId(), swamp.getId(), forest.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Choosing a matching card puts it into hand and bottoms the rest randomly")
    void choosingMatchingCardPutsItIntoHand() {
        LlanowarElves elf = new LlanowarElves();
        setupTopCards(List.of(elf, new Plains(), new GrizzlyBears(), new Plains()));
        castAndResolveEtb();

        harness.handleMultipleCardsChosen(player1, List.of(elf.getId()));

        harness.assertInHand(player1, "Llanowar Elves");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3).doesNotContain(elf);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the optional reveal bottoms all four cards")
    void decliningBottomsEverything() {
        LlanowarElves elf = new LlanowarElves();
        setupTopCards(List.of(elf, new Plains(), new GrizzlyBears(), new Plains()));
        castAndResolveEtb();

        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertNotInHand(player1, "Llanowar Elves");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With no Elf, Swamp, or Forest among the top four, no choice is needed")
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
        harness.setHand(player1, List.of(new EclipsedElf()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
