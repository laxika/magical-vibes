package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DemolisherSpawn.class, GrizzlyBears.class, Forest.class, Shock.class, Millstone.class})
class DemolisherSpawnTest extends BaseCardTest {

    @Test
    @DisplayName("With delirium, other attacking creatures get +4/+4")
    void deliriumBoostsOtherAttackingCreatures() {
        setUpDelirium();
        Permanent spawn = addCreatureReady(player1, new DemolisherSpawn());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(attacker.getPowerModifier()).isEqualTo(4);
        assertThat(attacker.getToughnessModifier()).isEqualTo(4);
        assertThat(spawn.getPowerModifier()).isZero();
        assertThat(nonAttacker.getPowerModifier()).isZero();
        assertThat(nonAttacker.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Without delirium, the attack trigger does not boost creatures")
    void withoutDeliriumDoesNotBoostCreatures() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new Shock()));
        Permanent spawn = addCreatureReady(player1, new DemolisherSpawn());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(spawn.getPowerModifier()).isZero();
        assertThat(attacker.getPowerModifier()).isZero();
        assertThat(attacker.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        setUpDelirium();
        addCreatureReady(player1, new DemolisherSpawn());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();
        assertThat(attacker.getPowerModifier()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isZero();
        assertThat(attacker.getToughnessModifier()).isZero();
    }

    private void setUpDelirium() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Millstone()));
    }
}
