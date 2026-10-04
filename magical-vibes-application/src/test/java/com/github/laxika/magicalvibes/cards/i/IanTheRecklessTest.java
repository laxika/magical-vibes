package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.s.SilverShroudCostume;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IanTheReckless.class, SilverShroudCostume.class})
class IanTheRecklessTest extends BaseCardTest {

    @Test
    @DisplayName("A modified Ian may deal its power to you and any target")
    void modifiedIanDealsPowerDamageToBothTargets() {
        Permanent ian = addCreatureReady(player1, new IanTheReckless());
        ian.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(gd.getLife(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Declining Ian's attack trigger deals no damage")
    void decliningTriggerDealsNoDamage() {
        Permanent ian = addCreatureReady(player1, new IanTheReckless());
        ian.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("An unmodified Ian does not create an attack trigger")
    void unmodifiedIanDoesNotTrigger() {
        addCreatureReady(player1, new IanTheReckless());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Ian's ability does nothing if it is no longer modified at resolution")
    void losingModificationBeforeResolutionStopsAbility() {
        Permanent ian = addCreatureReady(player1, new IanTheReckless());
        ian.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, player2.getId());
        ian.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Ian uses its power at resolution rather than when it attacked")
    void usesCurrentPowerAtResolution() {
        Permanent ian = addCreatureReady(player1, new IanTheReckless());
        ian.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, player2.getId());
        ian.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 12);
    }

    @Test
    @DisplayName("Equipment alone modifies Ian and enables its attack ability")
    void equipmentEnablesAbilityWithoutCounters() {
        Permanent ian = addCreatureReady(player1, new IanTheReckless());
        Permanent costume = harness.addToBattlefieldAndReturn(player1, new SilverShroudCostume());
        costume.setAttachedTo(ian.getId());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Ian may target its controller, dealing twice its power to that player")
    void controllerCanBeChosenAsTarget() {
        Permanent ian = addCreatureReady(player1, new IanTheReckless());
        ian.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 14);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Ian can deal its power to a creature")
    void creatureCanBeChosenAsTarget() {
        Permanent ian = addCreatureReady(player1, new IanTheReckless());
        ian.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = addCreatureReady(player2, new IanTheReckless());
        harness.setLife(player1, 20);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 17);
        harness.assertInGraveyard(player2, "Ian the Reckless");
    }

    @Test
    @DisplayName("An illegal sole target prevents Ian's self damage as well")
    void removedTargetStopsAllAbilityDamage() {
        Permanent ian = addCreatureReady(player1, new IanTheReckless());
        ian.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = addCreatureReady(player2, new IanTheReckless());
        harness.setLife(player1, 20);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.getPermanentRemovalService().sacrificePermanentToGraveyard(gd, target);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Ian uses its last known power if it leaves while modified")
    void removedIanUsesLastKnownPower() {
        Permanent ian = addCreatureReady(player1, new IanTheReckless());
        ian.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, player2.getId());
        ian.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.getPermanentRemovalService().sacrificePermanentToGraveyard(gd, ian);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 16);
    }
}
