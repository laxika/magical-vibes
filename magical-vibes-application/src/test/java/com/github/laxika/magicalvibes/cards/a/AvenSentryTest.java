package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.m.MammothSpider;
import com.github.laxika.magicalvibes.cards.p.PrimordialWurm;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AvenSentry.class, PrimordialWurm.class, MammothSpider.class})
class AvenSentryTest extends BaseCardTest {

    @Test
    void groundCreatureCannotBlock() {
        addCreatureReady(player1, new AvenSentry());
        addCreatureReady(player2, new PrimordialWurm());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    void flyingCreatureCanBlock() {
        addCreatureReady(player1, new AvenSentry());
        Permanent blocker = addCreatureReady(player2, new AvenSentry());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void reachCreatureCanBlock() {
        addCreatureReady(player1, new AvenSentry());
        Permanent blocker = addCreatureReady(player2, new MammothSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void canBlockGroundCreature() {
        addCreatureReady(player1, new PrimordialWurm());
        Permanent blocker = addCreatureReady(player2, new AvenSentry());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
