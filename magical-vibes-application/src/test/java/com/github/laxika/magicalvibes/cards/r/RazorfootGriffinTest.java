package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RazorfootGriffin.class, GrizzlyBears.class, GiantSpider.class})
class RazorfootGriffinTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a creature without flying or reach from blocking")
    void cannotBeBlockedByGroundCreature() {
        addCreatureReady(player1, new RazorfootGriffin());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("Flying creature can be blocked by another flying creature")
    void canBeBlockedByFlyingCreature() {
        addCreatureReady(player1, new RazorfootGriffin());
        Permanent blocker = addCreatureReady(player2, new RazorfootGriffin());

        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Flying creature can be blocked by a creature with reach")
    void canBeBlockedByCreatureWithReach() {
        Permanent blocker = addCreatureReady(player2, new GiantSpider());
        addCreatureReady(player1, new RazorfootGriffin());

        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("First strike deals combat damage before a creature without first strike")
    void firstStrikeDealsDamageFirst() {
        addCreatureReady(player1, new RazorfootGriffin());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();

        harness.assertOnBattlefield(player1, "Razorfoot Griffin");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("First strike deals combat damage before a creature without first strike when blocking")
    void firstStrikeDealsDamageFirstWhenBlocking() {
        addCreatureReady(player1, new RazorfootGriffin());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Razorfoot Griffin");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
