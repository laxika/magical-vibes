package com.github.laxika.magicalvibes.cards.f;

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

@CardUsed({FamiliarGround.class, GrizzlyBears.class})
class FamiliarGroundTest extends BaseCardTest {

    @Test
    @DisplayName("A creature you control can't be blocked by two creatures while Familiar Ground is out")
    void creatureCannotBeBlockedByTwoCreatures() {
        harness.addToBattlefield(player1, new FamiliarGround());

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        Permanent blockerOne = addCreatureReady(player2, new GrizzlyBears());
        Permanent blockerTwo = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        int blockerOneIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blockerOne);
        int blockerTwoIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blockerTwo);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerOneIndex, attackerIndex),
                new BlockerAssignment(blockerTwoIndex, attackerIndex)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked by more than 1 creature");
    }

    @Test
    @DisplayName("A single blocker is legal while Familiar Ground is out")
    void canBeBlockedByOneCreature() {
        harness.addToBattlefield(player1, new FamiliarGround());

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("An opponent's creature can be blocked by two creatures")
    void opponentCreatureCanBeBlockedByTwoCreatures() {
        harness.addToBattlefield(player1, new FamiliarGround());

        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent blockerOne = addCreatureReady(player1, new GrizzlyBears());
        Permanent blockerTwo = addCreatureReady(player1, new GrizzlyBears());

        prepareDeclareBlockers(player2);

        int attackerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(attacker);
        int blockerOneIndex = gd.playerBattlefields.get(player1.getId()).indexOf(blockerOne);
        int blockerTwoIndex = gd.playerBattlefields.get(player1.getId()).indexOf(blockerTwo);

        gs.declareBlockers(gd, player1, List.of(
                new BlockerAssignment(blockerOneIndex, attackerIndex),
                new BlockerAssignment(blockerTwoIndex, attackerIndex)));

        assertThat(blockerOne.isBlocking()).isTrue();
        assertThat(blockerTwo.isBlocking()).isTrue();
    }
}
