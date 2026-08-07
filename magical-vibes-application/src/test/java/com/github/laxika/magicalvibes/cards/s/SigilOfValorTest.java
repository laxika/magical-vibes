package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SigilOfValorTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking alone boosts the equipped creature per other creature you control")
    void attacksAloneBoostsPerOtherCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sigil = addCreatureReady(player1, new SigilOfValor());
        sigil.setAttachedTo(creature.getId());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(2);
        assertThat(creature.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking alone with no other creatures gives no boost")
    void attacksAloneWithNoOtherCreatures() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sigil = addCreatureReady(player1, new SigilOfValor());
        sigil.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("No trigger when the equipped creature doesn't attack alone")
    void noTriggerWhenNotAttackingAlone() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sigil = addCreatureReady(player1, new SigilOfValor());
        sigil.setAttachedTo(creature.getId());
        addCreatureReady(player1, new GrizzlyBears());

        // Index 1 is the Equipment, so the second creature is index 2.
        declareAttackers(player1, List.of(0, 2));
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sigil = addCreatureReady(player1, new SigilOfValor());
        sigil.setAttachedTo(creature.getId());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
    }
}
