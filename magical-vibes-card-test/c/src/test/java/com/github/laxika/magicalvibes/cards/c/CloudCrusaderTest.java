package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.s.StormfrontPegasus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CloudCrusader.class, GiantSpider.class, RuneclawBear.class, StormfrontPegasus.class})
class CloudCrusaderTest extends BaseCardTest {

    @Test
    void groundCreatureCannotBlock() {
        addCreatureReady(player1, new CloudCrusader());
        addCreatureReady(player2, new RuneclawBear());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    void killsFlyingBlockerBeforeRegularDamage() {
        Permanent crusader = addCreatureReady(player1, new CloudCrusader());
        addCreatureReady(player2, new StormfrontPegasus());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Cloud Crusader");
        harness.assertInGraveyard(player2, "Stormfront Pegasus");
        assertThat(crusader.getMarkedDamage()).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void canBlockGroundCreatureAndKillItBeforeRegularDamage() {
        addCreatureReady(player1, new RuneclawBear());
        Permanent crusader = addCreatureReady(player2, new CloudCrusader());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Runeclaw Bear");
        harness.assertOnBattlefield(player2, "Cloud Crusader");
        assertThat(crusader.getMarkedDamage()).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void reachBlockerSurvivesFirstStrikeAndDealsRegularDamage() {
        Permanent crusader = addCreatureReady(player1, new CloudCrusader());
        Permanent spider = addCreatureReady(player2, new GiantSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Cloud Crusader");
        harness.assertOnBattlefield(player2, "Giant Spider");
        assertThat(crusader.getMarkedDamage()).isEqualTo(2);
        assertThat(spider.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void unblockedFirstStrikerDealsDamageOnlyOnce() {
        addCreatureReady(player1, new CloudCrusader());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
}
