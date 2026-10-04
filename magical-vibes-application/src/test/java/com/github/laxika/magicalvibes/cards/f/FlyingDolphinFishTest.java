package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DutifulKnowledgeSeeker;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlyingDolphinFish.class, DutifulKnowledgeSeeker.class, FrogSquirrels.class})
class FlyingDolphinFishTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a non-flying creature from blocking Flying Dolphin-Fish")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        Permanent dolphinFish = addCreatureReady(player1, new FlyingDolphinFish());
        addCreatureReady(player2, new DutifulKnowledgeSeeker());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(dolphinFish);
    }

    @Test
    @DisplayName("Flying allows Flying Dolphin-Fish to be blocked by another flying creature")
    void canBeBlockedByFlyingCreature() {
        addCreatureReady(player1, new FlyingDolphinFish());
        Permanent blocker = addCreatureReady(player2, new FlyingDolphinFish());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Flying Dolphin-Fish can be blocked by a creature with reach")
    void canBeBlockedByReachCreature() {
        addCreatureReady(player1, new FlyingDolphinFish());
        Permanent blocker = addCreatureReady(player2, new FrogSquirrels());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Flying Dolphin-Fish can block a creature without flying")
    void canBlockNonFlyingCreature() {
        addCreatureReady(player1, new DutifulKnowledgeSeeker());
        Permanent blocker = addCreatureReady(player2, new FlyingDolphinFish());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
