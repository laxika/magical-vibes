package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AugurOfSkulls;
import com.github.laxika.magicalvibes.cards.p.PactOfTheTitan;
import com.github.laxika.magicalvibes.cards.w.Whetwheel;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Nihilith.class, PactOfTheTitan.class, AugurOfSkulls.class, Whetwheel.class})
class NihilithTest extends BaseCardTest {

    @Test
    @DisplayName("Suspend exiles Nihilith with seven time counters")
    void suspendExilesWithSevenTimeCounters() {
        Nihilith card = suspendCard();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 7);
    }

    @Test
    @DisplayName("An opponent's card entering their graveyard offers to remove a time counter")
    void opponentCardEnteringGraveyardOffersTimeCounterRemoval() {
        Nihilith card = suspendCard();

        castOpponentPact();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 6);
    }

    @Test
    @DisplayName("Declining Nihilith's graveyard trigger leaves its time counters unchanged")
    void decliningTriggerLeavesTimeCountersUnchanged() {
        Nihilith card = suspendCard();

        castOpponentPact();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 7);
    }

    @Test
    @DisplayName("A card entering its controller's own graveyard does not trigger Nihilith")
    void ownCardEnteringGraveyardDoesNotTrigger() {
        Nihilith card = suspendCard();

        castOwnPact();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 7);
    }

    @Test
    void ownUpkeepRemovesOneCounter() {
        Nihilith card = suspendCard();
        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 6);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void opponentUpkeepDoesNotRemoveCounter() {
        Nihilith card = suspendCard();
        advanceToUpkeep(player2);
        resolveAllTriggers();
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 7);
    }

    @Test
    void lastCounterFromGraveyardTriggerOffersSeparateFreeHastyCast() {
        Nihilith card = suspendCard();
        gd.exiledCardTimeCounters.put(card.getId(), 1);
        castOpponentPact();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.withAutoStop(gd.currentStep, () -> harness.passBothPriorities());

        assertThat(findPermanent(player1, "Nihilith").getCard()).isSameAs(card);
        assertThat(findPermanent(player1, "Nihilith").hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);
    }

    @Test
    void decliningLastCounterCastStopsGraveyardTriggers() {
        Nihilith card = suspendCard();
        gd.exiledCardTimeCounters.put(card.getId(), 1);
        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());

        castOpponentPact();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentGraveyardDoesNotTriggerNihilithOnBattlefield() {
        harness.addToBattlefield(player1, new Nihilith());
        castOpponentPact();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentSacrificeTriggersBeforeSacrificedCreaturesAbilityResolves() {
        Nihilith card = suspendCard();
        harness.addToBattlefield(player2, new AugurOfSkulls());
        harness.setHand(player1, List.of());
        advanceToUpkeep(player2);

        harness.activateAbility(player2, 0, 1, null, player1.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 6);
        assertThat(gd.stack).hasSize(1);
        harness.assertInGraveyard(player2, "Augur of Skulls");
    }

    @Test
    void eachDiscardedCardOffersItsOwnCounterRemoval() {
        Nihilith card = suspendCard();
        harness.addToBattlefield(player1, new AugurOfSkulls());
        harness.setHand(player2, List.of(new PactOfTheTitan(), new PactOfTheTitan()));
        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 5);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 5);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    void suspendCannotBeUsedDuringOpponentsTurnWithoutFlash() {
        Nihilith card = new Nihilith();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).contains(card);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);
    }

    @Test
    void millingOpponentLibraryTriggersForEachCard() {
        Nihilith card = suspendCard();
        harness.addToBattlefield(player1, new Whetwheel());
        harness.setLibrary(player2, List.of(new PactOfTheTitan(), new AugurOfSkulls()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 5);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    void queuedGraveyardTriggerDoesNothingAfterLastCounterIsRemoved() {
        Nihilith card = suspendCard();
        gd.exiledCardTimeCounters.put(card.getId(), 1);
        harness.addToBattlefield(player1, new Whetwheel());
        harness.setLibrary(player2, List.of(new PactOfTheTitan(), new AugurOfSkulls()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 2, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
    }

    @Test
    void opponentTokenDyingDoesNotTriggerCounterRemoval() {
        Nihilith card = suspendCard();
        castOpponentPact();
        harness.handleMayAbilityChosen(player1, true);
        findPermanent(player2, "Giant").setMarkedDamage(4);

        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 6);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Giant");
    }

    @Test
    void fearRejectsNonartifactNonblackBlocker() {
        harness.castFromHand(player2, new PactOfTheTitan(), "{0}");
        resolveAllTriggers();
        var blocker = findPermanent(player2, "Giant");
        var attacker = addCreatureReady(player1, new Nihilith());
        attacker.setAttacking(true);
        prepareDeclareBlockers();
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void fearAllowsBlackCreatureToBlock() {
        var blocker = addCreatureReady(player2, new AugurOfSkulls());
        var attacker = addCreatureReady(player1, new Nihilith());
        attacker.setAttacking(true);
        prepareDeclareBlockers();
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.getBlockingTargets()).contains(attackerIndex);
    }

    private Nihilith suspendCard() {
        Nihilith card = new Nihilith();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, null);
        return card;
    }

    private void castOpponentPact() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new PactOfTheTitan(), "{0}");
        resolveAllTriggers();
    }

    private void castOwnPact() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new PactOfTheTitan(), "{0}");
        resolveAllTriggers();
    }
}
