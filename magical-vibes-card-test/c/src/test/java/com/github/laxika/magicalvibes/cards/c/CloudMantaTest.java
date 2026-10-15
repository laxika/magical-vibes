package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantMantis;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CloudManta.class, GrizzlyBears.class, GiantMantis.class, CoralhelmGuide.class})
class CloudMantaTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a nonflying creature from blocking Cloud Manta")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        addCreatureReady(player1, new CloudManta());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(flying)");
    }

    @Test
    @DisplayName("A flying creature can block Cloud Manta")
    void flyingCreatureCanBlock() {
        addCreatureReady(player1, new CloudManta());
        Permanent blocker = addCreatureReady(player2, new CloudManta());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A creature with reach can block Cloud Manta")
    void reachCreatureCanBlock() {
        addCreatureReady(player1, new CloudManta());
        Permanent blocker = addCreatureReady(player2, new GiantMantis());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Cloud Manta can block a creature without flying")
    void canBlockGroundCreature() {
        addCreatureReady(player1, new CoralhelmGuide());
        Permanent blocker = addCreatureReady(player2, new CloudManta());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
