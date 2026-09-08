package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MistwalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability gives Mistwalker +1/-1 until end of turn")
    void resolvingAbilityBoostsPowerAndReducesToughness() {
        Permanent mistwalker = addReadyMistwalker(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(mistwalker.getPowerModifier()).isEqualTo(1);
        assertThat(mistwalker.getToughnessModifier()).isEqualTo(-1);
        assertThat(mistwalker.getEffectivePower()).isEqualTo(2);
        assertThat(mistwalker.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("The temporary boost resets at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent mistwalker = addReadyMistwalker(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(mistwalker.getPowerModifier()).isEqualTo(1);
        assertThat(mistwalker.getToughnessModifier()).isEqualTo(-1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(mistwalker.getPowerModifier()).isEqualTo(0);
        assertThat(mistwalker.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The ability cannot be activated without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyMistwalker(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addReadyMistwalker(Player player) {
        Permanent mistwalker = new Permanent(new Mistwalker());
        mistwalker.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(mistwalker);
        return mistwalker;
    }
}
