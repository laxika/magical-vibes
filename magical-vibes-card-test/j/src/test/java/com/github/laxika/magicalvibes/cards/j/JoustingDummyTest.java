package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(JoustingDummy.class)
class JoustingDummyTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {3} gives Jousting Dummy +1/+0 until end of turn")
    void activatedAbilityBoostsSelfUntilEndOfTurn() {
        Permanent dummy = addCreatureReady(player1, new JoustingDummy());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(dummy.getPowerModifier()).isEqualTo(2);
        assertThat(dummy.getToughnessModifier()).isZero();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(dummy.getPowerModifier()).isZero();
        assertThat(dummy.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The boost uses the stack and applies only to the activating Dummy")
    void boostsOnlyItsSourceAfterResolution() {
        Permanent source = addCreatureReady(player1, new JoustingDummy());
        Permanent other = addCreatureReady(player1, new JoustingDummy());
        Permanent opponent = addCreatureReady(player2, new JoustingDummy());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(source.getPowerModifier()).isZero();

        harness.passBothPriorities();

        assertThat(source.getPowerModifier()).isEqualTo(1);
        assertThat(source.getToughnessModifier()).isZero();
        assertThat(other.getPowerModifier()).isZero();
        assertThat(opponent.getPowerModifier()).isZero();
        assertThat(source.isTapped()).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Dummy can activate its ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent dummy = harness.addToBattlefieldAndReturn(player1, new JoustingDummy());
        dummy.setSummoningSick(true);
        dummy.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(dummy.getPowerModifier()).isEqualTo(1);
        assertThat(dummy.getToughnessModifier()).isZero();
        assertThat(dummy.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Two mana cannot pay for the ability")
    void cannotActivateWithInsufficientMana() {
        Permanent dummy = addCreatureReady(player1, new JoustingDummy());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.stack).isEmpty();
        assertThat(dummy.getPowerModifier()).isZero();
    }
}
