package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BarkformHarvester;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.d.DazzlingDenial;
import com.github.laxika.magicalvibes.cards.z.RoostOfDrakes;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThundertrapTrainer.class, DazzlingDenial.class, BarkformHarvester.class, Plains.class})
class ThundertrapTrainerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers only noncreature, nonland cards among the top four")
    void etbOffersOnlyNoncreatureNonlands() {
        Card firstDazzlingDenial = new DazzlingDenial();
        Card secondDazzlingDenial = new DazzlingDenial();
        setupTopCards(List.of(firstDazzlingDenial, new BarkformHarvester(), new Plains(), secondDazzlingDenial));
        castAndResolveEtb();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).hasSize(4);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(firstDazzlingDenial.getId(), secondDazzlingDenial.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Revealing a card puts it into hand and bottoms the rest")
    void revealingPutsCardIntoHand() {
        Card denial = new DazzlingDenial();
        setupTopCards(List.of(denial, new BarkformHarvester(), new Plains(), new BarkformHarvester()));
        castAndResolveEtb();

        harness.handleMultipleCardsChosen(player1, List.of(denial.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(denial);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3).doesNotContain(denial);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining puts all four cards on the bottom")
    void decliningBottomsEverything() {
        Card denial = new DazzlingDenial();
        setupTopCards(List.of(denial, new BarkformHarvester(), new Plains(), new BarkformHarvester()));
        castAndResolveEtb();

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(denial);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With no eligible card among the top four no choice is needed")
    void noEligibleCardNeedsNoChoice() {
        setupTopCards(List.of(new BarkformHarvester(), new Plains(), new BarkformHarvester(), new Plains()));
        castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
    }

    @Test
    void offspringCreatesOneOneTokenCopyWhenPaid() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new ThundertrapTrainer()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getEffectivePower()).isEqualTo(1);
        assertThat(tokens.getFirst().getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    void shortLibraryStillAllowsChoosingAnEligibleCard() {
        Card denial = new DazzlingDenial();
        setupTopCards(List.of(new Plains(), denial));
        castAndResolveEtb();

        harness.handleMultipleCardsChosen(player1, List.of(denial.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(denial);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void unexaminedCardsRemainAboveBottomedCards() {
        Card denial = new DazzlingDenial();
        Card untouched = new Plains();
        List<Card> examined = List.of(denial, new Plains(), new BarkformHarvester(), new Plains());
        harness.setLibrary(player1, List.of(examined.get(0), examined.get(1),
                examined.get(2), examined.get(3), untouched));
        castAndResolveEtb();

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 5))
                .containsExactlyInAnyOrderElementsOf(examined);
    }

    @Test
    void unpaidOffspringCreatesNoToken() {
        harness.setLibrary(player1, List.of());
        castAndResolveEtb();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getCard().isToken()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void offspringTokenAlsoLooksAtFourCards() {
        Card denial = new DazzlingDenial();
        harness.setLibrary(player1, List.of(new Plains(), new Plains(), new Plains(), new Plains(), denial));
        harness.setHand(player1, List.of(new ThundertrapTrainer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(denial.getId());
        harness.handleMultipleCardsChosen(player1, List.of(denial.getId()));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(denial);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed(RoostOfDrakes.class)
    void payingOffspringDoesNotCountAsKickingASpell() {
        harness.addToBattlefield(player1, new RoostOfDrakes());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new ThundertrapTrainer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castKickedCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void paidOffspringAndLibrarySelectionAreSeparateTriggers() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new ThundertrapTrainer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
    }

    private void setupTopCards(List<Card> cards) {
        harness.setLibrary(player1, cards);
    }

    private void castAndResolveEtb() {
        harness.setHand(player1, List.of(new ThundertrapTrainer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
