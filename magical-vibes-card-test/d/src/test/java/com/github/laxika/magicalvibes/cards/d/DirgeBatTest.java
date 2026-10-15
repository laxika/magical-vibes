package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
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

@CardUsed({DirgeBat.class, GrizzlyBears.class, JaceBeleren.class})
class DirgeBatTest extends BaseCardTest {

    @Test
    void mutatingDestroysTargetCreatureAnOpponentControls() {
        Permanent bat = addCreatureReady(player1, new DirgeBat());
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());

        triggerMutation(bat);
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void mutatingDestroysTargetPlaneswalkerAnOpponentControls() {
        Permanent bat = addCreatureReady(player1, new DirgeBat());
        Permanent jace = harness.enterBattlefieldAndReturn(player2, new JaceBeleren());

        triggerMutation(bat);
        harness.handlePermanentChosen(player1, jace.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Jace Beleren");
        harness.assertInGraveyard(player2, "Jace Beleren");
    }

    @Test
    void mutatingCannotTargetOwnCreature() {
        Permanent bat = addCreatureReady(player1, new DirgeBat());
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        triggerMutation(bat);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownBear.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    void mutatingCannotTargetOwnPlaneswalker() {
        Permanent bat = addCreatureReady(player1, new DirgeBat());
        Permanent jace = harness.enterBattlefieldAndReturn(player1, new JaceBeleren());
        Permanent opposingBat = addCreatureReady(player2, new DirgeBat());

        triggerMutation(bat);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, jace.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
        harness.handlePermanentChosen(player1, opposingBat.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Jace Beleren");
        harness.assertNotOnBattlefield(player2, "Dirge Bat");
    }

    @Test
    void mutationWithoutAnOpposingCreatureOrPlaneswalkerDoesNotAwaitATarget() {
        Permanent bat = addCreatureReady(player1, new DirgeBat());

        triggerMutation(bat);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Dirge Bat");
    }

    @Test
    void targetThatChangesToTheAbilityControllersControlIsNotDestroyed() {
        Permanent bat = addCreatureReady(player1, new DirgeBat());
        Permanent opposingBat = addCreatureReady(player2, new DirgeBat());

        triggerMutation(bat);
        harness.handlePermanentChosen(player1, opposingBat.getId());
        gd.playerBattlefields.get(player2.getId()).remove(opposingBat);
        gd.playerBattlefields.get(player1.getId()).add(opposingBat);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bat, opposingBat);
        harness.assertNotInGraveyard(player1, "Dirge Bat");
        harness.assertNotInGraveyard(player2, "Dirge Bat");
    }

    @Test
    void destructionAbilityStillResolvesAfterItsSourceLeavesTheBattlefield() {
        Permanent bat = addCreatureReady(player1, new DirgeBat());
        Permanent opposingBat = addCreatureReady(player2, new DirgeBat());

        triggerMutation(bat);
        harness.handlePermanentChosen(player1, opposingBat.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bat);
        harness.setGraveyard(player1, List.of(bat.getCard()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Dirge Bat");
        harness.assertInGraveyard(player2, "Dirge Bat");
    }

    @Test
    void eachSubsequentMutationTriggersDestructionAgain() {
        Permanent bat = addCreatureReady(player1, new DirgeBat());
        Permanent firstTarget = addCreatureReady(player2, new DirgeBat());
        Permanent secondTarget = addCreatureReady(player2, new DirgeBat());

        triggerMutation(bat);
        harness.handlePermanentChosen(player1, firstTarget.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(secondTarget).doesNotContain(firstTarget);

        triggerMutation(bat);
        harness.handlePermanentChosen(player1, secondTarget.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Dirge Bat");
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    void normalCastingWorksDuringTheOpponentsUpkeepWithoutTriggeringDestruction() {
        addCreatureReady(player2, new DirgeBat());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new DirgeBat()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dirge Bat");
        harness.assertOnBattlefield(player2, "Dirge Bat");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canCastForMutateCostTargetingAnOwnedNonHumanCreature() {
        Permanent host = addCreatureReady(player1, new DirgeBat());
        harness.setHand(player1, List.of(new DirgeBat()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castWithAlternateCost(player1, 0, host.getId());

        assertThat(gd.stack).hasSize(1);
        harness.assertNotInHand(player1, "Dirge Bat");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    private void triggerMutation(Permanent bat) {
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, bat, List.of(bat.getCard()), player1.getId()));
        harness.inMutationScope(() -> harness.getTriggerCollectionService().processNextSelfTriggeredAbilityTarget(gd));
    }
}
