package com.github.laxika.magicalvibes.cards.c;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.d.DurkwoodBaloth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({CurseOfTheCabal.class, DurkwoodBaloth.class})
class CurseOfTheCabalTest extends BaseCardTest {

    @Test
    @DisplayName("Suspend exiles Curse of the Cabal with two time counters")
    void suspendExilesWithTwoTimeCounters() {
        CurseOfTheCabal card = suspendCard();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 2);
    }

    @Test
    @DisplayName("Curse of the Cabal sacrifices half the target player's permanents rounded down")
    void sacrificesHalfTargetPermanentsRoundedDown() {
        Permanent first = addCreatureReady(player2, new DurkwoodBaloth());
        addCreatureReady(player2, new DurkwoodBaloth());
        addCreatureReady(player2, new DurkwoodBaloth());
        CurseOfTheCabal card = new CurseOfTheCabal();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 9);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        harness.handleMultiplePermanentsChosen(player2, List.of(first.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The active player may decline the suspended upkeep trigger")
    void activePlayerMayDeclineSuspendedUpkeepTrigger() {
        CurseOfTheCabal card = suspendCard();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 2);
    }

    @Test
    @DisplayName("Accepting the suspended upkeep trigger without a permanent does not add counters")
    void acceptingSuspendedUpkeepTriggerWithoutPermanentDoesNotAddCounters() {
        CurseOfTheCabal card = suspendCard();

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 2);
    }

    @Test
    @DisplayName("Accepting the suspended upkeep trigger sacrifices a permanent and adds two counters")
    void acceptingSuspendedUpkeepTriggerAddsCountersAfterSacrifice() {
        CurseOfTheCabal card = suspendCard();
        Permanent sacrificed = addCreatureReady(player2, new DurkwoodBaloth());
        addCreatureReady(player2, new DurkwoodBaloth());

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        harness.handleMultiplePermanentsChosen(player2, List.of(sacrificed.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
    }

    @Test
    void ownerMaySacrificeDuringOwnUpkeep() {
        CurseOfTheCabal card = suspendCard();
        addCreatureReady(player1, new DurkwoodBaloth());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 3);
    }

    @Test
    void decliningOwnUpkeepSacrificeStillRemovesTimeCounter() {
        CurseOfTheCabal card = suspendCard();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 1);
    }

    @Test
    void upkeepTriggerDoesNothingWhenCardIsNoLongerSuspended() {
        CurseOfTheCabal card = suspendCard();
        Permanent permanent = addCreatureReady(player2, new DurkwoodBaloth());
        advanceToUpkeep(player2);
        gd.exiledCardTimeCounters.remove(card.getId());

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(permanent);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
    }

    @Test
    void targetWithOnePermanentSacrificesNothing() {
        Permanent permanent = addCreatureReady(player2, new DurkwoodBaloth());
        harness.setHand(player1, List.of(new CurseOfTheCabal()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 9);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(permanent);
    }

    @Test
    void mayTargetSelfAndCountsPermanentsAtResolution() {
        Permanent first = addCreatureReady(player1, new DurkwoodBaloth());
        harness.setHand(player1, List.of(new CurseOfTheCabal()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 9);
        harness.castSorcery(player1, 0, player1.getId());
        Permanent second = addCreatureReady(player1, new DurkwoodBaloth());
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(second);
    }

    @Test
    void decliningLastCounterCastLeavesCardExiledAndStopsUpkeepAbility() {
        CurseOfTheCabal card = suspendCard();
        for (int i = 0; i < 2; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, false);
            harness.passBothPriorities();
        }
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void lastCounterCastTriggerWaitsForAnotherPriorityRound() {
        CurseOfTheCabal card = suspendCard();
        for (int i = 0; i < 2; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, false);
            harness.passBothPriorities();
        }

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
    }

    private CurseOfTheCabal suspendCard() {
        CurseOfTheCabal card = new CurseOfTheCabal();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateHandAbility(player1, 0, null);
        return card;
    }
}
