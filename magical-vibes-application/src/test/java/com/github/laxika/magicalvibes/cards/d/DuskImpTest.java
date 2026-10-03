package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DuskImp.class, DrossCrocodile.class, GiantSpider.class})
class DuskImpTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a non-flying creature from blocking Dusk Imp")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        addCreatureReady(player1, new DuskImp());
        addCreatureReady(player2, new DrossCrocodile());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(flying)");
    }

    @Test
    @DisplayName("A creature with flying can block Dusk Imp")
    void flyingCreatureCanBlockDuskImp() {
        addCreatureReady(player1, new DuskImp());
        Permanent blocker = addCreatureReady(player2, new DuskImp());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A creature with reach can block Dusk Imp")
    void reachCreatureCanBlockDuskImp() {
        addCreatureReady(player1, new DuskImp());
        Permanent blocker = addCreatureReady(player2, new GiantSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Dusk Imp can block a creature without flying")
    void duskImpCanBlockNonFlyingCreature() {
        addCreatureReady(player1, new DrossCrocodile());
        Permanent blocker = addCreatureReady(player2, new DuskImp());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
