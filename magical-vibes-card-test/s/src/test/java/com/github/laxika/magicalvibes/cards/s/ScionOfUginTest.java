package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AtarkaBeastbreaker;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScionOfUgin.class, AtarkaBeastbreaker.class})
class ScionOfUginTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents non-flying creatures from blocking Scion of Ugin")
    void flyingPreventsGroundBlockers() {
        addCreatureReady(player2, new AtarkaBeastbreaker());
        addCreatureReady(player1, new ScionOfUgin());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("A flying creature can block Scion of Ugin")
    void flyingCreatureCanBlock() {
        addCreatureReady(player1, new ScionOfUgin());
        Permanent blocker = addCreatureReady(player2, new ScionOfUgin());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Scion of Ugin can block a ground creature")
    void canBlockGroundCreature() {
        addCreatureReady(player1, new AtarkaBeastbreaker());
        Permanent blocker = addCreatureReady(player2, new ScionOfUgin());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
