package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PerilousShadow.class})
class PerilousShadowTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability gives +2/+2")
    void resolvingAbilityBoosts() {
        addCreatureReady(player1, new PerilousShadow());
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
        addCreatureReady(player1, new PerilousShadow());
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
        addCreatureReady(player1, new PerilousShadow());
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
        Permanent shadow = addCreatureReady(player1, new PerilousShadow());
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
        addCreatureReady(player1, new PerilousShadow());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Generic mana cannot replace the black part of the cost")
    void requiresBlackMana() {
        addCreatureReady(player1, new PerilousShadow());
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("A summoning-sick Shadow can pay with black and another color")
    void worksWithSummoningSicknessAndMixedMana() {
        Permanent shadow = harness.addToBattlefieldAndReturn(player1, new PerilousShadow());
        shadow.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(shadow.getEffectivePower()).isEqualTo(2);
        assertThat(shadow.getEffectiveToughness()).isEqualTo(6);
        assertThat(shadow.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The boost waits for resolution and affects only the source copy")
    void boostsOnlySourceOnResolution() {
        Permanent source = addCreatureReady(player1, new PerilousShadow());
        Permanent other = addCreatureReady(player1, new PerilousShadow());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(source.getEffectivePower()).isEqualTo(0);
        assertThat(source.getEffectiveToughness()).isEqualTo(4);

        harness.passBothPriorities();

        assertThat(source.getEffectivePower()).isEqualTo(2);
        assertThat(source.getEffectiveToughness()).isEqualTo(6);
        assertThat(other.getEffectivePower()).isEqualTo(0);
        assertThat(other.getEffectiveToughness()).isEqualTo(4);
    }
}
