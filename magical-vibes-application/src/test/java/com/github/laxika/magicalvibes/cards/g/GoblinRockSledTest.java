package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinRockSled.class, Mountain.class, ScatheZombies.class})
class GoblinRockSledTest extends BaseCardTest {

    @Test
    @DisplayName("Goblin Rock Sled can attack when defending player controls a Mountain")
    void canAttackWhenDefenderControlsMountain() {
        harness.addToBattlefield(player2, new Mountain());
        Permanent sled = addCreatureReady(player1, new GoblinRockSled());

        declareAttackers(player1, List.of(0));

        assertThat(sled.isAttackedDuringControllersCurrentTurn()).isTrue();
    }

    @Test
    @DisplayName("Goblin Rock Sled cannot attack when defending player controls no Mountain")
    void cannotAttackWithoutMountain() {
        addCreatureReady(player1, new GoblinRockSled());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotAttackWithOnlyAttackerMountain() {
        harness.addToBattlefield(player1, new Mountain());
        addCreatureReady(player1, new GoblinRockSled());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Goblin Rock Sled untap restriction does not use the stack")
    void attackingDoesNotUseStackForUntapRestriction() {
        harness.addToBattlefield(player2, new Mountain());
        Permanent sled = addCreatureReady(player1, new GoblinRockSled());

        declareAttackers(player1, List.of(0));

        assertThat(sled.isAttackedDuringControllersCurrentTurn()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Goblin Rock Sled stays tapped through the next untap step only")
    void attackingSkipsNextUntap() {
        harness.addToBattlefield(player2, new Mountain());
        Permanent sled = addCreatureReady(player1, new GoblinRockSled());

        declareAttackers(player1, List.of(0));
        advanceTurn();
        advanceTurn();

        assertThat(sled.isTapped()).isTrue();

        advanceTurn();
        advanceTurn();

        assertThat(sled.isTapped()).isFalse();
    }

    @Test
    void untapsIfTappedWithoutAttacking() {
        Permanent sled = addCreatureReady(player1, new GoblinRockSled());
        sled.tap();

        advanceTurn();
        advanceTurn();

        assertThat(sled.isTapped()).isFalse();
    }

    @Test
    void tramplesOverBlockerEvenWhenBothCreaturesDie() {
        harness.setLife(player2, 20);
        Permanent sled = addCreatureReady(player1, new GoblinRockSled());
        Permanent blocker = addCreatureReady(player2, new ScatheZombies());
        harness.addToBattlefield(player2, new Mountain());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 1
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sled);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    private void advanceTurn() {
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

}
