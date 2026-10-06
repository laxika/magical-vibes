package com.github.laxika.magicalvibes.cards.m;

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

@CardUsed({Mistwalker.class})
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

    @Test
    @DisplayName("Repeated activations stack and affect only their source")
    void repeatedActivationsStackOnlyOnSource() {
        Permanent source = addReadyMistwalker(player1);
        Permanent other = addReadyMistwalker(player1);
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        assertThat(source.getEffectivePower()).isEqualTo(1);
        assertThat(source.getEffectiveToughness()).isEqualTo(4);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(source.getEffectivePower()).isEqualTo(3);
        assertThat(source.getEffectiveToughness()).isEqualTo(2);
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Four activations put Mistwalker into the graveyard for zero toughness")
    void repeatedActivationsCanReduceToughnessToZero() {
        addReadyMistwalker(player1);
        harness.addMana(player1, ManaColor.BLUE, 8);

        for (int i = 0; i < 4; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        harness.assertNotOnBattlefield(player1, "Mistwalker");
        harness.assertInGraveyard(player1, "Mistwalker");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Mistwalker can activate its ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent mistwalker = harness.addToBattlefieldAndReturn(player1, new Mistwalker());
        mistwalker.setSummoningSick(true);
        mistwalker.tap();
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(mistwalker.getEffectivePower()).isEqualTo(2);
        assertThat(mistwalker.getEffectiveToughness()).isEqualTo(3);
        assertThat(mistwalker.isTapped()).isTrue();
    }

    private Permanent addReadyMistwalker(Player player) {
        return addCreatureReady(player, new Mistwalker());
    }
}
