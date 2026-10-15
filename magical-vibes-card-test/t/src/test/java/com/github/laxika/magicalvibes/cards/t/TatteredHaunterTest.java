package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FalkenrathReaver;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TatteredHaunter.class, FalkenrathReaver.class})
class TatteredHaunterTest extends BaseCardTest {

    @Test
    @DisplayName("Tattered Haunter can block a creature with flying")
    void canBlockCreatureWithFlying() {
        Permanent blocker = addCreatureReady(player2, new TatteredHaunter());
        Permanent attacker = addCreatureReady(player1, new TatteredHaunter());
        declareAttackersAndPrepareBlockers(List.of(0));

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Tattered Haunter cannot block a creature without flying")
    void cannotBlockCreatureWithoutFlying() {
        Permanent blocker = addCreatureReady(player2, new TatteredHaunter());
        Permanent attacker = addCreatureReady(player1, new FalkenrathReaver());
        declareAttackersAndPrepareBlockers(List.of(0));

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with flying");
    }

    @Test
    @DisplayName("A creature without flying or reach cannot block an attacking Tattered Haunter")
    void cannotBeBlockedByCreatureWithoutFlyingOrReach() {
        addCreatureReady(player1, new TatteredHaunter());
        Permanent blocker = addCreatureReady(player2, new FalkenrathReaver());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
        assertThat(blocker.isBlocking()).isFalse();
    }
}
