package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.s.SunbathingRootwalla;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElfhameWurm.class, SunbathingRootwalla.class})
class ElfhameWurmTest extends BaseCardTest {

    @Test
    void vigilanceKeepsItUntappedWhenItAttacks() {
        Permanent wurm = addCreatureReady(player1, new ElfhameWurm());

        declareAttackers(List.of(0));

        assertThat(wurm.isTapped()).isFalse();
    }

    @Test
    void trampleDealsExcessCombatDamageToDefendingPlayer() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new ElfhameWurm());
        Permanent blocker = addCreatureReady(player2, new SunbathingRootwalla());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 3));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    void trampleDoesNotDealPlayerDamageWhenBlockersAbsorbAllDamage() {
        harness.setLife(player2, 20);
        Permanent wurm = addCreatureReady(player1, new ElfhameWurm());
        Permanent first = addCreatureReady(player2, new SunbathingRootwalla());
        Permanent second = addCreatureReady(player2, new SunbathingRootwalla());
        Permanent third = addCreatureReady(player2, new SunbathingRootwalla());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)));
        harness.passBothPriorities();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                first.getId(), 2,
                second.getId(), 2,
                third.getId(), 1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(wurm);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(third).doesNotContain(first, second);
    }
}
