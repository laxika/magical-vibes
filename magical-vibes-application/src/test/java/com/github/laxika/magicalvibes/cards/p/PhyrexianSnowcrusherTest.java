package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(PhyrexianSnowcrusher.class)
class PhyrexianSnowcrusherTest extends BaseCardTest {

    @Test
    @DisplayName("Phyrexian Snowcrusher must attack each combat when able")
    void mustAttackWhenAble() {
        addReadySnowcrusher(player1);

        assertThatThrownBy(() -> declareAttackers(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("A tapped Snowcrusher does not have to attack")
    void doesNotHaveToAttackWhenTapped() {
        Permanent snowcrusher = addReadySnowcrusher(player1);
        snowcrusher.tap();

        declareAttackers(List.of());

        assertThat(snowcrusher.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Snow mana activates the power boost")
    void snowManaActivatesPowerBoost() {
        Permanent snowcrusher = addReadySnowcrusher(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 1);
        int originalPower = gqs.getEffectivePower(gd, snowcrusher);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, snowcrusher)).isEqualTo(originalPower + 1);
        assertThat(gd.playerManaPools.get(player1.getId()).getSnowManaTotal()).isZero();
    }

    @Test
    @DisplayName("The power boost wears off at end of turn")
    void powerBoostWearsOffAtEndOfTurn() {
        Permanent snowcrusher = addReadySnowcrusher(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 1);
        int originalPower = gqs.getEffectivePower(gd, snowcrusher);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, snowcrusher)).isEqualTo(originalPower + 1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, snowcrusher)).isEqualTo(originalPower);
    }

    @Test
    @DisplayName("Regular mana cannot pay the snow activation cost")
    void regularManaCannotPaySnowCost() {
        addReadySnowcrusher(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addReadySnowcrusher(Player player) {
        return addCreatureReady(player, new PhyrexianSnowcrusher());
    }

    @Test
    @DisplayName("A summoning-sick Snowcrusher does not have to attack")
    void doesNotHaveToAttackWhileSummoningSick() {
        Permanent snowcrusher = harness.addToBattlefieldAndReturn(player1, new PhyrexianSnowcrusher());
        snowcrusher.setSummoningSick(true);

        declareAttackers(List.of());

        assertThat(snowcrusher.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Colored snow mana can pay both activation symbols")
    void coloredSnowManaCanPayEntireActivation() {
        Permanent snowcrusher = addReadySnowcrusher(player1);
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.BLUE, 2);
        int originalPower = gqs.getEffectivePower(gd, snowcrusher);
        int originalToughness = gqs.getEffectiveToughness(gd, snowcrusher);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, snowcrusher)).isEqualTo(originalPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, snowcrusher)).isEqualTo(originalToughness);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("One snow mana cannot pay both the generic and snow symbols")
    void oneSnowManaIsInsufficient() {
        addReadySnowcrusher(player1);
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("A tapped summoning-sick Snowcrusher can activate repeatedly")
    void powerBoostStacksWithoutTapOrSummoningSicknessRestriction() {
        Permanent snowcrusher = addReadySnowcrusher(player1);
        snowcrusher.tap();
        snowcrusher.setSummoningSick(true);
        int originalPower = gqs.getEffectivePower(gd, snowcrusher);
        int originalToughness = gqs.getEffectiveToughness(gd, snowcrusher);

        for (int i = 0; i < 2; i++) {
            harness.addMana(player1, ManaColor.COLORLESS, 1);
            gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.RED, 1);
            harness.activateAbility(player1, 0, 0, null, null);
            harness.passBothPriorities();
        }

        assertThat(gqs.getEffectivePower(gd, snowcrusher)).isEqualTo(originalPower + 2);
        assertThat(gqs.getEffectiveToughness(gd, snowcrusher)).isEqualTo(originalToughness);
        assertThat(snowcrusher.isTapped()).isTrue();
    }
}
