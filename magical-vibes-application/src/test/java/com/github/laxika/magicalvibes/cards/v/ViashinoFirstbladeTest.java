package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ViashinoFirstbladeTest extends BaseCardTest {

    private Permanent castFirstblade(Player player) {
        harness.setHand(player, List.of(new ViashinoFirstblade()));
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.castCreature(player, 0);
        harness.passBothPriorities(); // resolve the creature, queue ETB trigger
        harness.passBothPriorities(); // resolve ETB trigger
        return findPermanent(player, "Viashino Firstblade");
    }

    @Test
    @DisplayName("Enters as a 4/4 thanks to the +2/+2 ETB boost")
    void etbBoostsSelf() {
        Permanent firstblade = castFirstblade(player1);

        assertThat(gqs.getEffectivePower(gd, firstblade)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, firstblade)).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost wears off at end of turn, leaving a 2/2")
    void boostWearsOff() {
        Permanent firstblade = castFirstblade(player1);
        assertThat(gqs.getEffectivePower(gd, firstblade)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, firstblade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, firstblade)).isEqualTo(2);
    }
}
