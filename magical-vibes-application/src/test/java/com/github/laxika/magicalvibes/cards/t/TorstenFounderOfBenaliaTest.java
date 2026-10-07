package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
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

@CardUsed({TorstenFounderOfBenalia.class, Forest.class, GrizzlyBears.class, Plains.class,
        Shock.class, WrathOfGod.class})
class TorstenFounderOfBenaliaTest extends BaseCardTest {

    @Test
    @DisplayName("ETB reveals seven and lets you put any number of creature and land cards into your hand")
    void entersAndPutsSelectedCreaturesAndLandsIntoHand() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card secondCreature = new GrizzlyBears();
        Card secondLand = new Plains();
        Card shock = new Shock();
        Card secondShock = new Shock();
        Card thirdShock = new Shock();
        harness.setLibrary(player1, List.of(creature, land, secondCreature, secondLand,
                shock, secondShock, thirdShock));

        castAndResolveTorsten();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                creature.getId(), land.getId(), secondCreature.getId(), secondLand.getId());
        assertThat(choice.maxCount()).isEqualTo(4);
        assertThat(choice.minCount()).isZero();
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId(), land.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(creature, land);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(secondCreature, secondLand, shock, secondShock, thirdShock);
    }

    @Test
    @DisplayName("When Torsten dies, seven Soldier tokens are created")
    void deathCreatesSevenSoldiers() {
        harness.addToBattlefield(player1, new TorstenFounderOfBenalia());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> soldiers = findPermanents(player1, "Soldier");
        assertThat(soldiers).hasSize(7);
        assertThat(soldiers).allSatisfy(soldier -> {
            assertThat(soldier.getCard().isToken()).isTrue();
            assertThat(soldier.getEffectivePower()).isEqualTo(1);
            assertThat(soldier.getEffectiveToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Torsten may put zero eligible cards into hand and bottoms only the top seven")
    void mayDeclineAllCardsAndPreservesUnrevealedLibraryOrder() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card secondLand = new Plains();
        Card shock = new Shock();
        Card secondShock = new Shock();
        Card thirdShock = new Shock();
        Card fourthShock = new Shock();
        Card eighthCard = new GrizzlyBears();
        Card ninthCard = new Plains();
        List<Card> revealed = List.of(creature, land, secondLand, shock,
                secondShock, thirdShock, fourthShock);
        harness.setLibrary(player1, List.of(creature, land, secondLand, shock,
                secondShock, thirdShock, fourthShock, eighthCard, ninthCard));

        castAndResolveTorsten();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                creature.getId(), land.getId(), secondLand.getId());
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(9);
        assertThat(library.subList(0, 2)).containsExactly(eighthCard, ninthCard);
        assertThat(library.subList(2, 9)).containsExactlyInAnyOrderElementsOf(revealed);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Torsten can put all seven eligible cards into hand")
    void mayChooseAllSevenEligibleCards() {
        List<Card> cards = List.of(new GrizzlyBears(), new Forest(), new Plains(),
                new GrizzlyBears(), new Forest(), new Plains(), new GrizzlyBears());
        harness.setLibrary(player1, cards);

        castAndResolveTorsten();
        harness.handleMultipleCardsChosen(player1, cards.stream().map(Card::getId).toList());

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrderElementsOf(cards);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Torsten reveals as many as possible from a short library")
    void shortLibraryStillAllowsChoosingSomeEligibleCards() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(creature, land, shock));

        castAndResolveTorsten();
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(creature, shock);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Torsten bottoms a library containing no eligible cards without a choice")
    void noEligibleCardsAreReturnedToLibrary() {
        List<Card> cards = List.of(new Shock(), new Shock(), new WrathOfGod());
        harness.setLibrary(player1, cards);

        castAndResolveTorsten();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(cards);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Torsten's enter trigger resolves harmlessly with an empty library")
    void emptyLibraryDoesNotRequireAChoice() {
        harness.setLibrary(player1, List.of());

        castAndResolveTorsten();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An opponent's Torsten creates Soldiers for that opponent after a board wipe")
    void deathCreatesTokensForTorstensController() {
        harness.addToBattlefield(player2, new TorstenFounderOfBenalia());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Soldier")).isEmpty();
        assertThat(findPermanents(player2, "Soldier")).hasSize(7);
    }

    private void castAndResolveTorsten() {
        harness.setHand(player1, List.of(new TorstenFounderOfBenalia()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
