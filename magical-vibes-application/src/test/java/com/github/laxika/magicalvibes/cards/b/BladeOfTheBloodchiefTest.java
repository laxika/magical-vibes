package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.v.VampireHexmage;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

    private Permanent addBladeReady() {
        Permanent blade = new Permanent(new BladeOfTheBloodchief());
        blade.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(blade);
        return blade;
    }

    private void destroyWithLightningBolt(Permanent victim) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, victim.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
