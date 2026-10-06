package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HiddenCataract;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SinuousBenthisaur.class, Plains.class, Forest.class, HiddenCataract.class})
class SinuousBenthisaurTest extends BaseCardTest {

    @Test
    @DisplayName("Counts controlled Caves and Cave cards in the controller's graveyard")
    void countsControlledCavesAndCaveCardsInGraveyard() {
        Card battlefieldCave = cave();
        Card opponentCave = cave();
        Card graveyardCave = cave();
        Card topOne = new Forest();
        Card topTwo = new Plains();
        Card topThree = new Forest();

        harness.addToBattlefield(player1, battlefieldCave);
        harness.addToBattlefield(player2, opponentCave);
        harness.setGraveyard(player1, List.of(graveyardCave));
        harness.setLibrary(player1, List.of(topOne, topTwo, topThree));
        castSinuousBenthisaur();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(topOne, topTwo);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topThree);
    }

    @Test
    @DisplayName("Puts the unchosen cards on the bottom in a random order")
    void putsUnchosenCardsOnBottomRandomly() {
        Card firstCave = cave();
        Card secondCave = cave();
        Card graveyardCave = cave();
        Card topOne = new Forest();
        Card topTwo = new Plains();
        Card topThree = new Forest();
        Card topFour = new Plains();
        Card bottomCard = new Forest();

        harness.addToBattlefield(player1, firstCave);
        harness.addToBattlefield(player1, secondCave);
        harness.setGraveyard(player1, List.of(graveyardCave));
        harness.setLibrary(player1, List.of(topOne, topTwo, topThree, topFour, bottomCard));
        castSinuousBenthisaur();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(topOne.getId(), topTwo.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(topOne, topTwo);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(topThree, topFour, bottomCard);
    }

    @Test
    void noCavesLeavesLibraryUnchangedAndIgnoresOpponentsGraveyard() {
        Card first = new Forest();
        Card second = new Plains();
        harness.addToBattlefield(player2, new HiddenCataract());
        harness.setGraveyard(player2, List.of(new HiddenCataract()));
        harness.setGraveyard(player1, List.of(new Plains()));
        harness.setLibrary(player1, List.of(first, second));

        castSinuousBenthisaur();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void oneCavePutsOnlyOneCardIntoHand() {
        Card first = new Forest();
        Card second = new Plains();
        harness.addToBattlefield(player1, new HiddenCataract());
        harness.setLibrary(player1, List.of(first, second));

        castSinuousBenthisaur();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void fewerLibraryCardsThanCavesPutsAvailableCardIntoHand() {
        Card onlyCard = new Forest();
        harness.setGraveyard(player1, List.of(new HiddenCataract(), new HiddenCataract(), new HiddenCataract()));
        harness.setLibrary(player1, List.of(onlyCard));

        castSinuousBenthisaur();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyLibraryDoesNotRequireAChoice() {
        harness.addToBattlefield(player1, new HiddenCataract());
        harness.setLibrary(player1, List.of());

        castSinuousBenthisaur();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void countsCavesWhenTriggerResolves() {
        Card first = new Forest();
        Card second = new Plains();
        Card third = new Forest();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new SinuousBenthisaur()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.setGraveyard(player1, List.of(new HiddenCataract(), new HiddenCataract()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
    }

    @Test
    void mustChooseExactlyTwoAndKeepsUnlookedCardsAboveBottomedCards() {
        Card first = new Forest();
        Card second = new Plains();
        Card third = new Forest();
        Card fourth = new Plains();
        Card untouched = new Forest();
        harness.setGraveyard(player1, List.of(new HiddenCataract(), new HiddenCataract(),
                new HiddenCataract(), new HiddenCataract()));
        harness.setLibrary(player1, List.of(first, second, third, fourth, untouched));
        castSinuousBenthisaur();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(first.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(second.getId(), fourth.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(second, fourth);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 3)).containsExactlyInAnyOrder(first, third);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Card cave() {
        Card cave = new Plains().createRuntimeCopy();
        cave.setSubtypes(List.of(CardSubtype.CAVE));
        return cave;
    }

    private void castSinuousBenthisaur() {
        harness.setHand(player1, List.of(new SinuousBenthisaur()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
