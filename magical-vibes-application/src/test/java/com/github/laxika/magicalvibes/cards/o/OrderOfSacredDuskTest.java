package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.BaronyVampire;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
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
        Permanent order = addReadyCreature(player1, new OrderOfSacredDusk());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, order)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, order)).isEqualTo(6);
    }

    @Test
    @DisplayName("Another Vampire gets both exalted instances")
    void anotherVampireGetsTwoExaltedInstances() {
        addReadyCreature(player1, new OrderOfSacredDusk());
        Permanent vampire = addReadyCreature(player1, new BaronyVampire());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, vampire)).isEqualTo(4);
    }

    @Test
    @DisplayName("The granted exalted ability does not affect other creature types")
    void nonVampireGetsOnlyOrdersExalted() {
        addReadyCreature(player1, new OrderOfSacredDusk());
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exalted does not trigger when multiple creatures attack")
    void noTriggerWhenNotAlone() {
        addReadyCreature(player1, new OrderOfSacredDusk());
        Permanent vampire = addReadyCreature(player1, new BaronyVampire());
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1, 2));

        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, vampire)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exalted boosts wear off at end of turn")
    void boostsWearOff() {
        addReadyCreature(player1, new OrderOfSacredDusk());
        Permanent vampire = addReadyCreature(player1, new BaronyVampire());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(3);
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
