package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AkroanJailer;
import com.github.laxika.magicalvibes.cards.a.AspiringAeronaut;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HitchclawRecluse.class, AkroanJailer.class, AspiringAeronaut.class})
class HitchclawRecluseTest extends BaseCardTest {

    @Test
    void canBlockFlyingCreature() {
        Permanent spider = addCreatureReady(player2, new HitchclawRecluse());
        addCreatureReady(player1, new AspiringAeronaut());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(spider.isBlocking()).isTrue();
    }

    @Test
    void canBlockNonFlyingCreature() {
        Permanent spider = addCreatureReady(player2, new HitchclawRecluse());
        addCreatureReady(player1, new AkroanJailer());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(spider.isBlocking()).isTrue();
    }

    @Test
    void reachDoesNotGrantFlyingEvasion() {
        addCreatureReady(player1, new HitchclawRecluse());
        Permanent jailer = addCreatureReady(player2, new AkroanJailer());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(jailer.isBlocking()).isTrue();
    }

    @Test
    void tappedSpiderCannotBlockFlyingCreature() {
        Permanent spider = addCreatureReady(player2, new HitchclawRecluse());
        spider.tap();
        addCreatureReady(player1, new AspiringAeronaut());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't block");
        assertThat(spider.isBlocking()).isFalse();
    }
}
