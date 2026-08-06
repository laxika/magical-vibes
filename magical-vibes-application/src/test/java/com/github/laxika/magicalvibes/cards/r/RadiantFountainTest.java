package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RadiantFountainTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield gains its controller 2 life")
    void entersGainingTwoLife() {
        harness.setHand(player1, List.of(new RadiantFountain()));
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, startingLife + 2);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Tapping for mana adds {C}")
    void tapForColorlessMana() {
        harness.addToBattlefield(player1, new RadiantFountain());

        harness.activateAbility(player1, 0, 0, null, null);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }
}
