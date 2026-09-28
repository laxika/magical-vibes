package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BreathkeeperSeraph.class, GrizzlyBears.class})
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
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
