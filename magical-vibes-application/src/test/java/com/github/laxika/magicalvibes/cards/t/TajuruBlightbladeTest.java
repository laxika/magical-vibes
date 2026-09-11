package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TajuruBlightblade.class, GrizzlyBears.class})
class TajuruBlightbladeTest extends BaseCardTest {

    @Test
    void hasDeathtouch() {
        Permanent blightblade = harness.addToBattlefieldAndReturn(player1, new TajuruBlightblade());

        assertThat(gqs.hasKeyword(gd, blightblade, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    void deathtouchDestroysLargerBlocker() {
        Permanent blightblade = addCreatureReady(player1, new TajuruBlightblade());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(blightblade)));
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(blightblade))));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(blightblade);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }
}
