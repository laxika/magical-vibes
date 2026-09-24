package com.github.laxika.magicalvibes.cards.i;

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

@CardUsed(InkriseInfiltrator.class)
class InkriseInfiltratorTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability gives +2/+2 until end of turn")
    void abilityBoostsSelf() {
        Permanent infiltrator = addReadyInkriseInfiltrator(player1);
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(infiltrator.getEffectivePower()).isEqualTo(3);
        assertThat(infiltrator.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("The ability can be activated repeatedly")
    void abilityStacks() {
        Permanent infiltrator = addReadyInkriseInfiltrator(player1);
        harness.addMana(player1, ManaColor.BLACK, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(infiltrator.getEffectivePower()).isEqualTo(5);
        assertThat(infiltrator.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOff() {
        Permanent infiltrator = addReadyInkriseInfiltrator(player1);
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(infiltrator.getPowerModifier()).isZero();
        assertThat(infiltrator.getToughnessModifier()).isZero();
        assertThat(infiltrator.getEffectivePower()).isEqualTo(1);
        assertThat(infiltrator.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The ability cannot be activated without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyInkriseInfiltrator(player1);
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addReadyInkriseInfiltrator(Player player) {
        Permanent permanent = new Permanent(new InkriseInfiltrator());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
