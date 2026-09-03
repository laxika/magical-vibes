package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(AdarkarWastes.class)
class AdarkarWastesTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for colorless adds {C} and does not deal damage")
    void tapForColorlessAddsManaNoDamage() {
        harness.setLife(player1, 20);
        Permanent wastes = addReadyWastes(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(wastes.isTapped()).isTrue();
        // Mana ability — does not use the stack
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapping for white adds {W} and deals 1 damage to controller")
    void tapForWhiteAddsManaAndDealsDamage() {
        harness.setLife(player1, 20);
        addReadyWastes(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        // Mana ability — does not use the stack
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapping for blue adds {U} and deals 1 damage to controller")
    void tapForBlueAddsManaAndDealsDamage() {
        harness.setLife(player1, 20);
        addReadyWastes(player1);

        harness.activateAbility(player1, 0, 2, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        // Mana ability — does not use the stack
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent wastes = addReadyWastes(player1);
        wastes.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Multiple pain land activations across turns accumulate damage")
    void cumulativeDamageAcrossTurns() {
        harness.setLife(player1, 20);
        Permanent wastes = addReadyWastes(player1);

        // Tap for white — 1 damage
        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(19);

        // Untap and tap for blue — 1 more damage
        wastes.untap();
        harness.activateAbility(player1, 0, 2, null, null);
        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(18);

        // Untap and tap for colorless — no damage
        wastes.untap();
        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    private Permanent addReadyWastes(Player player) {
        return harness.addToBattlefieldAndReturn(player, new AdarkarWastes());
    }
}
