package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KnightOfTheSkywardEyeTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability gives +3/+3 until end of turn")
    void activatingAbilityBoosts() {
        Permanent knight = addReadyKnight(player1);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(5);
    }

    @Test
    @DisplayName("Can only be activated once each turn")
    void onlyOncePerTurn() {
        addReadyKnight(player1);
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no more than 1 times each turn");
    }

    @Test
    @DisplayName("Boost wears off at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        Permanent knight = addReadyKnight(player1);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(knight.getPowerModifier()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(knight.getPowerModifier()).isEqualTo(0);
        assertThat(knight.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Activation limit resets on a new turn")
    void activationLimitResetsOnNewTurn() {
        addReadyKnight(player1);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Can activate while summoning sick (no tap cost)")
    void canActivateWhileSummoningSick() {
        KnightOfTheSkywardEye card = new KnightOfTheSkywardEye();
        Permanent knight = new Permanent(card);
        knight.setSummoningSick(true);
        gd.playerBattlefields.get(player1.getId()).add(knight);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(5);
    }

    private Permanent addReadyKnight(Player player) {
        KnightOfTheSkywardEye card = new KnightOfTheSkywardEye();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
