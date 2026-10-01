package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.s.Squire;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlyingMen.class, Squire.class})
class FlyingMenTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a non-flying creature from blocking Flying Men")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        addCreatureReady(player1, new FlyingMen());
        addCreatureReady(player2, new Squire());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block Flying Men (flying)");
    }

    @Test
    @DisplayName("Flying still allows a creature with flying to block Flying Men")
    void flyingAllowsFlyingCreatureToBlock() {
        addCreatureReady(player1, new FlyingMen());
        Permanent blocker = addCreatureReady(player2, new FlyingMen());

        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
