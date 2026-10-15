package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TajuruSnarecaster.class, SuntailHawk.class, GrizzlyBears.class})
class TajuruSnarecasterTest extends BaseCardTest {

    @Test
    void reachAllowsBlockingFlyingCreature() {
        addCreatureReady(player1, new SuntailHawk());
        Permanent snarecaster = addCreatureReady(player2, new TajuruSnarecaster());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(snarecaster.isBlocking()).isTrue();
    }

    @Test
    void creatureWithoutReachCannotBlockFlyingCreature() {
        addCreatureReady(player1, new SuntailHawk());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void reachAllowsBlockingNonflyingCreature() {
        addCreatureReady(player1, new TajuruSnarecaster());
        Permanent snarecaster = addCreatureReady(player2, new TajuruSnarecaster());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(snarecaster.isBlocking()).isTrue();
    }

    @Test
    void reachDoesNotPreventNonflyingCreatureFromBlockingSnarecaster() {
        addCreatureReady(player1, new TajuruSnarecaster());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(bears.isBlocking()).isTrue();
    }
}
