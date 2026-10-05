package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(PanickedAltisaur.class)
class PanickedAltisaurTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability deals 2 damage to each opponent")
    void tapAbilityDealsDamage() {
        addReadyAltisaur(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Tap ability taps Panicked Altisaur")
    void tapAbilityTapsCreature() {
        Permanent altisaur = addReadyAltisaur(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(altisaur.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Summoning sickness prevents paying the tap cost")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new PanickedAltisaur());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Altisaur cannot activate again")
    void cannotActivateAgainWhileTapped() {
        addReadyAltisaur(player1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The ability damages the opponent of its controller only on resolution")
    void opposingControllerDealsDamageOnResolution() {
        addReadyAltisaur(player2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player2, 0, null, null);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    private Permanent addReadyAltisaur(Player player) {
        return addCreatureReady(player, new PanickedAltisaur());
    }
}
