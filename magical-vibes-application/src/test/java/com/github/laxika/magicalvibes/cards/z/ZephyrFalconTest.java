package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZephyrFalcon.class, GrizzlyBears.class})
class ZephyrFalconTest extends BaseCardTest {

    @Test
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        Permanent falcon = addCreatureReady(player1, new ZephyrFalcon());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        int falconIndex = gd.playerBattlefields.get(player1.getId()).indexOf(falcon);
        int bearsIndex = gd.playerBattlefields.get(player2.getId()).indexOf(bears);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(bearsIndex, falconIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void vigilanceKeepsItUntappedWhenItAttacks() {
        Permanent falcon = addCreatureReady(player1, new ZephyrFalcon());

        declareAttackers(List.of(0));

        assertThat(falcon.isTapped()).isFalse();
    }
}
