package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.cards.t.Timecrafting;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WatcherOfHours.class, SwordsToPlowshares.class, Timecrafting.class})
class WatcherOfHoursTest extends BaseCardTest {

    @Test
    @DisplayName("Suspend exiles Watcher of Hours with six time counters")
    void suspendExilesWithSixTimeCounters() {
        WatcherOfHours card = suspendCard();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 6);
    }

    @Test
    @DisplayName("Surveils 1 whenever a time counter is removed while suspended")
    void surveilsWhenTimeCounterIsRemoved() {
        WatcherOfHours card = suspendCard();
        WatcherOfHours topCard = new WatcherOfHours();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 5);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    void surveilCanPutTheTopCardIntoTheGraveyard() {
        suspendCard();
        WatcherOfHours topCard = new WatcherOfHours();
        WatcherOfHours nextCard = new WatcherOfHours();
        harness.setLibrary(player1, List.of(topCard, nextCard));

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void opponentsUpkeepDoesNotRemoveACounter() {
        WatcherOfHours card = suspendCard();

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 6);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void surveilWithAnEmptyLibraryDoesNotRequireAChoice() {
        WatcherOfHours card = suspendCard();
        harness.setLibrary(player1, List.of());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 5);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void removingMultipleCountersSurveilsOnceForEachCounter() {
        WatcherOfHours card = suspendCard();
        WatcherOfHours firstCard = new WatcherOfHours();
        WatcherOfHours secondCard = new WatcherOfHours();
        WatcherOfHours thirdCard = new WatcherOfHours();
        harness.setLibrary(player1, List.of(firstCard, secondCard, thirdCard));

        removeCounters(player1, card, 2);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(thirdCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firstCard, secondCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentRemovingATimeCounterDoesNotTriggerSurveil() {
        WatcherOfHours card = suspendCard();
        WatcherOfHours topCard = new WatcherOfHours();
        harness.setLibrary(player1, List.of(topCard));

        removeCounters(player2, card, 1);

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 5);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void lastCounterOffersAFreeCastAndTheCreatureCanAttackImmediately() {
        WatcherOfHours card = suspendCard();
        harness.setLibrary(player1, List.of());
        gd.exiledCardTimeCounters.put(card.getId(), 1);

        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        harness.assertOnBattlefield(player1, "Watcher of Hours");
        declareAttackersAndPrepareBlockers(List.of(0));
        assertThat(findPermanent(player1, "Watcher of Hours").isAttacking()).isTrue();
    }

    @Test
    void lastCounterCreatesSeparateSurveilAndCastTriggers() {
        WatcherOfHours card = suspendCard();
        harness.setLibrary(player1, List.of(new WatcherOfHours()));
        gd.exiledCardTimeCounters.put(card.getId(), 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void decliningTheLastCounterCastLeavesTheCardExiledWithoutCounters() {
        WatcherOfHours card = suspendCard();
        harness.setLibrary(player1, List.of());
        gd.exiledCardTimeCounters.put(card.getId(), 1);

        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        harness.assertNotOnBattlefield(player1, "Watcher of Hours");
        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotSuspendDuringAnOpponentsTurn() {
        WatcherOfHours card = new WatcherOfHours();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void wardCountersAnOpponentSpellWhenTheyCannotPayThree() {
        var watcher = harness.addToBattlefieldAndReturn(player1, new WatcherOfHours());
        harness.setHand(player2, List.of(new SwordsToPlowshares()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castInstant(player2, 0, watcher.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Watcher of Hours");
        harness.assertInGraveyard(player2, "Swords to Plowshares");
        harness.assertLife(player1, 20);
    }

    private void removeCounters(Player player, WatcherOfHours card, int count) {
        harness.setHand(player, List.of(new Timecrafting()));
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.COLORLESS, count);
        harness.castModalInstantForX(player, 0, 0, count, card.getId());
        harness.passBothPriorities();
    }

    private WatcherOfHours suspendCard() {
        WatcherOfHours card = new WatcherOfHours();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, null);
        return card;
    }
}
