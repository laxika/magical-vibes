package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.ChandraBoldPyromancer;
import com.github.laxika.magicalvibes.cards.e.ElfhameDruid;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinChainwhirler.class, GrizzlyBears.class, ElfhameDruid.class, ChandraBoldPyromancer.class})
class GoblinChainwhirlerTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts it on the stack as a creature spell")
    void castingPutsOnStack() {
        castChainwhirler();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Goblin Chainwhirler");
    }

    // ===== ETB damage to opponent =====

    @Test
    @DisplayName("Resolving puts Goblin Chainwhirler on battlefield with ETB trigger on stack")
    void resolvingPutsOnBattlefieldWithEtbOnStack() {
        castChainwhirler();
        harness.passBothPriorities(); // resolve creature spell

        harness.assertOnBattlefield(player1, "Goblin Chainwhirler");

        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Goblin Chainwhirler");
    }

    @Test
    @DisplayName("ETB deals 1 damage to each opponent")
    void etbDeals1DamageToEachOpponent() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castChainwhirler();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    // ===== ETB damage to creatures =====

    @Test
    @DisplayName("ETB deals 1 damage to each creature opponent controls, killing 1/1s")
    void etbKillsOneOneCreatures() {
        GrizzlyBears smallCreature = new GrizzlyBears();
        smallCreature.setPower(1);
        smallCreature.setToughness(1);
        harness.addToBattlefield(player2, smallCreature);

        castChainwhirler();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB deals 1 damage to opponent's 2/2 creature but does not kill it")
    void etbDoesNotKillTwoTwoCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        castChainwhirler();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB does not damage controller's creatures")
    void etbDoesNotDamageControllerCreatures() {
        GrizzlyBears ownCreature = new GrizzlyBears();
        ownCreature.setPower(1);
        ownCreature.setToughness(1);
        harness.addToBattlefield(player1, ownCreature);

        castChainwhirler();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB damages multiple opponent creatures simultaneously")
    void etbDamagesMultipleCreatures() {
        GrizzlyBears creature1 = new GrizzlyBears();
        creature1.setPower(1);
        creature1.setToughness(1);
        GrizzlyBears creature2 = new GrizzlyBears();
        creature2.setPower(1);
        creature2.setToughness(1);
        harness.addToBattlefield(player2, creature1);
        harness.addToBattlefield(player2, creature2);

        castChainwhirler();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    // ===== Combined damage =====

    @Test
    @DisplayName("ETB deals damage to opponent AND their creatures simultaneously")
    void etbDealsDamageToBothOpponentAndCreatures() {
        harness.setLife(player2, 20);
        GrizzlyBears creature = new GrizzlyBears();
        creature.setPower(1);
        creature.setToughness(1);
        harness.addToBattlefield(player2, creature);

        castChainwhirler();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    // ===== Stack is clean after resolution =====

    @Test
    @DisplayName("Stack is empty after full resolution")
    void stackIsEmptyAfterResolution() {
        castChainwhirler();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.stack).isEmpty();
    }

    // ===== Helpers =====

    @Test
    @DisplayName("ETB damages opposing planeswalkers and leaves own planeswalkers untouched")
    void etbDamagesOnlyOpposingPlaneswalkers() {
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new ChandraBoldPyromancer());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new ChandraBoldPyromancer());
        opposing.setCounterCount(CounterType.LOYALTY, 5);
        own.setCounterCount(CounterType.LOYALTY, 5);

        castChainwhirler();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(opposing.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(own.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("ETB puts a planeswalker with one loyalty into its owner's graveyard")
    void etbKillsPlaneswalkerAtOneLoyalty() {
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new ChandraBoldPyromancer());
        opposing.setCounterCount(CounterType.LOYALTY, 1);

        castChainwhirler();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Chandra, Bold Pyromancer");
        harness.assertInGraveyard(player2, "Chandra, Bold Pyromancer");
    }

    @Test
    @DisplayName("ETB marks exactly one damage on a surviving opposing creature")
    void etbMarksDamageOnSurvivor() {
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new ElfhameDruid());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new ElfhameDruid());

        castChainwhirler();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(opposing.getMarkedDamage()).isEqualTo(1);
        assertThat(own.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("First strike kills a blocker before it can deal combat damage")
    void firstStrikeKillsBlockerBeforeRegularDamage() {
        Permanent attacker = addCreatureReady(player1, new GoblinChainwhirler());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertOnBattlefield(player1, "Goblin Chainwhirler");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(attacker.getMarkedDamage()).isZero();
    }

    private void castChainwhirler() {
        harness.castFromHand(player1, new GoblinChainwhirler(), "{R}{R}{R}");
    }
}
