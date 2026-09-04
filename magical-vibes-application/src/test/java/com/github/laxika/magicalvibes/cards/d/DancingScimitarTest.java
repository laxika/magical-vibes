package com.github.laxika.magicalvibes.cards.d;

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

@CardUsed({DancingScimitar.class, GrizzlyBears.class})
class DancingScimitarTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a ground creature from blocking Dancing Scimitar")
    void flyingPreventsGroundCreatureFromBlocking() {
        Permanent scimitar = addCreatureReady(player1, new DancingScimitar());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(scimitar.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Flying creatures can block Dancing Scimitar")
    void flyingCreatureCanBlock() {
        addCreatureReady(player1, new DancingScimitar());
        Permanent blocker = addCreatureReady(player2, new DancingScimitar());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
