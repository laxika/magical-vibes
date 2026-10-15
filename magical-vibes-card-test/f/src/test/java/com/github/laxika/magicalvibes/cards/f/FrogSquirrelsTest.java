package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AangAirNomad;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FrogSquirrels.class, AangAirNomad.class})
class FrogSquirrelsTest extends BaseCardTest {

    @Test
    void reachAllowsBlockingFlyingCreature() {
        addCreatureReady(player1, new AangAirNomad());
        Permanent frogSquirrels = addCreatureReady(player2, new FrogSquirrels());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(frogSquirrels.isBlocking()).isTrue();
    }

    @Test
    void reachDoesNotPreventBeingBlockedByCreatureWithoutFlying() {
        addCreatureReady(player1, new FrogSquirrels());
        Permanent blocker = addCreatureReady(player2, new FrogSquirrels());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
