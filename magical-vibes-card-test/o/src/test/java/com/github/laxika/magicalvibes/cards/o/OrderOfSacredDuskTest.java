package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.BaronyVampire;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OrderOfSacredDusk.class, BaronyVampire.class, GrizzlyBears.class})
class OrderOfSacredDuskTest extends BaseCardTest {

    @Test
    @DisplayName("Order of Sacred Dusk's exalted boosts itself when attacking alone")
    void selfAttackingAloneIsBoosted() {
        Permanent order = addCreatureReady(player1, new OrderOfSacredDusk());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, order)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, order)).isEqualTo(6);
    }

    @Test
    @DisplayName("Another Vampire gets both exalted instances")
    void anotherVampireGetsTwoExaltedInstances() {
        addCreatureReady(player1, new OrderOfSacredDusk());
        Permanent vampire = addCreatureReady(player1, new BaronyVampire());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, vampire)).isEqualTo(4);
    }

    @Test
    @DisplayName("The granted exalted ability does not affect other creature types")
    void nonVampireGetsOnlyOrdersExalted() {
        addCreatureReady(player1, new OrderOfSacredDusk());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exalted does not trigger when multiple creatures attack")
    void noTriggerWhenNotAlone() {
        addCreatureReady(player1, new OrderOfSacredDusk());
        Permanent vampire = addCreatureReady(player1, new BaronyVampire());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1, 2));

        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, vampire)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exalted boosts wear off at end of turn")
    void boostsWearOff() {
        addCreatureReady(player1, new OrderOfSacredDusk());
        Permanent vampire = addCreatureReady(player1, new BaronyVampire());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(3);
    }

    @Test
    @DisplayName("Each exalted instance from multiple Orders triggers separately")
    void multipleOrdersHaveIndependentExaltedTriggers() {
        Permanent attacker = addCreatureReady(player1, new OrderOfSacredDusk());
        addCreatureReady(player1, new OrderOfSacredDusk());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).hasSize(4);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(6);
    }

    @Test
    @DisplayName("An opponent's Vampire receives no exalted from Order")
    void opponentsVampireDoesNotReceiveExalted() {
        addCreatureReady(player1, new OrderOfSacredDusk());
        Permanent vampire = addCreatureReady(player2, new BaronyVampire());

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, vampire)).isEqualTo(2);
    }

    @Test
    @DisplayName("Summoning-sick creatures can convoke Order's colored mana")
    void convokePaysColoredManaWithSummoningSickCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new OrderOfSacredDusk());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new OrderOfSacredDusk());
        harness.setHand(player1, List.of(new OrderOfSacredDusk()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(first.getId(), second.getId()));

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Order of Sacred Dusk")).isEqualTo(3);
    }

    @Test
    @DisplayName("Order can attack immediately and gains life from flying combat damage")
    void hasteFlyingAndLifelinkWorkInCombat() {
        Permanent order = harness.addToBattlefieldAndReturn(player1, new OrderOfSacredDusk());
        addCreatureReady(player2, new GrizzlyBears());
        int startingLife = gd.playerLifeTotals.get(player1.getId());
        int defendingLife = gd.playerLifeTotals.get(player2.getId());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 6);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(defendingLife - 6);
        assertThat(gqs.getEffectivePower(gd, order)).isEqualTo(6);
    }
}
