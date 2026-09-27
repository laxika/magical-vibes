package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(TalismanOfProgress.class)
class TalismanOfProgressTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for colorless mana adds {C} and deals no damage")
    void tapForColorlessMana() {
        Permanent talisman = harness.addToBattlefieldAndReturn(player1, new TalismanOfProgress());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(talisman.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        harness.assertLife(player1, lifeBefore);
    }

    @Test
    @DisplayName("Tapping for white mana adds {W} and deals 1 damage to controller")
    void tapForWhiteMana() {
        Permanent talisman = harness.addToBattlefieldAndReturn(player1, new TalismanOfProgress());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(talisman.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        harness.assertLife(player1, lifeBefore - 1);
    }

    @Test
    @DisplayName("Tapping for blue mana adds {U} and deals 1 damage to controller")
    void tapForBlueMana() {
        Permanent talisman = harness.addToBattlefieldAndReturn(player1, new TalismanOfProgress());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        assertThat(talisman.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        harness.assertLife(player1, lifeBefore - 1);
    }

    @Test
    @DisplayName("Colored mana damages only the controller")
    void coloredManaDamagesOnlyController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 17);
        harness.addToBattlefield(player1, new TalismanOfProgress());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "WHITE");

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhileTapped() {
        harness.addToBattlefield(player1, new TalismanOfProgress());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }
}
