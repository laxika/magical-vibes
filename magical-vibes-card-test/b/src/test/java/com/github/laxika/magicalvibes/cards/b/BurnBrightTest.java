package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BurnBright.class, GrizzlyBears.class})
class BurnBrightTest extends BaseCardTest {

    private void castBurnBright() {
        harness.castFromHand(player1, new BurnBright(), "{2}{R}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Creatures you control get +2/+0 until end of turn")
    void boostsOwnCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castBurnBright();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost opponent's creatures")
    void doesNotBoostOpponentCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castBurnBright();

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castBurnBright();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Boosts every creature controlled when the spell resolves")
    void boostsAllCreaturesAtResolution() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new BurnBright(), "{2}{R}");
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(first.getEffectivePower()).isEqualTo(2);
        assertThat(second.getEffectivePower()).isEqualTo(2);
        harness.passBothPriorities();

        assertThat(first.getEffectivePower()).isEqualTo(4);
        assertThat(second.getEffectivePower()).isEqualTo(4);
        assertThat(first.getEffectiveToughness()).isEqualTo(2);
        assertThat(second.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures entering after resolution do not receive the boost")
    void doesNotBoostLaterCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castBurnBright();

        Permanent later = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(first.getEffectivePower()).isEqualTo(4);
        assertThat(later.getEffectivePower()).isEqualTo(2);
        assertThat(later.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple Burn Bright spells stack and both boosts expire")
    void multipleBoostsStackAndExpire() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castBurnBright();
        castBurnBright();

        assertThat(bears.getEffectivePower()).isEqualTo(6);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }
}
