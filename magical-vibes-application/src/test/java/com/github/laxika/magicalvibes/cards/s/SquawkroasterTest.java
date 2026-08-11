package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfAir;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SquawkroasterTest extends BaseCardTest {

    @Test
    @DisplayName("Power equals the number of distinct colors among controlled permanents")
    void powerEqualsDistinctControlledColors() {
        Permanent squawkroaster = addSquawkroaster(player1);

        assertThat(gqs.getEffectivePower(gd, squawkroaster)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, squawkroaster)).isEqualTo(4);

        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new AirElemental());
        harness.addToBattlefield(player1, new WallOfAir());

        assertThat(gqs.getEffectivePower(gd, squawkroaster)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, squawkroaster)).isEqualTo(4);
    }

    @Test
    @DisplayName("Counts each color once and ignores permanents controlled by an opponent")
    void countsDistinctColorsOnlyFromController() {
        Permanent squawkroaster = addSquawkroaster(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new AirElemental());
        harness.addToBattlefield(player2, new WallOfAir());

        assertThat(gqs.getEffectivePower(gd, squawkroaster)).isEqualTo(3);
    }

    @Test
    @DisplayName("Power updates when controlled permanents change")
    void powerUpdatesWithControlledPermanents() {
        Permanent squawkroaster = addSquawkroaster(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, squawkroaster)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(bears);

        assertThat(gqs.getEffectivePower(gd, squawkroaster)).isEqualTo(1);
    }

    private Permanent addSquawkroaster(Player player) {
        Permanent permanent = new Permanent(new Squawkroaster());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
