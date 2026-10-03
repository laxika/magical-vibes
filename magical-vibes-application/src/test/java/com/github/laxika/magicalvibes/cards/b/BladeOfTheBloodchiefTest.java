package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
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

@CardUsed({BladeOfTheBloodchief.class, GrizzlyBears.class, LightningBolt.class, VampireHexmage.class,
        IntoTheRoil.class})
class BladeOfTheBloodchiefTest extends BaseCardTest {

    @Test
    @DisplayName("Equip attaches Blade of the Bloodchief to a creature")
    void equipsCreature() {
        Permanent blade = addBladeReady();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("A creature dying puts one +1/+1 counter on a non-Vampire equipped creature")
    void nonVampireGetsOneCounter() {
        Permanent host = addCreatureReady(player1, new GrizzlyBears());
        Permanent blade = addBladeReady();
        blade.setAttachedTo(host.getId());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        destroyWithLightningBolt(victim);

        assertThat(host.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature dying puts two +1/+1 counters on a Vampire equipped creature")
    void vampireGetsTwoCounters() {
        Permanent host = addCreatureReady(player1, new VampireHexmage());
        Permanent blade = addBladeReady();
        blade.setAttachedTo(host.getId());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        destroyWithLightningBolt(victim);

        assertThat(host.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("An unattached Blade of the Bloodchief cannot put counters on a creature")
    void unattachedBladeDoesNothing() {
        Permanent blade = addBladeReady();
        Permanent host = addCreatureReady(player1, new GrizzlyBears());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        destroyWithLightningBolt(victim);

        assertThat(blade.getAttachedTo()).isNull();
        assertThat(host.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A sacrificed allied creature triggers the Blade")
    void alliedSacrificeGivesVampireTwoCounters() {
        Permanent host = addCreatureReady(player1, new VampireHexmage());
        Permanent blade = addBladeReady();
        blade.setAttachedTo(host.getId());
        addCreatureReady(player1, new VampireHexmage());

        harness.activateAbility(player1, 2, null, blade.getId());
        resolveAllTriggers();

        assertThat(host.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("A pending trigger still gives a Vampire two counters after the Blade leaves")
    void vampireGetsTwoCountersAfterBladeLeaves() {
        Permanent host = addCreatureReady(player1, new VampireHexmage());
        Permanent blade = addBladeReady();
        blade.setAttachedTo(host.getId());
        addCreatureReady(player1, new VampireHexmage());
        harness.setHand(player1, List.of(new IntoTheRoil()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 2, null, blade.getId());
        harness.castAndResolveInstant(player1, 0, blade.getId());
        harness.assertInHand(player1, "Blade of the Bloodchief");
        resolveAllTriggers();

        assertThat(host.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The equipped creature dying does not transfer counters to another creature")
    void equippedCreatureDiesWithoutGivingAnotherCreatureCounters() {
        Permanent host = addCreatureReady(player1, new VampireHexmage());
        Permanent blade = addBladeReady();
        blade.setAttachedTo(host.getId());
        Permanent survivor = addCreatureReady(player1, new VampireHexmage());

        harness.activateAbility(player1, 0, null, blade.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(host);
        assertThat(survivor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addBladeReady() {
        return addCreatureReady(player1, new BladeOfTheBloodchief());
    }

    private void destroyWithLightningBolt(Permanent victim) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, victim.getId());
        harness.passBothPriorities();
    }
}
