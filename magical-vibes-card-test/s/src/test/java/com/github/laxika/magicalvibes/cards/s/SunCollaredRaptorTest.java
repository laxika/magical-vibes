package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed({SunCollaredRaptor.class})
class SunCollaredRaptorTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability gives +3/+0 until end of turn")
    void resolvingAbilityBoostsSelf() {
        Permanent raptor = addReadyRaptor(player1);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(raptor.getPowerModifier()).isEqualTo(3);
        assertThat(raptor.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Ability can be activated repeatedly for a cumulative boost")
    void repeatedActivationsStack() {
        Permanent raptor = addReadyRaptor(player1);
        harness.addMana(player1, ManaColor.RED, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(raptor.getPowerModifier()).isEqualTo(6);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent raptor = addReadyRaptor(player1);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(raptor.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate the ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyRaptor(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Ability requires red mana even when enough total mana is available")
    void cannotActivateWithoutRedMana() {
        addReadyRaptor(player1);
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapped, summoning-sick raptor can activate using generic and red mana")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent raptor = harness.addToBattlefieldAndReturn(player1, new SunCollaredRaptor());
        raptor.setSummoningSick(true);
        raptor.tap();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(raptor.getPowerModifier()).isZero();
        harness.passBothPriorities();

        assertThat(raptor.getPowerModifier()).isEqualTo(3);
        assertThat(raptor.getToughnessModifier()).isZero();
        assertThat(raptor.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Boost affects only the raptor that activated the ability")
    void boostsOnlySource() {
        Permanent source = addReadyRaptor(player1);
        Permanent other = addReadyRaptor(player1);
        Permanent opponent = addReadyRaptor(player2);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(source.getPowerModifier()).isEqualTo(3);
        assertThat(other.getPowerModifier()).isZero();
        assertThat(opponent.getPowerModifier()).isZero();
    }

    private Permanent addReadyRaptor(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new SunCollaredRaptor());
        perm.setSummoningSick(false);
        return perm;
    }
}
