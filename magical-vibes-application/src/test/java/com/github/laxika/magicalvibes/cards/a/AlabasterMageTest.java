package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AlabasterMageTest extends BaseCardTest {

    @Test
    @DisplayName("Ability grants lifelink to target creature you control")
    void grantsLifelinkToOwnCreature() {
        addCreatureReady(player1, new AlabasterMage());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Granted lifelink wears off at end of turn")
    void lifelinkWearsOff() {
        addCreatureReady(player1, new AlabasterMage());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        addCreatureReady(player1, new AlabasterMage());
        Permanent oppBears = addCreatureReady(player2, new GrizzlyBears());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, oppBears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability requires {1}{W}")
    void requiresMana() {
        addCreatureReady(player1, new AlabasterMage());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
