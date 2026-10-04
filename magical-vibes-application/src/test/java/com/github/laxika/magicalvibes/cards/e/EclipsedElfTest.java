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
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EclipsedElf.class, LlanowarElves.class, Swamp.class, Forest.class, Plains.class, GrizzlyBears.class})
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

    @Test
    @DisplayName("Either eligible basic land can be put into hand")
    void canChooseSwampOrForest() {
        for (Card land : List.of(new Swamp(), new Forest())) {
            setupTopCards(List.of(land));
            castAndResolveEtb();

            harness.handleMultipleCardsChosen(player1, List.of(land.getId()));

            assertThat(gd.playerHands.get(player1.getId())).contains(land);
            assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
            assertThat(gd.interaction.activeInteraction()).isNull();
        }
    }

    @Test
    @DisplayName("Only the top four are considered and the rest go below untouched cards")
    void preservesUntouchedLibraryCardsAboveBottomedCards() {
        Forest forest = new Forest();
        Plains first = new Plains();
        Plains second = new Plains();
        GrizzlyBears bear = new GrizzlyBears();
        Swamp fifth = new Swamp();
        LlanowarElves sixth = new LlanowarElves();
        setupTopCards(List.of(forest, first, second, bear, fifth, sixth));
        castAndResolveEtb();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(forest.getId());
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library.subList(0, 2)).containsExactly(fifth, sixth);
        assertThat(library.subList(2, 5)).containsExactlyInAnyOrder(first, second, bear);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A sole matching card can still be declined")
    void canDeclineWithOneCardInLibrary() {
        Forest forest = new Forest();
        setupTopCards(List.of(forest));
        castAndResolveEtb();

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library resolves without a choice or a draw")
    void emptyLibraryResolvesWithoutChoice() {
        setupTopCards(List.of());
        castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Eclipsed Elf");
    }

    @Test
    @DisplayName("The choice rejects nonmatching cards and taking two eligible cards")
    void rejectsInvalidSelectionsWithoutLosingChoice() {
        Forest forest = new Forest();
        Swamp swamp = new Swamp();
        Plains plains = new Plains();
        setupTopCards(List.of(forest, swamp, plains));
        castAndResolveEtb();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(plains.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(forest.getId(), swamp.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player1, List.of(swamp.getId()));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(swamp);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, plains);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void setupTopCards(List<Card> cards) {
        harness.setLibrary(player1, cards);
    }

    private void castAndResolveEtb() {
        harness.setHand(player1, List.of(new EclipsedElf()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
