package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AvenInitiate;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MinotaurSureshot.class, AvenInitiate.class})
class MinotaurSureshotTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1}{R} gives +1/+0 until end of turn")
    void payingGivesBoost() {
        Permanent sureshot = addCreatureReady(player1, new MinotaurSureshot());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(sureshot.getPowerModifier()).isEqualTo(1);
        assertThat(sureshot.getToughnessModifier()).isEqualTo(0);
        assertThat(sureshot.getEffectivePower()).isEqualTo(3);
        assertThat(sureshot.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Activating twice stacks the boost")
    void activatingTwiceStacks() {
        Permanent sureshot = addCreatureReady(player1, new MinotaurSureshot());
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(sureshot.getEffectivePower()).isEqualTo(4);
        assertThat(sureshot.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent sureshot = addCreatureReady(player1, new MinotaurSureshot());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(sureshot.getEffectivePower()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(sureshot.getPowerModifier()).isEqualTo(0);
        assertThat(sureshot.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Reach allows blocking a flying creature")
    void canBlockFlyingCreature() {
        addCreatureReady(player1, new AvenInitiate());
        Permanent sureshot = addCreatureReady(player2, new MinotaurSureshot());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(sureshot.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The ability works while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent sureshot = harness.addToBattlefieldAndReturn(player1, new MinotaurSureshot());
        sureshot.setSummoningSick(true);
        sureshot.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(sureshot.getEffectivePower()).isEqualTo(2);
        harness.passBothPriorities();

        assertThat(sureshot.getEffectivePower()).isEqualTo(3);
        assertThat(sureshot.getEffectiveToughness()).isEqualTo(3);
        assertThat(sureshot.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Two generic mana cannot pay the red mana requirement")
    void cannotActivateWithoutRedMana() {
        Permanent sureshot = addCreatureReady(player1, new MinotaurSureshot());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.stack).isEmpty();
        assertThat(sureshot.getEffectivePower()).isEqualTo(2);
    }
}
