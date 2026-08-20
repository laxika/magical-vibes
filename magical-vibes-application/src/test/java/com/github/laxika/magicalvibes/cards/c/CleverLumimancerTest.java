package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BarkshellBlessing;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CleverLumimancerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant boosts Clever Lumimancer until end of turn")
    void castingInstantBoostsLumimancer() {
        Permanent lumimancer = addCreatureReady(player1, new CleverLumimancer());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(lumimancer.getEffectivePower()).isEqualTo(2);
        assertThat(lumimancer.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Copying an instant triggers Clever Lumimancer")
    void copyingInstantBoostsLumimancer() {
        Permanent lumimancer = addCreatureReady(player1, new CleverLumimancer());
        Permanent conspireA = addCreatureReady(player1, new GrizzlyBears());
        Permanent conspireB = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BarkshellBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithConspire(player1, 0, target.getId(), List.of(conspireA.getId(), conspireB.getId()));
        harness.passBothPriorities();

        assertThat(lumimancer.getEffectivePower()).isEqualTo(4);
        assertThat(lumimancer.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Magecraft boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent lumimancer = addCreatureReady(player1, new CleverLumimancer());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(lumimancer.getEffectivePower()).isZero();
        assertThat(lumimancer.getEffectiveToughness()).isEqualTo(1);
    }
}
