package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PrideOfTheRoadTest extends BaseCardTest {

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    void grantsDoubleStrikeToTargetCreatureAtMaxSpeed() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new PrideOfTheRoad());
        gd.playerSpeeds.put(player1.getId(), 4);

        advanceToCombat(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void grantsDoubleStrikeToTargetVehicleAtMaxSpeed() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new DuskLegionDreadnought());
        harness.addToBattlefield(player1, new PrideOfTheRoad());
        gd.playerSpeeds.put(player1.getId(), 4);

        advanceToCombat(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, vehicle.getId());
        harness.passBothPriorities();

        assertThat(vehicle.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void doesNotTriggerWithoutMaxSpeed() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new PrideOfTheRoad());
        gd.playerSpeeds.put(player1.getId(), 1);

        advanceToCombat(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction())
                .isNotInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(creature.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void cannotTargetCreatureAnOpponentControls() {
        harness.addToBattlefield(player1, new PrideOfTheRoad());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        gd.playerSpeeds.put(player1.getId(), 4);

        advanceToCombat(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .doesNotContain(opponentCreature.getId());
    }

    @Test
    void doubleStrikeWearsOffAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new PrideOfTheRoad());
        gd.playerSpeeds.put(player1.getId(), 4);

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        assertThat(creature.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
    }
}
