package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BigPlay;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RecklessAmplimancer.class, BigPlay.class})
class RecklessAmplimancerTest extends BaseCardTest {

    @Test
    void activatingOnceDoublesPowerAndToughness() {
        Permanent amplimancer = addAmplimancerReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(amplimancer.getEffectivePower()).isEqualTo(4);
        assertThat(amplimancer.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    void activatingTwiceCompoundsTheBoost() {
        Permanent amplimancer = addAmplimancerReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 10);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(amplimancer.getEffectivePower()).isEqualTo(8);
        assertThat(amplimancer.getEffectiveToughness()).isEqualTo(8);
    }

    @Test
    void boostWearsOffAtEndOfTurn() {
        Permanent amplimancer = addAmplimancerReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(amplimancer.getEffectivePower()).isEqualTo(2);
        assertThat(amplimancer.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void canActivateWhileTappedAndSummoningSick() {
        Permanent amplimancer = harness.addToBattlefieldAndReturn(player1, new RecklessAmplimancer());
        amplimancer.setSummoningSick(true);
        amplimancer.tap();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(amplimancer.getEffectivePower()).isEqualTo(4);
        assertThat(amplimancer.getEffectiveToughness()).isEqualTo(4);
        assertThat(amplimancer.isTapped()).isTrue();
    }

    @Test
    void cannotActivateWithoutGreenMana() {
        Permanent amplimancer = addAmplimancerReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(amplimancer.getEffectivePower()).isEqualTo(2);
        assertThat(amplimancer.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doublesStatsAtResolutionIncludingPumpAndCounterAddedInResponse() {
        Permanent amplimancer = addAmplimancerReady(player1);
        harness.setHand(player1, List.of(new BigPlay()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.activateAbility(player1, 0, null, null);
        harness.castAndResolveInstant(player1, 0, amplimancer.getId());
        assertThat(amplimancer.getEffectivePower()).isEqualTo(5);
        assertThat(amplimancer.getEffectiveToughness()).isEqualTo(5);
        harness.passBothPriorities();

        assertThat(amplimancer.getEffectivePower()).isEqualTo(10);
        assertThat(amplimancer.getEffectiveToughness()).isEqualTo(10);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(amplimancer.getEffectivePower()).isEqualTo(3);
        assertThat(amplimancer.getEffectiveToughness()).isEqualTo(3);
    }

    private Permanent addAmplimancerReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new RecklessAmplimancer());
        perm.setSummoningSick(false);
        return perm;
    }
}
