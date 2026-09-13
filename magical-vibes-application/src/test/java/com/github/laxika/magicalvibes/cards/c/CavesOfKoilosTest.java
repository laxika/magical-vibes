package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(CavesOfKoilos.class)
class CavesOfKoilosTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for colorless adds {C} and does not deal damage")
    void tapForColorlessAddsManaNoDamage() {
        harness.setLife(player1, 20);
        Permanent caves = addReadyCaves(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        harness.assertLife(player1, 20);
        assertThat(caves.isTapped()).isTrue();
        // Mana ability — does not use the stack
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapping for white adds {W} and deals 1 damage to controller")
    void tapForWhiteAddsManaAndDealsDamage() {
        harness.setLife(player1, 20);
        addReadyCaves(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        harness.assertLife(player1, 19);
        // Mana ability — does not use the stack
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapping for black adds {B} and deals 1 damage to controller")
    void tapForBlackAddsManaAndDealsDamage() {
        harness.setLife(player1, 20);
        addReadyCaves(player1);

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        harness.assertLife(player1, 19);
        // Mana ability — does not use the stack
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenTapped() {
        addReadyCaves(player1);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Multiple pain land activations across turns accumulate damage")
    void cumulativeDamageAcrossTurns() {
        harness.setLife(player1, 20);
        Permanent caves = addReadyCaves(player1);

        // Tap for white — 1 damage
        harness.activateAbility(player1, 0, 1, null, null);
        harness.assertLife(player1, 19);

        // Untap and tap for black — 1 more damage
        caves.untap();
        harness.activateAbility(player1, 0, 2, null, null);
        harness.assertLife(player1, 18);

        // Untap and tap for colorless — no damage
        caves.untap();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Pain damage affects only the activating controller")
    void painDamageAffectsOnlyActivatingController() {
        addReadyCaves(player1);
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertLife(player2, opponentLifeBefore);
    }

    private Permanent addReadyCaves(Player player) {
        return harness.addToBattlefieldAndReturn(player, new CavesOfKoilos());
    }
}
