package com.github.laxika.magicalvibes.cards.g;

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

@CardUsed(GhorClanBloodscale.class)
class GhorClanBloodscaleTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability gives Ghor-Clan Bloodscale +2/+2 until end of turn")
    void resolvingAbilityBoostsSelf() {
        Permanent bloodscale = addReadyBloodscale(player1);
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bloodscale.getPowerModifier()).isEqualTo(2);
        assertThat(bloodscale.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("The ability cannot be activated without green mana")
    void cannotActivateWithoutGreenMana() {
        Permanent bloodscale = addReadyBloodscale(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(bloodscale.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Activating the ability does not tap Ghor-Clan Bloodscale")
    void activationDoesNotTapSource() {
        Permanent bloodscale = addReadyBloodscale(player1);
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(bloodscale.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The ability can be activated only once each turn")
    void cannotActivateMoreThanOnceEachTurn() {
        addReadyBloodscale(player1);
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("The boost wears off at end of turn and the ability becomes available again")
    void boostWearsOffAndActivationResets() {
        Permanent bloodscale = addReadyBloodscale(player1);
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        assertThat(bloodscale.getPowerModifier()).isEqualTo(0);
        assertThat(bloodscale.getToughnessModifier()).isEqualTo(0);

        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
    }

    private Permanent addReadyBloodscale(Player player) {
        return addCreatureReady(player, new GhorClanBloodscale());
    }

    private void addAbilityMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 3);
        harness.addMana(player, ManaColor.GREEN, 1);
    }
}
