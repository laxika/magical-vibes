package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BorealCentaurTest extends BaseCardTest {

    @Test
    @DisplayName("Snow activation gives Boreal Centaur +1/+1 until end of turn")
    void snowActivationBoostsSelf() {
        Permanent centaur = addReadyCentaur(player1);
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(centaur.getEffectivePower()).isEqualTo(3);
        assertThat(centaur.getEffectiveToughness()).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).getSnowManaTotal()).isZero();
    }

    @Test
    @DisplayName("Boreal Centaur can activate only once each turn and the boost wears off at cleanup")
    void onceEachTurnAndTemporary() {
        Permanent centaur = addReadyCentaur(player1);
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(centaur.getEffectivePower()).isEqualTo(2);
        assertThat(centaur.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Boreal Centaur cannot activate with only nonsnow mana")
    void requiresSnowMana() {
        addReadyCentaur(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addReadyCentaur(Player player) {
        Permanent permanent = new Permanent(new BorealCentaur());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
