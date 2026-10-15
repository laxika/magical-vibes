package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeadlyDesigns.class, GrizzlyBears.class, SavannahLions.class, Disenchant.class})
class DeadlyDesignsTest extends BaseCardTest {

    @Test
    void anyPlayerMayAddPlotCounters() {
        Permanent designs = harness.addToBattlefieldAndReturn(player1, new DeadlyDesigns());
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(designs.getCounterCount(CounterType.PLOT)).isEqualTo(1);
    }

    @Test
    void fivePlotCountersSacrificeAndDestroyUpToTwoCreatures() {
        Permanent designs = harness.addToBattlefieldAndReturn(player1, new DeadlyDesigns());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        designs.setCounterCount(CounterType.PLOT, 4);

        harness.runStateBasedActions();
        assertThat(gd.stack).isEmpty();

        designs.setCounterCount(CounterType.PLOT, 5);
        harness.runStateBasedActions();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, firstCreature.getId());
        harness.handlePermanentChosen(player1, secondCreature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Deadly Designs");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Deadly Designs");
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(firstCreature.getCard(), secondCreature.getCard());
    }

    @Test
    void sacrificesWithNoCreaturesToTarget() {
        Permanent designs = harness.addToBattlefieldAndReturn(player1, new DeadlyDesigns());
        designs.setCounterCount(CounterType.PLOT, 5);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Deadly Designs");
        harness.assertInGraveyard(player1, "Deadly Designs");
    }

    @Test
    void controllersActivationAddsFifthCounterAndTriggersSacrifice() {
        Permanent designs = harness.addToBattlefieldAndReturn(player1, new DeadlyDesigns());
        designs.setCounterCount(CounterType.PLOT, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        assertThat(designs.getCounterCount(CounterType.PLOT)).isEqualTo(4);
        harness.passBothPriorities();

        assertThat(designs.getCounterCount(CounterType.PLOT)).isEqualTo(5);
        harness.assertOnBattlefield(player1, "Deadly Designs");
        assertThat(gd.stack).hasSize(1);
        harness.runStateBasedActions();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Deadly Designs");
    }

    @Test
    void mayChooseZeroTargetsEvenWhenCreaturesExist() {
        Permanent designs = harness.addToBattlefieldAndReturn(player1, new DeadlyDesigns());
        harness.addToBattlefield(player2, new SavannahLions());
        designs.setCounterCount(CounterType.PLOT, 5);

        harness.runStateBasedActions();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Deadly Designs");
        harness.assertOnBattlefield(player2, "Savannah Lions");
    }

    @Test
    void mayDestroyOnlyOneCreatureIncludingYourOwn() {
        Permanent designs = harness.addToBattlefieldAndReturn(player1, new DeadlyDesigns());
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new SavannahLions());
        harness.addToBattlefield(player2, new SavannahLions());
        designs.setCounterCount(CounterType.PLOT, 6);

        harness.runStateBasedActions();
        harness.handlePermanentChosen(player1, chosen.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Deadly Designs");
        harness.assertInGraveyard(player1, "Savannah Lions");
        harness.assertOnBattlefield(player2, "Savannah Lions");
    }

    @Test
    void triggerStillResolvesIfPlotCountersFallBelowFive() {
        Permanent designs = harness.addToBattlefieldAndReturn(player1, new DeadlyDesigns());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SavannahLions());
        designs.setCounterCount(CounterType.PLOT, 5);
        harness.runStateBasedActions();
        harness.handlePermanentChosen(player1, creature.getId());

        designs.setCounterCount(CounterType.PLOT, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Deadly Designs");
        harness.assertInGraveyard(player2, "Savannah Lions");
    }

    @Test
    void doesNotDestroyTargetsWhenSourceLeavesBeforeResolution() {
        Permanent designs = harness.addToBattlefieldAndReturn(player1, new DeadlyDesigns());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SavannahLions());
        designs.setCounterCount(CounterType.PLOT, 5);
        harness.runStateBasedActions();
        harness.handlePermanentChosen(player1, creature.getId());

        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, designs.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Deadly Designs");
        harness.assertOnBattlefield(player2, "Savannah Lions");
    }
}
