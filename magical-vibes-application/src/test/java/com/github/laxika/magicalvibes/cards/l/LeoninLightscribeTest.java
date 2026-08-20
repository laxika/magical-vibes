package com.github.laxika.magicalvibes.cards.l;

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

class LeoninLightscribeTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant boosts all creatures you control until end of turn")
    void castingInstantBoostsOwnCreatures() {
        Permanent lightscribe = addCreatureReady(player1, new LeoninLightscribe());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(lightscribe.getEffectivePower()).isEqualTo(3);
        assertThat(lightscribe.getEffectiveToughness()).isEqualTo(3);
        assertThat(creature.getEffectivePower()).isEqualTo(6);
        assertThat(creature.getEffectiveToughness()).isEqualTo(6);
        assertThat(opponentCreature.getEffectivePower()).isEqualTo(2);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Copying an instant triggers Leonin Lightscribe")
    void copyingInstantBoostsOwnCreatures() {
        Permanent lightscribe = addCreatureReady(player1, new LeoninLightscribe());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent conspireA = addCreatureReady(player1, new GrizzlyBears());
        Permanent conspireB = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BarkshellBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithConspire(player1, 0, target.getId(), List.of(conspireA.getId(), conspireB.getId()));
        harness.passBothPriorities();

        assertThat(lightscribe.getEffectivePower()).isEqualTo(4);
        assertThat(lightscribe.getEffectiveToughness()).isEqualTo(4);
        assertThat(creature.getEffectivePower()).isEqualTo(4);
        assertThat(creature.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger Leonin Lightscribe")
    void castingCreatureDoesNotBoostOwnCreatures() {
        Permanent lightscribe = addCreatureReady(player1, new LeoninLightscribe());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(lightscribe.getEffectivePower()).isEqualTo(2);
        assertThat(lightscribe.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Magecraft boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent lightscribe = addCreatureReady(player1, new LeoninLightscribe());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(lightscribe.getEffectivePower()).isEqualTo(2);
        assertThat(lightscribe.getEffectiveToughness()).isEqualTo(2);
        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
    }
}
