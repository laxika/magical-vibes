package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.h.HighGround;
import com.github.laxika.magicalvibes.cards.s.SengirBats;
import com.github.laxika.magicalvibes.cards.w.WillowFaerie;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RashkaTheSlayer.class, SengirBats.class, WillowFaerie.class, HighGround.class})
class RashkaTheSlayerTest extends BaseCardTest {

    @Test
    void blocksBlackCreatureBoosts() {
        addCreatureReady(player1, new SengirBats());
        Permanent rashka = addCreatureReady(player2, new RashkaTheSlayer());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(rashka.getPowerModifier()).isEqualTo(1);
        assertThat(rashka.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    void blocksNonBlackCreatureDoesNothing() {
        addCreatureReady(player1, new WillowFaerie());
        Permanent rashka = addCreatureReady(player2, new RashkaTheSlayer());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(rashka.getPowerModifier()).isZero();
        assertThat(rashka.getToughnessModifier()).isZero();
    }

    @Test
    void becomesBlockedDoesNothing() {
        Permanent rashka = addCreatureReady(player1, new RashkaTheSlayer());
        addCreatureReady(player2, new SengirBats());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(rashka.getPowerModifier()).isZero();
        assertThat(rashka.getToughnessModifier()).isZero();
    }

    @Test
    void blocksMultipleBlackCreaturesBoostsOnce() {
        harness.addToBattlefield(player2, new HighGround());
        Permanent rashka = addCreatureReady(player2, new RashkaTheSlayer());
        addCreatureReady(player1, new SengirBats());
        addCreatureReady(player1, new SengirBats());

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        int rashkaIndex = gd.playerBattlefields.get(player2.getId()).indexOf(rashka);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(rashkaIndex, 0),
                new BlockerAssignment(rashkaIndex, 1)));
        resolveAllTriggers();

        assertThat(rashka.getPowerModifier()).isEqualTo(1);
        assertThat(rashka.getToughnessModifier()).isEqualTo(2);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void blocksBlackAndNonBlackCreaturesBoostsOnceRegardlessOfAssignmentOrder(boolean blackFirst) {
        harness.addToBattlefield(player2, new HighGround());
        Permanent rashka = addCreatureReady(player2, new RashkaTheSlayer());
        addCreatureReady(player1, new WillowFaerie());
        addCreatureReady(player1, new SengirBats());

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        int rashkaIndex = gd.playerBattlefields.get(player2.getId()).indexOf(rashka);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(rashkaIndex, blackFirst ? 1 : 0),
                new BlockerAssignment(rashkaIndex, blackFirst ? 0 : 1)));
        resolveAllTriggers();

        assertThat(rashka.getPowerModifier()).isEqualTo(1);
        assertThat(rashka.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    void boostStillResolvesAfterBlockedBlackCreatureLeavesBattlefield() {
        Permanent attacker = addCreatureReady(player1, new SengirBats());
        Permanent rashka = addCreatureReady(player2, new RashkaTheSlayer());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(gd.stack).hasSize(1);
        harness.getPermanentRemovalService().removePermanentToHand(gd, attacker);
        resolveAllTriggers();

        assertThat(rashka.getPowerModifier()).isEqualTo(1);
        assertThat(rashka.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new SengirBats());
        Permanent rashka = addCreatureReady(player2, new RashkaTheSlayer());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(rashka.getPowerModifier()).isZero();
        assertThat(rashka.getToughnessModifier()).isZero();
    }
}
