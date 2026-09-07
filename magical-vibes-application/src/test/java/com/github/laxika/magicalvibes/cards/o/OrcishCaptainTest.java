package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.i.IcatianPhalanx;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrcishCaptain.class, IcatianPhalanx.class})
class OrcishCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("Coin flip either pumps +2/+0 (win) or -0/-2 (loss) on the target Orc")
    void coinFlipAppliesBranch() {
        harness.addToBattlefield(player1, new OrcishCaptain());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new OrcishCaptain());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        boolean won = target.getPowerModifier() == 2 && target.getToughnessModifier() == 0;
        boolean lost = target.getPowerModifier() == 0 && target.getToughnessModifier() == -2;
        assertThat(won != lost)
                .as("target must have exactly one of the +2/+0 (win) or -0/-2 (loss) branches")
                .isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        if (won) {
            assertThat(gameLogContains("wins the coin flip")).isTrue();
        } else {
            assertThat(gameLogContains("loses the coin flip")).isTrue();
        }
    }

    @Test
    @DisplayName("The pump wears off at end of turn")
    void pumpWearsOff() {
        harness.addToBattlefield(player1, new OrcishCaptain());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new OrcishCaptain());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Can target an Orc creature controlled by an opponent")
    void canTargetOpponentsOrc() {
        harness.addToBattlefield(player1, new OrcishCaptain());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OrcishCaptain());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        boolean won = target.getPowerModifier() == 2 && target.getToughnessModifier() == 0;
        boolean lost = target.getPowerModifier() == 0 && target.getToughnessModifier() == -2;
        assertThat(won || lost).isTrue();
    }

    @Test
    @DisplayName("Requires a target Orc creature")
    void requiresTarget() {
        harness.addToBattlefield(player1, new OrcishCaptain());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Ability requires a target");
    }

    @Test
    @DisplayName("Cannot activate without the generic mana cost")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new OrcishCaptain());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new OrcishCaptain());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot target a non-Orc creature")
    void cannotTargetNonOrc() {
        harness.addToBattlefield(player1, new OrcishCaptain());
        Permanent nonOrc = harness.addToBattlefieldAndReturn(player1, new IcatianPhalanx());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, nonOrc.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Orc creature");
    }
}
