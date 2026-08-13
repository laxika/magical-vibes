package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShivanGorgeTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for mana adds one colorless mana")
    void tapsForColorless() {
        harness.addToBattlefield(player1, new ShivanGorge());
        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Paying the activated ability deals 1 damage to each opponent")
    void activatedAbilityDamagesEachOpponent() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new ShivanGorge());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(land.isTapped()).isTrue();
    }
}
