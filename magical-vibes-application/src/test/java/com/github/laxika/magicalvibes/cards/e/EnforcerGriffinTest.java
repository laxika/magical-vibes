package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.p.PrimordialWurm;
import com.github.laxika.magicalvibes.cards.s.Snarespinner;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EnforcerGriffin.class, PrimordialWurm.class, Snarespinner.class})
class EnforcerGriffinTest extends BaseCardTest {

    @Test
    void cannotBeBlockedByGroundCreature() {
        addCreatureReady(player1, new EnforcerGriffin());
        addCreatureReady(player2, new PrimordialWurm());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    void canBeBlockedByFlyingCreature() {
        addCreatureReady(player1, new EnforcerGriffin());
        Permanent blocker = addCreatureReady(player2, new EnforcerGriffin());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void canBeBlockedByReachCreature() {
        addCreatureReady(player1, new EnforcerGriffin());
        Permanent blocker = addCreatureReady(player2, new Snarespinner());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
        resolveAllTriggers();
    }

    @Test
    void canBlockGroundCreature() {
        addCreatureReady(player1, new PrimordialWurm());
        Permanent blocker = addCreatureReady(player2, new EnforcerGriffin());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
