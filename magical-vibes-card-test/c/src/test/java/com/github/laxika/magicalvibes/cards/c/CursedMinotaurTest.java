package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.h.HyenaPack;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CursedMinotaur.class, HyenaPack.class})
class CursedMinotaurTest extends BaseCardTest {

    @Test
    void cannotBeBlockedByOneCreature() {
        addCreatureReady(player1, new CursedMinotaur());
        addCreatureReady(player2, new HyenaPack());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    void canBeBlockedByTwoCreatures() {
        addCreatureReady(player1, new CursedMinotaur());
        Permanent firstBlocker = addCreatureReady(player2, new HyenaPack());
        Permanent secondBlocker = addCreatureReady(player2, new HyenaPack());

        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }

    @Test
    void defenderMayChooseNotToBlockDespiteHavingTwoCreatures() {
        addCreatureReady(player1, new CursedMinotaur());
        addCreatureReady(player2, new HyenaPack());
        addCreatureReady(player2, new HyenaPack());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player2, 17);
    }
}
