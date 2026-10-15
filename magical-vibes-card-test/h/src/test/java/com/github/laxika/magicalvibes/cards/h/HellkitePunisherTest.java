package com.github.laxika.magicalvibes.cards.h;

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

@CardUsed({HellkitePunisher.class})
class HellkitePunisherTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability gives +1/+0 until end of turn")
    void resolvingAbilityBoostsSelf() {
        Permanent hellkite = addReadyHellkite(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(hellkite.getPowerModifier()).isEqualTo(1);
        assertThat(hellkite.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Ability can be activated repeatedly for a cumulative power boost")
    void repeatedActivationsStack() {
        Permanent hellkite = addReadyHellkite(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(hellkite.getPowerModifier()).isEqualTo(2);
        assertThat(hellkite.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate the ability without red mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyHellkite(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Power boost wears off at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent hellkite = addReadyHellkite(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(hellkite.getPowerModifier()).isEqualTo(0);
        assertThat(hellkite.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Hellkite can activate its ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent hellkite = harness.addToBattlefieldAndReturn(player1, new HellkitePunisher());
        hellkite.setSummoningSick(true);
        hellkite.tap();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(hellkite.getPowerModifier()).isEqualTo(1);
        assertThat(hellkite.getToughnessModifier()).isEqualTo(0);
        assertThat(hellkite.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability boosts only its source and only after resolving")
    void boostsOnlySourceOnResolution() {
        Permanent hellkite = addReadyHellkite(player1);
        Permanent other = addReadyHellkite(player1);
        Permanent opposing = addReadyHellkite(player2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(hellkite.getPowerModifier()).isEqualTo(0);
        harness.passBothPriorities();

        assertThat(hellkite.getPowerModifier()).isEqualTo(1);
        assertThat(other.getPowerModifier()).isEqualTo(0);
        assertThat(opposing.getPowerModifier()).isEqualTo(0);
    }

    private Permanent addReadyHellkite(Player player) {
        return addCreatureReady(player, new HellkitePunisher());
    }
}
