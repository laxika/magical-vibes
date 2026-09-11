package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DiamondValley.class, GrizzlyBears.class})
class DiamondValleyTest extends BaseCardTest {

    @Test
    void sacrificesCreatureAndGainsItsToughness() {
        Permanent valley = harness.addToBattlefieldAndReturn(player1, new DiamondValley());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 10);

        harness.activateAbility(player1, indexOf(player1, valley), null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 12);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void cannotActivateWithoutCreatureToSacrifice() {
        Permanent valley = harness.addToBattlefieldAndReturn(player1, new DiamondValley());

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, valley), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
