package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BonescytheSliver;
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

@CardUsed({ShiftingSliver.class, BonescytheSliver.class, GrizzlyBears.class})
class ShiftingSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Shifting Sliver cannot be blocked by a non-Sliver creature")
    void cannotBeBlockedByNonSliver() {
        Permanent shiftingSliver = addCreatureReady(player1, new ShiftingSliver());
        shiftingSliver.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by Sliver creatures");
    }

    @Test
    @DisplayName("Shifting Sliver can be blocked by a Sliver creature")
    void canBeBlockedBySliver() {
        Permanent shiftingSliver = addCreatureReady(player1, new ShiftingSliver());
        shiftingSliver.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BonescytheSliver());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Shifting Sliver restricts opposing Slivers too")
    void restrictsOpposingSlivers() {
        addCreatureReady(player1, new ShiftingSliver());
        Permanent blocker = addCreatureReady(player1, new GrizzlyBears());
        Permanent attackingSliver = addCreatureReady(player2, new BonescytheSliver());
        attackingSliver.setAttacking(true);

        prepareDeclareBlockers(player2);

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
        Permanent attackingCreature = addCreatureReady(player1, new GrizzlyBears());
        attackingCreature.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
