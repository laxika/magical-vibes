package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BladeSliver;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShiftingSliver.class, BladeSliver.class, FugitiveWizard.class})
class ShiftingSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Shifting Sliver cannot be blocked by a non-Sliver creature")
    void cannotBeBlockedByNonSliver() {
        addCreatureReady(player1, new ShiftingSliver());
        addCreatureReady(player2, new FugitiveWizard());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by Sliver creatures");
    }

    @Test
    @DisplayName("Shifting Sliver can be blocked by a Sliver creature")
    void canBeBlockedBySliver() {
        addCreatureReady(player1, new ShiftingSliver());
        Permanent blocker = addCreatureReady(player2, new BladeSliver());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Shifting Sliver restricts opposing Slivers too")
    void restrictsOpposingSlivers() {
        addCreatureReady(player1, new ShiftingSliver());
        Permanent blocker = addCreatureReady(player1, new FugitiveWizard());
        addCreatureReady(player2, new BladeSliver());

        declareAttackersAndPrepareBlockers(player2, List.of(0));

        int blockerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(blocker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(blockerIndex, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by Sliver creatures");
    }

    @Test
    @DisplayName("Shifting Sliver does not restrict non-Sliver creatures")
    void doesNotRestrictNonSlivers() {
        addCreatureReady(player1, new ShiftingSliver());
        addCreatureReady(player1, new FugitiveWizard());
        Permanent blocker = addCreatureReady(player2, new FugitiveWizard());

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
