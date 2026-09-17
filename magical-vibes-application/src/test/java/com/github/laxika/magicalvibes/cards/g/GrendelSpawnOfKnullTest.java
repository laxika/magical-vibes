package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrendelSpawnOfKnull.class, GrizzlyBears.class})
class GrendelSpawnOfKnullTest extends BaseCardTest {

    @Test
    @DisplayName("Has flying and deathtouch")
    void hasFlyingAndDeathtouch() {
        Permanent grendel = harness.addToBattlefieldAndReturn(player1, new GrendelSpawnOfKnull());

        assertThat(gqs.hasKeyword(gd, grendel, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, grendel, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Flying prevents a ground creature from blocking")
    void flyingPreventsGroundCreatureFromBlocking() {
        Permanent grendel = addCreatureReady(player1, new GrendelSpawnOfKnull());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
        assertThat(grendel.isAttacking()).isTrue();
    }
}
