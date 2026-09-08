package com.github.laxika.magicalvibes.cards.v;

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

@CardUsed(ViashinoSlasher.class)
class ViashinoSlasherTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability gives Viashino Slasher +1/-1 until end of turn")
    void activatingAbilityBoostsSelf() {
        Permanent slasher = addReadySlasher(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(slasher.getPowerModifier()).isEqualTo(1);
        assertThat(slasher.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("The ability can be activated multiple times and its boosts stack")
    void boostsStack() {
        Permanent slasher = addReadySlasher(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(slasher.getPowerModifier()).isEqualTo(2);
        assertThat(slasher.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent slasher = addReadySlasher(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(slasher.getPowerModifier()).isZero();
        assertThat(slasher.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The ability requires red mana")
    void abilityRequiresRedMana() {
        addReadySlasher(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addReadySlasher(Player player) {
        Permanent permanent = new Permanent(new ViashinoSlasher());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
