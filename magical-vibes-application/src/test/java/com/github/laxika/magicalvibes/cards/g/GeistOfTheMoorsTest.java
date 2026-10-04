package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.n.NetcasterSpider;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GeistOfTheMoors.class, RuneclawBear.class, NetcasterSpider.class})
class GeistOfTheMoorsTest extends BaseCardTest {

    @Test
    void cannotBeBlockedByGroundCreature() {
        addCreatureReady(player1, new GeistOfTheMoors());
        addCreatureReady(player2, new RuneclawBear());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(flying)");
    }

    @Test
    void canBeBlockedByFlyingCreature() {
        addCreatureReady(player1, new GeistOfTheMoors());
        Permanent blocker = addCreatureReady(player2, new GeistOfTheMoors());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void canBeBlockedByReachCreature() {
        addCreatureReady(player1, new GeistOfTheMoors());
        Permanent blocker = addCreatureReady(player2, new NetcasterSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
        resolveAllTriggers();
    }

    @Test
    void canBlockGroundCreature() {
        addCreatureReady(player1, new RuneclawBear());
        Permanent blocker = addCreatureReady(player2, new GeistOfTheMoors());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
