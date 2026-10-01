package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.i.Imperiosaur;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LucentLiminid.class, Imperiosaur.class})
class LucentLiminidTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a non-flying creature from blocking Lucent Liminid")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        addCreatureReady(player1, new LucentLiminid());
        addCreatureReady(player2, new Imperiosaur());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A creature with flying can block Lucent Liminid")
    void flyingCreatureCanBlock() {
        addCreatureReady(player1, new LucentLiminid());
        Permanent blocker = addCreatureReady(player2, new LucentLiminid());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
