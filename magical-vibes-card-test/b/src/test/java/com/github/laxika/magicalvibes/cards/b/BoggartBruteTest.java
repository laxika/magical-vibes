package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BoggartBrute.class})
class BoggartBruteTest extends BaseCardTest {

    @Test
    void cannotBeBlockedByOneCreatureEvenWhenAnotherIsAvailable() {
        addCreatureReady(player1, new BoggartBrute());
        addCreatureReady(player2, new BoggartBrute());
        addCreatureReady(player2, new BoggartBrute());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3})
    void canBeBlockedByTwoOrMoreCreatures(int blockerCount) {
        addCreatureReady(player1, new BoggartBrute());
        List<Permanent> blockers = new ArrayList<>();
        List<BlockerAssignment> assignments = new ArrayList<>();
        for (int i = 0; i < blockerCount; i++) {
            blockers.add(addCreatureReady(player2, new BoggartBrute()));
            assignments.add(new BlockerAssignment(i, 0));
        }

        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, assignments));

        assertThat(blockers).allSatisfy(blocker -> assertThat(blocker.isBlocking()).isTrue());
        harness.assertLife(player2, 20);
    }

    @Test
    void defenderMayLeaveMenaceCreatureUnblocked() {
        addCreatureReady(player1, new BoggartBrute());
        addCreatureReady(player2, new BoggartBrute());
        addCreatureReady(player2, new BoggartBrute());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player2, 17);
    }
}
