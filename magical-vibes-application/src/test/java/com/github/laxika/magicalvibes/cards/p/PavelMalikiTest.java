package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(PavelMaliki.class)
class PavelMalikiTest extends BaseCardTest {

    @Test
    @DisplayName("Pavel Maliki gets +1/+0 after paying black and red mana")
    void activationBoostsPower() {
        Permanent pavel = addPavelReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(pavel.getPowerModifier()).isEqualTo(1);
        assertThat(pavel.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Pavel Maliki can be activated repeatedly")
    void activationCanBeRepeated() {
        Permanent pavel = addPavelReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(pavel.getPowerModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Pavel Maliki's boost wears off at end of turn")
    void activationBoostWearsOff() {
        Permanent pavel = addPavelReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(pavel.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Pavel Maliki cannot be activated without both colors of mana")
    void activationRequiresBlackAndRedMana() {
        addPavelReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addPavelReady(Player player) {
        Permanent permanent = new Permanent(new PavelMaliki());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
