package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StormcloudSpirit.class, GrizzlyBears.class, GiantSpider.class})
class StormcloudSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a non-flying creature from blocking Stormcloud Spirit")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        Permanent spirit = addCreatureReady(player1, new StormcloudSpirit());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(spirit);
    }

    @Test
    @DisplayName("Flying allows Stormcloud Spirit to be blocked by another flying creature")
    void canBeBlockedByFlyingCreature() {
        addCreatureReady(player1, new StormcloudSpirit());
        Permanent blocker = addCreatureReady(player2, new StormcloudSpirit());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Stormcloud Spirit can be blocked by a creature with reach")
    void canBeBlockedByCreatureWithReach() {
        addCreatureReady(player1, new StormcloudSpirit());
        Permanent blocker = addCreatureReady(player2, new GiantSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Stormcloud Spirit can block a creature without flying")
    void canBlockCreatureWithoutFlying() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new StormcloudSpirit());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
