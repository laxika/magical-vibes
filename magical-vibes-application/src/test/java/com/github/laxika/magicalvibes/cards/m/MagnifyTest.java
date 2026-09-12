package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BraidwoodCup;
import com.github.laxika.magicalvibes.cards.w.WildColos;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Magnify.class, WildColos.class, BraidwoodCup.class})
class MagnifyTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts all creatures on the battlefield")
    void boostsAllCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new WildColos());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new WildColos());

        harness.castFromHand(player1, new Magnify(), "{G}");
        harness.passBothPriorities();

        assertThat(ownCreature.getEffectivePower()).isEqualTo(3);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(3);
        assertThat(opponentCreature.getEffectivePower()).isEqualTo(3);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WildColos());

        harness.castFromHand(player1, new Magnify(), "{G}");
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(3);
        assertThat(creature.getEffectiveToughness()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not affect noncreatures or creatures entering after resolution")
    void onlyAffectsCreaturesPresentAtResolution() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new BraidwoodCup());

        harness.castFromHand(player1, new Magnify(), "{G}");
        harness.passBothPriorities();

        assertThat(artifact.getPowerModifier()).isZero();
        assertThat(artifact.getToughnessModifier()).isZero();

        Permanent laterCreature = harness.addToBattlefieldAndReturn(player2, new WildColos());
        assertThat(laterCreature.getEffectivePower()).isEqualTo(2);
        assertThat(laterCreature.getEffectiveToughness()).isEqualTo(2);
    }
}
