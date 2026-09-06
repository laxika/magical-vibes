package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RegalBunnicorn.class, Forest.class, GrizzlyBears.class})
class RegalBunnicornTest extends BaseCardTest {

    @Test
    @DisplayName("Regal Bunnicorn counts itself as a nonland permanent")
    void countsItself() {
        Permanent bunnicorn = addBunnicorn(player1);

        assertStats(bunnicorn, 1, 1);
    }

    @Test
    @DisplayName("Regal Bunnicorn counts your nonland permanents but not lands")
    void countsOwnNonlandPermanents() {
        Permanent bunnicorn = addBunnicorn(player1);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertStats(bunnicorn, 3, 3);
    }

    @Test
    @DisplayName("Regal Bunnicorn ignores an opponent's nonland permanents")
    void ignoresOpponentsNonlandPermanents() {
        Permanent bunnicorn = addBunnicorn(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertStats(bunnicorn, 1, 1);
    }

    @Test
    @DisplayName("Regal Bunnicorn updates as your nonland permanents change")
    void updatesWhenNonlandPermanentsChange() {
        Permanent bunnicorn = addBunnicorn(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertStats(bunnicorn, 2, 2);

        gd.playerBattlefields.get(player1.getId()).remove(bears);
        assertStats(bunnicorn, 1, 1);
    }

    private Permanent addBunnicorn(Player player) {
        Permanent permanent = new Permanent(new RegalBunnicorn());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void assertStats(Permanent bunnicorn, int power, int toughness) {
        assertThat(gqs.getEffectivePower(gd, bunnicorn)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, bunnicorn)).isEqualTo(toughness);
    }
}
