package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AlpineGrizzly;
import com.github.laxika.magicalvibes.cards.s.SaguArcher;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MantisRider.class, AlpineGrizzly.class, SaguArcher.class})
class MantisRiderTest extends BaseCardTest {

    @Test
    @DisplayName("Haste lets Mantis Rider attack the turn it enters")
    void hasteAllowsAttackingWithSummoningSickness() {
        addCreatureReady(player2, new MantisRider());

        Permanent rider = harness.addToBattlefieldAndReturn(player1, new MantisRider());

        declareAttackers(List.of(0));

        assertThat(rider.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Vigilance keeps Mantis Rider untapped after attacking")
    void vigilancePreventsTappingWhenAttacking() {
        Permanent rider = addCreatureReady(player1, new MantisRider());

        declareAttackers(List.of(0));

        assertThat(rider.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Flying prevents a non-flying creature from blocking Mantis Rider")
    void flyingPreventsNonFlyingBlocker() {
        Permanent blocker = addCreatureReady(player2, new AlpineGrizzly());

        Permanent rider = addCreatureReady(player1, new MantisRider());
        rider.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int riderIndex = gd.playerBattlefields.get(player1.getId()).indexOf(rider);

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(blockerIndex, riderIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("A flying creature can block Mantis Rider")
    void flyingCreatureCanBlock() {
        Permanent blocker = addCreatureReady(player2, new MantisRider());
        addCreatureReady(player1, new MantisRider());
        declareAttackersAndPrepareBlockers(List.of(0));

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A creature with reach can block Mantis Rider even with summoning sickness")
    void reachCreatureCanBlockWithSummoningSickness() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new SaguArcher());
        addCreatureReady(player1, new MantisRider());
        declareAttackersAndPrepareBlockers(List.of(0));

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
