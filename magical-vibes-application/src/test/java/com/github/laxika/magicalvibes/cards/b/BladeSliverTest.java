package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AvenEnvoy;
import com.github.laxika.magicalvibes.cards.s.ShiftingSliver;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BladeSliver.class, ShiftingSliver.class, AvenEnvoy.class})
class BladeSliverTest extends BaseCardTest {

    @Test
    void boostsAllSliversIncludingItself() {
        Permanent ownSliver = addCreatureReady(player1, new BladeSliver());
        Permanent opponentSliver = addCreatureReady(player2, new ShiftingSliver());

        assertThat(gqs.getEffectivePower(gd, ownSliver)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownSliver)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentSliver)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentSliver)).isEqualTo(2);
    }

    @Test
    void doesNotBoostNonSliverCreatures() {
        addCreatureReady(player1, new BladeSliver());
        Permanent envoy = addCreatureReady(player1, new AvenEnvoy());

        assertThat(gqs.getEffectivePower(gd, envoy)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, envoy)).isEqualTo(2);
    }

    @Test
    void bonusDisappearsWhenSourceLeavesBattlefield() {
        Permanent bladeSliver = addCreatureReady(player1, new BladeSliver());
        Permanent sliver = addCreatureReady(player2, new ShiftingSliver());

        assertThat(gqs.getEffectivePower(gd, sliver)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(bladeSliver);

        assertThat(gqs.getEffectivePower(gd, sliver)).isEqualTo(2);
    }
}
