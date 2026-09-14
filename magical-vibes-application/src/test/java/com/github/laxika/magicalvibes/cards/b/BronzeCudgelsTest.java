package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BronzeCudgels.class, GrizzlyBears.class})
class BronzeCudgelsTest extends BaseCardTest {

    @Test
    void pumpIncreasesWithEachResolution() {
        Permanent cudgels = addReadyCudgels();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        cudgels.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(creature.getPowerModifier()).isEqualTo(1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(creature.getPowerModifier()).isEqualTo(3);
    }

    @Test
    void resolutionCountResetsAtEndOfTurn() {
        Permanent cudgels = addReadyCudgels();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        cudgels.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(creature.getPowerModifier()).isEqualTo(3);

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(creature.getPowerModifier()).isZero();
    }

    @Test
    void unattachedResolutionStillCounts() {
        Permanent cudgels = addReadyCudgels();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        cudgels.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(2);
    }

    @Test
    void equipAttachesToCreatureYouControl() {
        Permanent cudgels = addReadyCudgels();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(cudgels.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addReadyCudgels() {
        Permanent cudgels = harness.addToBattlefieldAndReturn(player1, new BronzeCudgels());
        cudgels.setSummoningSick(false);
        return cudgels;
    }
}
