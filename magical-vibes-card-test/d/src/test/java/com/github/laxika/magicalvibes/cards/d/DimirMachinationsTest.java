package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DimirMachinations.class, DriftOfPhantasms.class, Forest.class, Island.class, Mountain.class})
class DimirMachinationsTest extends BaseCardTest {

    @Test
    void looksAtTargetPlayersTopThreeExilesAnyNumberAndReordersTheRest() {
        Card top = new Island();
        Card second = new Forest();
        Card third = new Mountain();
        Card fourth = new Island();
        harness.setHand(player1, List.of(new DimirMachinations()));
        harness.setLibrary(player2, List.of(top, second, third, fourth));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().targetPlayerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player1, 1);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.getPlayerExiledCards(player2.getId())).extracting(Card::getId).containsExactly(second.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerDecks.get(player2.getId())).extracting(Card::getId)
                .containsExactly(third.getId(), top.getId(), fourth.getId());
    }

    @Test
    void canExileNoCardsAndReordersAllThree() {
        Card top = new Island();
        Card second = new Forest();
        Card third = new Mountain();
        harness.setHand(player1, List.of(new DimirMachinations()));
        harness.setLibrary(player2, List.of(top, second, third));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, -1);

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 0, 1)));

        assertThat(gd.playerDecks.get(player2.getId())).extracting(Card::getId)
                .containsExactly(third.getId(), top.getId(), second.getId());
    }

    @Test
    void transmuteSearchesForTheSameManaValue() {
        DimirMachinations machinations = new DimirMachinations();
        DriftOfPhantasms matchingCard = new DriftOfPhantasms();
        Island differentManaValue = new Island();
        harness.setHand(player1, List.of(machinations));
        harness.setLibrary(player1, List.of(matchingCard, differentManaValue));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(matchingCard);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Dimir Machinations");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(matchingCard);
    }

    @Test
    void transmuteCanOnlyBeActivatedDuringYourMainPhase() {
        DimirMachinations machinations = new DimirMachinations();
        harness.setHand(player1, List.of(machinations));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed during your main phase");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(machinations);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }
    @Test
    void canExileAllThreeWithoutTouchingTheFourthCard() {
        Card top = new Island();
        Card second = new Forest();
        Card third = new Mountain();
        Card fourth = new Island();
        harness.setHand(player1, List.of(new DimirMachinations()));
        harness.setLibrary(player2, List.of(top, second, third, fourth));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(top, second, third);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(fourth);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Dimir Machinations");
    }

    @Test
    void canTargetYourOwnLibraryAndReturnTheSingleUnexiledCard() {
        Card top = new Island();
        Card second = new Forest();
        harness.setHand(player1, List.of(new DimirMachinations()));
        harness.setLibrary(player1, List.of(top, second));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(top);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void resolvesAgainstAnEmptyLibraryWithoutAChoice() {
        harness.setHand(player1, List.of(new DimirMachinations()));
        harness.setLibrary(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Dimir Machinations");
    }

    @Test
    void transmuteCanFailToFindEvenWhenAMatchingCardExists() {
        DimirMachinations machinations = new DimirMachinations();
        Card matchingCard = new DriftOfPhantasms();
        Card land = new Island();
        harness.setHand(player1, List.of(machinations));
        harness.setLibrary(player1, List.of(matchingCard, land));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Dimir Machinations");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(matchingCard, land);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void transmuteCannotBeActivatedWithASpellOnTheStack() {
        DimirMachinations machinations = new DimirMachinations();
        harness.setHand(player1, List.of(new DimirMachinations(), machinations));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(machinations);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        assertThat(gd.stack).hasSize(1);
    }
}
