package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.cards.b.BorosSignet;
import com.github.laxika.magicalvibes.cards.l.LoreBroker;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShredMemory.class, BorosRecruit.class, BorosSignet.class, LoreBroker.class})
class ShredMemoryTest extends BaseCardTest {

    @Test
    void exilesUpToFourCardsFromOneGraveyard() {
        Card first = new BorosSignet();
        Card second = new BorosRecruit();
        Card third = new BorosRecruit();
        Card fourth = new BorosSignet();
        harness.setGraveyard(player2, List.of(first, second, third, fourth));
        harness.setHand(player1, List.of(new ShredMemory()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1,
                List.of(first.getId(), second.getId(), third.getId(), fourth.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(first.getId(), second.getId(), third.getId(), fourth.getId());
    }

    @Test
    void canChooseFewerThanFourCards() {
        Card chosen = new BorosRecruit();
        Card remaining = new BorosSignet();
        harness.setGraveyard(player1, List.of(chosen, remaining));
        Card shredMemory = new ShredMemory();
        harness.setHand(player1, List.of(shredMemory));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(remaining, shredMemory);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(chosen.getId());
    }

    @Test
    void canChooseNoCards() {
        Card remaining = new BorosRecruit();
        ShredMemory shredMemory = new ShredMemory();
        harness.setGraveyard(player1, List.of(remaining));
        harness.setHand(player1, List.of(shredMemory));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(remaining, shredMemory);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void cannotChooseCardsFromTwoGraveyards() {
        Card ownCard = new BorosSignet();
        Card opposingCard = new BorosRecruit();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opposingCard));
        harness.setHand(player1, List.of(new ShredMemory()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(ownCard.getId(), opposingCard.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("single graveyard");
    }

    @Test
    void transmuteSearchesForTheSameManaValue() {
        ShredMemory shredMemory = new ShredMemory();
        BorosSignet matchingCard = new BorosSignet();
        BorosRecruit differentManaValue = new BorosRecruit();
        harness.setHand(player1, List.of(shredMemory));
        harness.setLibrary(player1, List.of(matchingCard, differentManaValue));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(matchingCard);
        assertThat(search.params().reveals()).isTrue();

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Shred Memory");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(matchingCard);
    }

    @Test
    void transmuteShufflesWithoutFindingAMatchingCard() {
        ShredMemory shredMemory = new ShredMemory();
        BorosRecruit nonMatchingCard = new BorosRecruit();
        harness.setHand(player1, List.of(shredMemory));
        harness.setLibrary(player1, List.of(nonMatchingCard));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Shred Memory");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonMatchingCard);
    }

    @Test
    void transmuteCanOnlyBeActivatedAtSorcerySpeed() {
        ShredMemory shredMemory = new ShredMemory();
        harness.setHand(player1, List.of(shredMemory));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(shredMemory);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    void transmuteUsesTheSourceManaValueAfterAnotherCardIsDiscardedInResponse() {
        ShredMemory shredMemory = new ShredMemory();
        BorosSignet matchingCard = new BorosSignet();
        BorosRecruit drawnAndDiscardedCard = new BorosRecruit();
        Permanent broker = addCreatureReady(player2, new LoreBroker());

        harness.setHand(player1, List.of(shredMemory));
        harness.setLibrary(player1, List.of(drawnAndDiscardedCard, matchingCard));
        harness.setHand(player2, List.of(new BorosRecruit()));
        harness.setLibrary(player2, List.of(new BorosRecruit()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(broker), null, null);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search)
                .as("the transmute search should still use Shred Memory's mana value after a response")
                .isNotNull();
        assertThat(search.params().cards()).containsExactly(matchingCard);

        harness.handleCardChosen(player1, 0);
        harness.assertInGraveyard(player1, "Shred Memory");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(matchingCard);
    }

    @Test
    void canBeCastWithEmptyGraveyards() {
        ShredMemory shredMemory = new ShredMemory();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        harness.setHand(player1, List.of(shredMemory));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(shredMemory);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void exilesRemainingLegalTargetAfterAnotherTargetIsExiledInResponse() {
        Card removedInResponse = new BorosRecruit();
        Card remainingTarget = new BorosSignet();
        harness.setGraveyard(player2, List.of(removedInResponse, remainingTarget));
        harness.setHand(player1, List.of(new ShredMemory()));
        harness.setHand(player2, List.of(new ShredMemory()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1,
                List.of(removedInResponse.getId(), remainingTarget.getId()));
        harness.castInstant(player2, 0);
        harness.handleMultipleCardsChosen(player2, List.of(removedInResponse.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(removedInResponse);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(remainingTarget);

        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(removedInResponse, remainingTarget);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(remainingTarget);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void transmuteMayFailToFindEvenWhenAMatchingCardExists() {
        ShredMemory shredMemory = new ShredMemory();
        Card matchingCard = new BorosSignet();
        harness.setHand(player1, List.of(shredMemory));
        harness.setLibrary(player1, List.of(matchingCard));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(shredMemory);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(matchingCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }
    @Test
    void transmuteStillSearchesAfterItsSourceIsExiledInResponse() {
        ShredMemory shredMemory = new ShredMemory();
        Card matchingCard = new BorosSignet();
        Card nonMatchingCard = new BorosRecruit();
        harness.setHand(player1, List.of(shredMemory));
        harness.setLibrary(player1, List.of(matchingCard, nonMatchingCard));
        harness.setHand(player2, List.of(new ShredMemory()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.castInstant(player2, 0);
        harness.handleMultipleCardsChosen(player2, List.of(shredMemory.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(shredMemory);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();

        harness.passBothPriorities();
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(matchingCard);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(matchingCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonMatchingCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(shredMemory);
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

}
