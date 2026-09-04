package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.t.Tremor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedLoseLifeAndReturnFromGraveyard;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BroodOfCockroaches.class, Tremor.class})
class BroodOfCockroachesTest extends BaseCardTest {

    private void castTremorFromPlayer2() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new Tremor(), "{R}");
    }

    @Test
    @DisplayName("Death registers a delayed trigger; no return or life loss yet")
    void deathRegistersDelayedTrigger() {
        Permanent brood = harness.addToBattlefieldAndReturn(player1, new BroodOfCockroaches());
        UUID broodId = brood.getCard().getId();
        int lifeBefore = gd.getLife(player1.getId());

        castTremorFromPlayer2();
        harness.passBothPriorities(); // Tremor resolves — Brood dies
        harness.passBothPriorities(); // death trigger registers delayed effect

        harness.assertInGraveyard(player1, "Brood of Cockroaches");
        assertThat(gd.getDelayedActions(DelayedLoseLifeAndReturnFromGraveyard.class)).hasSize(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(broodId));
    }

    @Test
    @DisplayName("At next end step, controller loses 1 life and Brood returns to hand")
    void losesLifeAndReturnsAtNextEndStep() {
        Permanent brood = harness.addToBattlefieldAndReturn(player1, new BroodOfCockroaches());
        UUID broodId = brood.getCard().getId();
        int lifeBefore = gd.getLife(player1.getId());

        castTremorFromPlayer2();
        harness.passBothPriorities(); // Tremor resolves
        harness.passBothPriorities(); // register delayed

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        assertThat(gd.stack).isNotEmpty();
        harness.passBothPriorities(); // resolve delayed trigger

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(broodId));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(broodId));
        assertThat(gd.getDelayedActions(DelayedLoseLifeAndReturnFromGraveyard.class)).isEmpty();
    }

    @Test
    @DisplayName("Life loss still happens when Brood leaves the graveyard before the delayed trigger resolves")
    void losesLifeWhenCardLeavesGraveyardBeforeDelayedTriggerResolves() {
        Permanent brood = harness.addToBattlefieldAndReturn(player1, new BroodOfCockroaches());
        UUID broodId = brood.getCard().getId();
        int lifeBefore = gd.getLife(player1.getId());

        castTremorFromPlayer2();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(brood.getCard()));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        assertThat(gd.stack).isNotEmpty();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(c -> c.getId().equals(broodId))
                .hasSize(1);
        assertThat(gd.getDelayedActions(DelayedLoseLifeAndReturnFromGraveyard.class)).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when an opponent controls Brood and it goes to its owner's graveyard")
    void doesNotTriggerWhenControlledByOpponent() {
        BroodOfCockroaches broodCard = new BroodOfCockroaches();
        broodCard.setOwnerId(player1.getId());
        harness.addToBattlefieldAndReturn(player2, broodCard);
        int lifeBefore = gd.getLife(player2.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new Tremor(), "{R}");
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Brood of Cockroaches");
        harness.assertNotInGraveyard(player2, "Brood of Cockroaches");
        assertThat(gd.getDelayedActions(DelayedLoseLifeAndReturnFromGraveyard.class)).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
    }
}
