package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PerilousShadowTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability gives +2/+2")
    void resolvingAbilityBoosts() {
        addShadow(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent shadow = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(shadow.getEffectivePower()).isEqualTo(2);
        assertThat(shadow.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("The ability can be activated repeatedly")
    void canActivateMultipleTimes() {
        addShadow(player1);
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent shadow = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(shadow.getEffectivePower()).isEqualTo(4);
        assertThat(shadow.getEffectiveToughness()).isEqualTo(8);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOff() {
        addShadow(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent shadow = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(shadow.getEffectivePower()).isEqualTo(0);
        assertThat(shadow.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("The ability does not tap and works while tapped")
    void worksWhileTapped() {
        Permanent shadow = addShadow(player1);
        shadow.tap();
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Cannot activate the ability without enough mana")
    void cannotActivateWithoutMana() {
        addShadow(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addShadow(Player player) {
        PerilousShadow card = new PerilousShadow();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
