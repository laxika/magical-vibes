package com.github.laxika.magicalvibes.cards.m;

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

@CardUsed(MobileFort.class)
class MobileFortTest extends BaseCardTest {

    private Permanent addFortReady() {
        return addCreatureReady(player1, new MobileFort());
    }

    @Test
    @DisplayName("Cannot attack without activating the ability")
    void cannotAttackWithDefender() {
        addFortReady();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Ability gives +3/-1 and lets Mobile Fort attack this turn")
    void abilityBoostsAndAllowsAttack() {
        Permanent fort = addFortReady();
        harness.addToBattlefield(player2, new MobileFort());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(fort.getEffectivePower()).isEqualTo(3);
        assertThat(fort.getEffectiveToughness()).isEqualTo(5);

        declareAttackers(List.of(0));

        assertThat(fort.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate more than once each turn")
    void onlyOncePerTurn() {
        Permanent fort = addFortReady();
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
        harness.passBothPriorities();

        assertThat(fort.getEffectivePower()).isEqualTo(3);
        assertThat(fort.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Boost and attack permission wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent fort = addFortReady();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(fort.getEffectivePower()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(fort.getEffectivePower()).isEqualTo(0);
        assertThat(fort.getEffectiveToughness()).isEqualTo(6);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Once-per-turn limit resets on the next turn")
    void oncePerTurnLimitResetsOnNextTurn() {
        Permanent fort = addFortReady();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(fort.getEffectivePower()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        advanceToUpkeep(player2);

        assertThat(fort.getEffectivePower()).isEqualTo(0);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(fort.getEffectivePower()).isEqualTo(3);
        assertThat(fort.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Cannot activate the ability without enough mana")
    void cannotActivateWithoutMana() {
        addFortReady();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }
}
