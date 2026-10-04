package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AlloyMyr;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Hovermyr.class, AlloyMyr.class})
class HovermyrTest extends BaseCardTest {

    @Test
    void flyingPreventsGroundCreatureFromBlocking() {
        addCreatureReady(player1, new Hovermyr());
        addCreatureReady(player2, new AlloyMyr());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(flying)");
    }

    @Test
    void flyingCreatureCanBlockHovermyr() {
        addCreatureReady(player1, new Hovermyr());
        Permanent blocker = addCreatureReady(player2, new Hovermyr());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void hovermyrCanBlockGroundCreature() {
        addCreatureReady(player1, new AlloyMyr());
        Permanent blocker = addCreatureReady(player2, new Hovermyr());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void vigilanceKeepsHovermyrUntappedWhenAttacking() {
        Permanent attacker = addCreatureReady(player1, new Hovermyr());
        addCreatureReady(player2, new Hovermyr());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(attacker.isAttacking()).isTrue();
        assertThat(attacker.isTapped()).isFalse();
    }
}
