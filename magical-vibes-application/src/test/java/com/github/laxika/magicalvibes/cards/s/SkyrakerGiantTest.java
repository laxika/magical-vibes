package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Cobblebrute;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkyrakerGiant.class, ScrapskinDrake.class, Cobblebrute.class})
class SkyrakerGiantTest extends BaseCardTest {

    @Test
    void reachAllowsBlockingFlyingCreature() {
        addCreatureReady(player1, new ScrapskinDrake());
        Permanent giant = addCreatureReady(player2, new SkyrakerGiant());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(giant.isBlocking()).isTrue();
        resolveCombat();
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Skyraker Giant");
        harness.assertInGraveyard(player1, "Scrapskin Drake");
    }

    @Test
    void reachAllowsBlockingNonFlyingCreature() {
        addCreatureReady(player1, new Cobblebrute());
        Permanent giant = addCreatureReady(player2, new SkyrakerGiant());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(giant.isBlocking()).isTrue();
    }

    @Test
    void reachDoesNotPreventGroundCreatureFromBlockingGiant() {
        addCreatureReady(player1, new SkyrakerGiant());
        Permanent blocker = addCreatureReady(player2, new Cobblebrute());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
