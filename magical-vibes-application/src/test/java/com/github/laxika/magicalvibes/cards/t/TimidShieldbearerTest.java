package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TimidShieldbearer.class})
class TimidShieldbearerTest extends BaseCardTest {

    @Test
    @DisplayName("Activation gives +1/+1 to all creatures you control")
    void activationBoostsOwnCreatures() {
        Permanent shieldbearer = harness.addToBattlefieldAndReturn(player1, new TimidShieldbearer());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new TimidShieldbearer());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new TimidShieldbearer());
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(shieldbearer.getPowerModifier()).isEqualTo(1);
        assertThat(shieldbearer.getToughnessModifier()).isEqualTo(1);
        assertThat(otherCreature.getPowerModifier()).isEqualTo(1);
        assertThat(otherCreature.getToughnessModifier()).isEqualTo(1);
        assertThat(opponentCreature.getPowerModifier()).isEqualTo(0);
        assertThat(opponentCreature.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Activation boost expires at end of turn")
    void activationBoostExpiresAtEndOfTurn() {
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new TimidShieldbearer());
        harness.addToBattlefield(player1, new TimidShieldbearer());
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(otherCreature.getPowerModifier()).isEqualTo(1);
        assertThat(otherCreature.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(otherCreature.getPowerModifier()).isEqualTo(0);
        assertThat(otherCreature.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost applies to creatures present at resolution, not later entrants")
    void boostLocksInCreaturesAtResolution() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new TimidShieldbearer());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        assertThat(source.getPowerModifier()).isZero();
        assertThat(source.getToughnessModifier()).isZero();
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new TimidShieldbearer());
        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new TimidShieldbearer());

        assertThat(source.getPowerModifier()).isEqualTo(1);
        assertThat(source.getToughnessModifier()).isEqualTo(1);
        assertThat(beforeResolution.getPowerModifier()).isEqualTo(1);
        assertThat(beforeResolution.getToughnessModifier()).isEqualTo(1);
        assertThat(afterResolution.getPowerModifier()).isZero();
        assertThat(afterResolution.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Tapped summoning-sick Shieldbearer can activate repeatedly and boosts stack")
    void tappedSummoningSickSourceCanActivateRepeatedly() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new TimidShieldbearer());
        source.tap();
        source.setSummoningSick(true);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(source.getPowerModifier()).isEqualTo(1);
        assertThat(source.getToughnessModifier()).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(source.getPowerModifier()).isEqualTo(2);
        assertThat(source.getToughnessModifier()).isEqualTo(2);
        assertThat(source.isTapped()).isTrue();
    }
}
