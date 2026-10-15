package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.AerieBowmasters;
import com.github.laxika.magicalvibes.cards.d.DromokaWarrior;
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

@CardUsed({UpdraftElemental.class, GrizzlyBears.class, AerieBowmasters.class, DromokaWarrior.class})
class UpdraftElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a non-flying creature from blocking Updraft Elemental")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        addCreatureReady(player1, new UpdraftElemental());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canBeBlockedByFlyingCreature() {
        addCreatureReady(player1, new UpdraftElemental());
        Permanent blocker = addCreatureReady(player2, new UpdraftElemental());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void canBeBlockedByCreatureWithReach() {
        addCreatureReady(player1, new UpdraftElemental());
        Permanent blocker = addCreatureReady(player2, new AerieBowmasters());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void canBlockGroundCreature() {
        addCreatureReady(player1, new DromokaWarrior());
        Permanent blocker = addCreatureReady(player2, new UpdraftElemental());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
