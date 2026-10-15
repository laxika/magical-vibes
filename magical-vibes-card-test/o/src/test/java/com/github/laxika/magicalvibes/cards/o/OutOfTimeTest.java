package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VampireHexmage;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OutOfTime.class, Disenchant.class, GrizzlyBears.class, VampireHexmage.class, Opalescence.class})
class OutOfTimeTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps and phases out all creatures, counting the creatures phased out")
    void untapsAndPhasesOutAllCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        ownCreature.tap();
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opposingCreature.tap();

        castAndResolveOutOfTime();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingCreature);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(ownCreature);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(opposingCreature);
        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(opposingCreature.isTapped()).isFalse();
        assertThat(findPermanent(player1, "Out of Time").getCounterCount(CounterType.TIME)).isEqualTo(2);
    }

    @Test
    @DisplayName("Keeps creatures phased out through their next untap step")
    void keepsCreaturesPhasedOutUntilItLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castAndResolveOutOfTime();

        harness.performUntapStep(player2);

        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(creature);
    }

    @Test
    @DisplayName("Phases creatures in untapped when it leaves the battlefield")
    void phasesCreaturesInUntappedWhenItLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castAndResolveOutOfTime();
        Permanent outOfTime = findPermanent(player1, "Out of Time");

        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castAndResolveInstant(player1, 0, outOfTime.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(creature.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Out of Time");
    }

    @Test
    @DisplayName("Removes a time counter during upkeep and sacrifices on the last one")
    void vanishingRemovesCountersAndSacrificesOnLastCounter() {
        Permanent outOfTime = harness.addToBattlefieldAndReturn(player1, new OutOfTime());
        outOfTime.setCounterCount(CounterType.TIME, 1);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Out of Time");
        harness.assertInGraveyard(player1, "Out of Time");
    }

    @Test
    @DisplayName("Entering with no creatures leaves it indefinitely without time counters")
    void noCreaturesLeavesItWithoutTimeCounters() {
        castAndResolveOutOfTime();
        Permanent outOfTime = findPermanent(player1, "Out of Time");

        assertThat(outOfTime.getCounterCount(CounterType.TIME)).isZero();
        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Out of Time");
    }

    @Test
    @DisplayName("The first of two time counters is removed without releasing creatures")
    void removesOneOfTwoCountersWithoutSacrificing() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castAndResolveOutOfTime();

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Out of Time").getCounterCount(CounterType.TIME)).isEqualTo(1);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(ownCreature);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(opposingCreature);
    }

    @Test
    @DisplayName("Vanishing sacrifice phases creatures back in untapped with their counters")
    void lastUpkeepCounterReleasesCreaturesWithCounters() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        creature.tap();
        castAndResolveOutOfTime();

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Out of Time");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removing the last time counter with another ability triggers sacrifice")
    void externalRemovalOfLastTimeCounterTriggersSacrifice() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castAndResolveOutOfTime();
        Permanent outOfTime = findPermanent(player1, "Out of Time");
        harness.addToBattlefield(player1, new VampireHexmage());

        harness.activateAbility(player1, 1, null, outOfTime.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Out of Time");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    @Test
    @DisplayName("If the source leaves before its entry trigger resolves, creatures only untap")
    void sourceLeavesBeforeEntryTriggerResolves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.tap();
        harness.setHand(player1, List.of(new OutOfTime(), new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        Permanent outOfTime = findPermanent(player1, "Out of Time");

        harness.castAndResolveInstant(player1, 0, outOfTime.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Out of Time");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An animated Out of Time phases out without receiving time counters")
    void animatedSourcePhasesOutWithoutReceivingCounters() {
        harness.addToBattlefield(player1, new Opalescence());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new OutOfTime()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        Permanent outOfTime = findPermanent(player1, "Out of Time");
        resolveAllTriggers();

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(outOfTime);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(creature);
        assertThat(outOfTime.getCounterCount(CounterType.TIME)).isZero();
        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.performUntapStep(player2);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(outOfTime);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(creature);
    }

    private void castAndResolveOutOfTime() {
        harness.setHand(player1, List.of(new OutOfTime()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

}
