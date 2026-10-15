package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.CentaurCourser;
import com.github.laxika.magicalvibes.cards.n.NetcasterSpider;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LightningStormkin.class, CentaurCourser.class, NetcasterSpider.class})
class LightningStormkinTest extends BaseCardTest {

    @Test
    @DisplayName("Lightning Stormkin can attack the turn it resolves")
    void canAttackImmediatelyAfterResolving() {
        harness.castFromHand(player1, new LightningStormkin(), "{U}{R}");
        harness.passBothPriorities();
        Permanent stormkin = findPermanent(player1, "Lightning Stormkin");

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(stormkin.isAttacking()).isTrue();
        assertThat(stormkin.isTapped()).isTrue();
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("A creature without flying or reach cannot block Lightning Stormkin")
    void cannotBeBlockedByGroundCreature() {
        harness.addToBattlefield(player1, new LightningStormkin());
        addCreatureReady(player2, new CentaurCourser());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Another flying creature can block Lightning Stormkin")
    void canBeBlockedByFlyingCreature() {
        harness.addToBattlefield(player1, new LightningStormkin());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new LightningStormkin());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A creature with reach can block Lightning Stormkin")
    void canBeBlockedByReachCreature() {
        harness.addToBattlefield(player1, new LightningStormkin());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new NetcasterSpider());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Lightning Stormkin can block a ground creature even on the turn it enters")
    void canBlockGroundCreatureImmediately() {
        addCreatureReady(player1, new CentaurCourser());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new LightningStormkin());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
