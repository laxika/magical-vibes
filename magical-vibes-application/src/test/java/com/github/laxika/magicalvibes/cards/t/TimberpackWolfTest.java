package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TimberpackWolfTest extends BaseCardTest {

    @Test
    @DisplayName("Timberpack Wolf is 2/2 when it is the only one")
    void isBaseStatsAlone() {
        Permanent wolf = addWolfReady(player1);

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(2);
    }

    @Test
    @DisplayName("Timberpack Wolf gets +1/+1 for each other Timberpack Wolf you control")
    void countsOwnOtherWolves() {
        Permanent wolf = addWolfReady(player1);
        harness.addToBattlefield(player1, new TimberpackWolf());
        harness.addToBattlefield(player1, new TimberpackWolf());

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(4);
    }

    @Test
    @DisplayName("Timberpack Wolf does not count opponents' Timberpack Wolves")
    void ignoresOpponentWolves() {
        Permanent wolf = addWolfReady(player1);
        harness.addToBattlefield(player2, new TimberpackWolf());
        harness.addToBattlefield(player2, new TimberpackWolf());

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(2);
    }

    @Test
    @DisplayName("Timberpack Wolf does not count creatures with different names")
    void ignoresDifferentNames() {
        Permanent wolf = addWolfReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(2);
    }

    @Test
    @DisplayName("Timberpack Wolf bonus shrinks when another Timberpack Wolf leaves the battlefield")
    void bonusUpdatesWhenOtherWolfLeaves() {
        Permanent wolf = addWolfReady(player1);
        harness.addToBattlefield(player1, new TimberpackWolf());
        harness.addToBattlefield(player1, new TimberpackWolf());

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> !p.getId().equals(wolf.getId()) && p.getCard().getName().equals("Timberpack Wolf"));

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(2);
    }

    private Permanent addWolfReady(Player player) {
        Permanent permanent = new Permanent(new TimberpackWolf());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
