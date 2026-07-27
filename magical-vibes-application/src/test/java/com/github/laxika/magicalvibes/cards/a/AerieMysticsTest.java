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

class AerieMysticsTest extends BaseCardTest {

    @Test
    @DisplayName("Ability grants shroud to creatures you control, not the opponent's")
    void grantsShroudToOwnCreatures() {
        Permanent mystics = addCreatureReady(player1, new AerieMystics());
        Permanent ally = addCreatureReady(player1, new GrizzlyBears());
        Permanent enemy = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(mystics.hasKeyword(Keyword.SHROUD)).isTrue();
        assertThat(ally.hasKeyword(Keyword.SHROUD)).isTrue();
        assertThat(enemy.hasKeyword(Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Shroud wears off at end of turn")
    void shroudWearsOffAtEndOfTurn() {
        Permanent mystics = addCreatureReady(player1, new AerieMystics());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(mystics.hasKeyword(Keyword.SHROUD)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(mystics.hasKeyword(Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Ability requires {1}{G}{U}")
    void requiresEnoughMana() {
        addCreatureReady(player1, new AerieMystics());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }
}
