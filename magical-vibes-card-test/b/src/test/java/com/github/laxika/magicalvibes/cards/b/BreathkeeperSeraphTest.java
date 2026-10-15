package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.Resurrection;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BreathkeeperSeraph.class, GrizzlyBears.class, Resurrection.class})
class BreathkeeperSeraphTest extends BaseCardTest {

    @Test
    @DisplayName("While paired, the partner may return at its controller's next upkeep")
    void pairedPartnerMayReturnAtNextUpkeep() {
        Permanent bears = castAndPairWithBears();

        bears.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Grizzly Bears");

        runUpkeepOf(player1);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("While paired, Breathkeeper Seraph itself may return at its next upkeep")
    void pairedSeraphMayReturnAtNextUpkeep() {
        castAndPairWithBears();
        Permanent seraph = findPermanent(player1, "Breathkeeper Seraph");

        seraph.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        runUpkeepOf(player1);

        harness.assertOnBattlefield(player1, "Breathkeeper Seraph");
        harness.assertNotInGraveyard(player1, "Breathkeeper Seraph");
    }

    @Test
    @DisplayName("Declining the death trigger leaves the creature in the graveyard")
    void decliningReturnLeavesCreatureInGraveyard() {
        Permanent bears = castAndPairWithBears();

        bears.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        runUpkeepOf(player1);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("An unpaired Breathkeeper Seraph grants no death return")
    void unpairedSeraphGrantsNoDeathReturn() {
        Permanent seraph = harness.addToBattlefieldAndReturn(player1, new BreathkeeperSeraph());
        seraph.setMarkedDamage(4);
        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Breathkeeper Seraph");
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    void stolenPartnerReturnsAtDeathControllersUpkeepUnderOwnersControl() {
        Permanent bears = castAndPairWithBears();
        gd.stolenCreatures.put(bears.getId(), player2.getId());
        bears.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        runUpkeepOf(player1);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void stolenPartnerDoesNotReturnAtOwnersEarlierUpkeep() {
        Permanent bears = castAndPairWithBears();
        gd.stolenCreatures.put(bears.getId(), player2.getId());
        bears.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        runUpkeepOf(player2);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void simultaneousDeathsTriggerForBothCreatures() {
        Permanent bears = castAndPairWithBears();
        Permanent seraph = findPermanent(player1, "Breathkeeper Seraph");
        bears.setMarkedDamage(2);
        seraph.setMarkedDamage(4);
        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Breathkeeper Seraph");
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    void delayedReturnDoesNotFollowCardThatLeftAndReenteredGraveyard() {
        Permanent bears = castAndPairWithBears();
        bears.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, java.util.List.of(new Resurrection()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castSorcery(player1, 0, bears.getCard().getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        Permanent returnedBears = findPermanent(player1, "Grizzly Bears");
        returnedBears.setMarkedDamage(2);
        harness.runStateBasedActions();

        runUpkeepOf(player1);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void unpairedSeraphCanPairWithAnotherEnteringCreature() {
        Permanent seraph = harness.addToBattlefieldAndReturn(player1, new BreathkeeperSeraph());
        harness.setHand(player1, java.util.List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(seraph.getPairedWithId()).isEqualTo(bears.getId());
        assertThat(bears.getPairedWithId()).isEqualTo(seraph.getId());
        bears.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        runUpkeepOf(player1);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    private Permanent castAndPairWithBears() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, java.util.List.of(new BreathkeeperSeraph()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.WHITE, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        return bears;
    }

    private void runUpkeepOf(Player player) {
        advanceToUpkeep(player);
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
