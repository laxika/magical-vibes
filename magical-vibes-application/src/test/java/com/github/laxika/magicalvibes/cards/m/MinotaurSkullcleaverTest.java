package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MinotaurSkullcleaverTest extends BaseCardTest {

    private Permanent castSkullcleaver(Player player) {
        harness.setHand(player, List.of(new MinotaurSkullcleaver()));
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.castCreature(player, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player, "Minotaur Skullcleaver");
    }

    @Test
    @DisplayName("Enters as a 4/2 thanks to the +2/+0 ETB boost")
    void etbBoostsSelf() {
        Permanent skullcleaver = castSkullcleaver(player1);

        assertThat(gqs.getEffectivePower(gd, skullcleaver)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, skullcleaver)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at end of turn, leaving a 2/2")
    void boostWearsOff() {
        Permanent skullcleaver = castSkullcleaver(player1);
        assertThat(gqs.getEffectivePower(gd, skullcleaver)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, skullcleaver)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, skullcleaver)).isEqualTo(2);
    }
}
