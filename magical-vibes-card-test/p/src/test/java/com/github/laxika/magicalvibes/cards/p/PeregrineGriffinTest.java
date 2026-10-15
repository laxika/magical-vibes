package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.s.StormfrontPegasus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PeregrineGriffin.class, GoblinPiker.class, GiantSpider.class, StormfrontPegasus.class})
class PeregrineGriffinTest extends BaseCardTest {

    @Test
    void groundCreatureCannotBlock() {
        addCreatureReady(player1, new PeregrineGriffin());
        addCreatureReady(player2, new GoblinPiker());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    void flyingBlockerDiesBeforeDealingDamage() {
        Permanent griffin = addCreatureReady(player1, new PeregrineGriffin());
        addCreatureReady(player2, new StormfrontPegasus());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Peregrine Griffin");
        harness.assertInGraveyard(player2, "Stormfront Pegasus");
        assertThat(griffin.getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
    }

    @Test
    void reachCreatureCanBlockAndDealRegularDamage() {
        Permanent griffin = addCreatureReady(player1, new PeregrineGriffin());
        Permanent spider = addCreatureReady(player2, new GiantSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Peregrine Griffin");
        harness.assertOnBattlefield(player2, "Giant Spider");
        assertThat(griffin.getMarkedDamage()).isEqualTo(2);
        assertThat(spider.getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player2, 20);
    }

    @Test
    void firstStrikeBlockerKillsGroundAttackerBeforeRegularDamage() {
        addCreatureReady(player1, new GoblinPiker());
        Permanent griffin = addCreatureReady(player2, new PeregrineGriffin());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Goblin Piker");
        harness.assertOnBattlefield(player2, "Peregrine Griffin");
        assertThat(griffin.getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
    }

    @Test
    void unblockedFirstStrikeCreatureDealsDamageOnlyOnce() {
        addCreatureReady(player1, new PeregrineGriffin());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player2, 18);
    }
}
