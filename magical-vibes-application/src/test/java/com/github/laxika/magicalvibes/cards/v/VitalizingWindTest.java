package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.p.PygmyRazorback;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VitalizingWind.class, PygmyRazorback.class})
class VitalizingWindTest extends BaseCardTest {

    private void castVitalizingWind() {
        harness.setHand(player1, List.of(new VitalizingWind()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.castAndResolveInstant(player1, 0);
    }

    @Test
    @DisplayName("Creatures you control get +7/+7 until end of turn")
    void boostsOwnCreatures() {
        Permanent razorback = harness.addToBattlefieldAndReturn(player1, new PygmyRazorback());
        castVitalizingWind();

        assertThat(razorback.getEffectivePower()).isEqualTo(9);
        assertThat(razorback.getEffectiveToughness()).isEqualTo(8);
    }

    @Test
    @DisplayName("Does not boost opponent's creatures")
    void doesNotBoostOpponentCreatures() {
        Permanent razorback = harness.addToBattlefieldAndReturn(player2, new PygmyRazorback());
        castVitalizingWind();

        assertThat(razorback.getEffectivePower()).isEqualTo(2);
        assertThat(razorback.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not boost creatures that enter after it resolves")
    void doesNotBoostCreaturesEnteringAfterResolution() {
        castVitalizingWind();

        Permanent razorback = harness.addToBattlefieldAndReturn(player1, new PygmyRazorback());

        assertThat(razorback.getEffectivePower()).isEqualTo(2);
        assertThat(razorback.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent razorback = harness.addToBattlefieldAndReturn(player1, new PygmyRazorback());
        castVitalizingWind();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(razorback.getEffectivePower()).isEqualTo(2);
        assertThat(razorback.getEffectiveToughness()).isEqualTo(1);
    }
}
