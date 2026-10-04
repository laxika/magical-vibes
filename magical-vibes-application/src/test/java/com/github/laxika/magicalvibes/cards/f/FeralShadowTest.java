package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FeralShadow.class, GrizzlyBears.class, GiantSpider.class})
class FeralShadowTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a non-flying creature from blocking Feral Shadow")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        addCreatureReady(player1, new FeralShadow());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void flyingCreatureCanBlockNonFlyingCreature() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new FeralShadow());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A creature with flying can block Feral Shadow")
    void flyingCreatureCanBlock() {
        addCreatureReady(player1, new FeralShadow());
        var blocker = addCreatureReady(player2, new FeralShadow());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A creature with reach can block Feral Shadow")
    void reachCreatureCanBlock() {
        addCreatureReady(player1, new FeralShadow());
        var blocker = addCreatureReady(player2, new GiantSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
