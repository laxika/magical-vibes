package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StormfrontPegasus.class, RuneclawBear.class, GiantSpider.class})
class StormfrontPegasusTest extends BaseCardTest {

    @Test
    void groundCreatureCannotBlockPegasus() {
        addCreatureReady(player1, new StormfrontPegasus());
        harness.addToBattlefield(player2, new RuneclawBear());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void flyingCreatureCanBlockPegasus() {
        addCreatureReady(player1, new StormfrontPegasus());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new StormfrontPegasus());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void reachCreatureCanBlockPegasus() {
        addCreatureReady(player1, new StormfrontPegasus());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void pegasusCanBlockGroundCreature() {
        addCreatureReady(player1, new RuneclawBear());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new StormfrontPegasus());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void pegasusDealsCombatDamagePastGroundCreature() {
        addCreatureReady(player1, new StormfrontPegasus());
        harness.addToBattlefield(player2, new RuneclawBear());
        int lifeBeforeCombat = gd.playerLifeTotals.get(player2.getId());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBeforeCombat - 2);
    }
}
