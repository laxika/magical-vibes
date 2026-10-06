package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RelicSloth.class})
class RelicSlothTest extends BaseCardTest {

    @Test
    @DisplayName("Vigilance keeps Relic Sloth untapped after attacking")
    void vigilanceDoesNotTapWhenAttacking() {
        Permanent sloth = addCreatureReady(player1, new RelicSloth());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        assertThat(sloth.isAttacking()).isTrue();

        assertThat(sloth.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Menace prevents Relic Sloth from being blocked by one creature")
    void menaceRequiresTwoBlockers() {
        Permanent sloth = addCreatureReady(player1, new RelicSloth());
        sloth.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new RelicSloth());

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(sloth);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3})
    @DisplayName("Menace allows two or more creatures to block Relic Sloth")
    void menaceAllowsTwoOrMoreBlockers(int blockerCount) {
        addCreatureReady(player1, new RelicSloth());
        List<Permanent> blockers = new ArrayList<>();
        List<BlockerAssignment> assignments = new ArrayList<>();
        for (int i = 0; i < blockerCount; i++) {
            blockers.add(addCreatureReady(player2, new RelicSloth()));
            assignments.add(new BlockerAssignment(i, 0));
        }
        declareAttackersAndPrepareBlockers(List.of(0));

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, assignments));

        assertThat(blockers).allSatisfy(blocker -> assertThat(blocker.isBlocking()).isTrue());
    }

    @Test
    @DisplayName("Menace permits no blocks and Relic Sloth deals combat damage")
    void menaceAllowsNoBlockers() {
        Permanent sloth = addCreatureReady(player1, new RelicSloth());
        addCreatureReady(player2, new RelicSloth());
        harness.setLife(player2, 20);
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player2, 16);
        assertThat(sloth.isTapped()).isFalse();
    }
}
