package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CreepingTrailblazerTest extends BaseCardTest {

    @Test
    @DisplayName("Other Elementals you control get +1/+0")
    void buffsOtherElementalsYouControl() {
        Permanent elemental = addCreatureReady(player1, new AirElemental());
        Permanent nonElemental = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentElemental = addCreatureReady(player2, new AirElemental());
        int elementalPower = gqs.getEffectivePower(gd, elemental);
        int nonElementalPower = gqs.getEffectivePower(gd, nonElemental);
        int opponentElementalPower = gqs.getEffectivePower(gd, opponentElemental);

        Permanent trailblazer = addCreatureReady(player1, new CreepingTrailblazer());

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(elementalPower + 1);
        assertThat(gqs.getEffectivePower(gd, nonElemental)).isEqualTo(nonElementalPower);
        assertThat(gqs.getEffectivePower(gd, opponentElemental)).isEqualTo(opponentElementalPower);
        assertThat(gqs.getEffectivePower(gd, trailblazer)).isEqualTo(2);
    }

    @Test
    @DisplayName("The activated ability counts Elementals you control and wears off at cleanup")
    void activatedAbilityCountsElementalsAndExpires() {
        Permanent trailblazer = addCreatureReady(player1, new CreepingTrailblazer());
        addCreatureReady(player1, new AirElemental());
        addCreatureReady(player1, new GrizzlyBears());
        int initialPower = gqs.getEffectivePower(gd, trailblazer);
        int initialToughness = gqs.getEffectiveToughness(gd, trailblazer);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, trailblazer)).isEqualTo(initialPower + 2);
        assertThat(gqs.getEffectiveToughness(gd, trailblazer)).isEqualTo(initialToughness + 2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, trailblazer)).isEqualTo(initialPower);
        assertThat(gqs.getEffectiveToughness(gd, trailblazer)).isEqualTo(initialToughness);
    }
}
