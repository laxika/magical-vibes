package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WallOfWonder.class, GrizzlyBears.class})
class WallOfWonderTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot attack without activating the ability (defender)")
    void cannotAttackWithDefender() {
        addCreatureReady(player1, new WallOfWonder());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Ability gives +4/-4 and lets the wall attack this turn")
    void abilityBoostsAndAllowsAttack() {
        Permanent wall = addCreatureReady(player1, new WallOfWonder());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(wall.getPowerModifier()).isEqualTo(4);
        assertThat(wall.getToughnessModifier()).isEqualTo(-4);
        assertThat(wall.getEffectivePower()).isEqualTo(5);
        assertThat(wall.getEffectiveToughness()).isEqualTo(1);

        declareAttackers(List.of(0));

        assertThat(wall.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Boost and attack permission wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent wall = addCreatureReady(player1, new WallOfWonder());
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(wall.getPowerModifier()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(wall.getPowerModifier()).isEqualTo(0);
        assertThat(wall.getToughnessModifier()).isEqualTo(0);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Activating the ability does not tap Wall of Wonder")
    void activationDoesNotTapWall() {
        Permanent wall = addCreatureReady(player1, new WallOfWonder());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wall.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate the ability without two blue mana")
    void cannotActivateWithoutTwoBlueMana() {
        addCreatureReady(player1, new WallOfWonder());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate the ability without enough mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new WallOfWonder());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }
}
