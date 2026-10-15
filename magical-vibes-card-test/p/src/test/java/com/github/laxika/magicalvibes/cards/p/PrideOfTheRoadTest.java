package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CloudspireSkycycle;
import com.github.laxika.magicalvibes.cards.m.MaraudingMako;
import com.github.laxika.magicalvibes.cards.s.SpikeshellHarrier;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrideOfTheRoad.class, MaraudingMako.class, CloudspireSkycycle.class, SpikeshellHarrier.class})
class PrideOfTheRoadTest extends BaseCardTest {

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
    }

    @Test
    void grantsDoubleStrikeToTargetCreatureAtMaxSpeed() {
        Permanent creature = addCreatureReady(player1, new MaraudingMako());
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
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new CloudspireSkycycle());
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
        Permanent creature = addCreatureReady(player1, new MaraudingMako());
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
        Permanent opponentCreature = addCreatureReady(player2, new MaraudingMako());
        gd.playerSpeeds.put(player1.getId(), 4);

        advanceToCombat(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .doesNotContain(opponentCreature.getId());
    }

    @Test
    void doubleStrikeWearsOffAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new MaraudingMako());
        harness.addToBattlefield(player1, new PrideOfTheRoad());
        gd.playerSpeeds.put(player1.getId(), 4);

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        assertThat(creature.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(creature.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void canTargetItselfAtMaxSpeed() {
        Permanent pride = harness.addToBattlefieldAndReturn(player1, new PrideOfTheRoad());
        gd.playerSpeeds.put(player1.getId(), 4);

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, pride.getId());
        harness.passBothPriorities();

        assertThat(pride.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void doesNotTriggerOnOpponentsTurn() {
        harness.addToBattlefield(player1, new PrideOfTheRoad());
        gd.playerSpeeds.put(player1.getId(), 4);

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.PermanentChoice.class);
    }

    @Test
    void cannotTargetOpponentsVehicle() {
        harness.addToBattlefield(player1, new PrideOfTheRoad());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player2, new CloudspireSkycycle());
        gd.playerSpeeds.put(player1.getId(), 4);

        advanceToCombat(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .doesNotContain(vehicle.getId());
    }

    @Test
    void doesNotGrantDoubleStrikeWhenTargetChangesController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MaraudingMako());
        harness.addToBattlefield(player1, new PrideOfTheRoad());
        gd.playerSpeeds.put(player1.getId(), 4);
        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerBattlefields.get(player2.getId()).add(creature);
        harness.passBothPriorities();

        assertThat(creature.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void triggeredAbilityResolvesAfterSourceLeavesAndSpeedDrops() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MaraudingMako());
        Permanent pride = harness.addToBattlefieldAndReturn(player1, new PrideOfTheRoad());
        gd.playerSpeeds.put(player1.getId(), 4);
        gd.playerSpeeds.put(player2.getId(), 1);
        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, creature.getId());

        harness.enterBattlefieldAndReturn(player2, new SpikeshellHarrier());
        harness.handlePermanentChosen(player2, pride.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Pride of the Road");
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(3);
        harness.passBothPriorities();

        assertThat(creature.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void triggeredAbilityResolvesAfterSourceLeavesAtUnchangedSpeed() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MaraudingMako());
        Permanent pride = harness.addToBattlefieldAndReturn(player1, new PrideOfTheRoad());
        gd.playerSpeeds.put(player1.getId(), 4);
        gd.playerSpeeds.put(player2.getId(), 4);
        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, creature.getId());

        harness.enterBattlefieldAndReturn(player2, new SpikeshellHarrier());
        harness.handlePermanentChosen(player2, pride.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Pride of the Road");
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(4);
        harness.passBothPriorities();

        assertThat(creature.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void doesNotGrantDoubleStrikeToTargetThatLeavesBattlefield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MaraudingMako());
        harness.addToBattlefield(player1, new PrideOfTheRoad());
        gd.playerSpeeds.put(player1.getId(), 4);
        gd.playerSpeeds.put(player2.getId(), 4);
        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, creature.getId());

        harness.enterBattlefieldAndReturn(player2, new SpikeshellHarrier());
        harness.handlePermanentChosen(player2, creature.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Marauding Mako");
        harness.passBothPriorities();

        assertThat(creature.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void startsEnginesAndIncreasesSpeedOnlyOncePerOwnTurn() {
        harness.forceActivePlayer(player1);
        harness.enterBattlefieldAndReturn(player1, new PrideOfTheRoad());
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);

        harness.inMutationScope(() -> harness.getLifeSupport().applyLifeLoss(gd, player2.getId(), 1, "Life loss"));
        harness.passBothPriorities();
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(2);

        harness.inMutationScope(() -> harness.getLifeSupport().applyLifeLoss(gd, player2.getId(), 1, "Life loss"));
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(2);

        harness.forceActivePlayer(player2);
        harness.inMutationScope(() -> harness.getLifeSupport().applyLifeLoss(gd, player2.getId(), 1, "Life loss"));
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(2);
    }
}
