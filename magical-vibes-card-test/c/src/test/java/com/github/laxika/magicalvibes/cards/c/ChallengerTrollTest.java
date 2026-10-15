package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.p.ParadiseDruid;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChallengerTroll.class, ParadiseDruid.class})
class ChallengerTrollTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures with power 4 or greater can't be blocked by more than one creature")
    void highPowerCreatureCannotBeBlockedByTwoCreatures() {
        addCreatureReady(player1, new ChallengerTroll());
        Permanent attacker = addCreatureReady(player1, new ParadiseDruid());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        attacker.setAttacking(true);
        Permanent blockerOne = addCreatureReady(player2, new ParadiseDruid());
        Permanent blockerTwo = addCreatureReady(player2, new ParadiseDruid());

        prepareDeclareBlockers();

        List<Permanent> attackers = gd.playerBattlefields.get(player1.getId());
        List<Permanent> blockers = gd.playerBattlefields.get(player2.getId());
        int attackerIndex = attackers.indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockers.indexOf(blockerOne), attackerIndex),
                new BlockerAssignment(blockers.indexOf(blockerTwo), attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked by more than 1 creature");
    }

    @Test
    @DisplayName("Creatures with power less than 4 can still be blocked by two creatures")
    void lowerPowerCreatureCanBeBlockedByTwoCreatures() {
        addCreatureReady(player1, new ChallengerTroll());
        Permanent attacker = addCreatureReady(player1, new ParadiseDruid());
        attacker.setAttacking(true);
        Permanent blockerOne = addCreatureReady(player2, new ParadiseDruid());
        Permanent blockerTwo = addCreatureReady(player2, new ParadiseDruid());

        prepareDeclareBlockers();

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(gd.playerBattlefields.get(player2.getId()).indexOf(blockerOne), attackerIndex),
                new BlockerAssignment(gd.playerBattlefields.get(player2.getId()).indexOf(blockerTwo), attackerIndex)));

        assertThat(blockerOne.isBlocking()).isTrue();
        assertThat(blockerTwo.isBlocking()).isTrue();
    }

    @Test
    void trollItselfCannotBeBlockedByTwoCreatures() {
        Permanent attacker = addCreatureReady(player1, new ChallengerTroll());
        attacker.setAttacking(true);
        addCreatureReady(player2, new ParadiseDruid());
        addCreatureReady(player2, new ParadiseDruid());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked by more than 1 creature");
    }

    @Test
    void trollCanBeBlockedByOneCreature() {
        Permanent attacker = addCreatureReady(player1, new ChallengerTroll());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new ParadiseDruid());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void opposingHighPowerCreatureCanBeBlockedByTwoCreatures() {
        addCreatureReady(player1, new ChallengerTroll());
        Permanent blocker = addCreatureReady(player1, new ParadiseDruid());
        Permanent attacker = addCreatureReady(player2, new ParadiseDruid());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        attacker.setAttacking(true);
        prepareDeclareBlockers(player2);

        gs.declareBlockers(gd, player1, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isBlocking()).isTrue();
    }

    @Test
    void creatureDroppingBelowFourPowerBeforeBlocksCanBeDoubleBlocked() {
        addCreatureReady(player1, new ChallengerTroll());
        Permanent attacker = addCreatureReady(player1, new ParadiseDruid());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        addCreatureReady(player2, new ParadiseDruid());
        addCreatureReady(player2, new ParadiseDruid());
        declareAttackersAndPrepareBlockers(List.of(1));
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 1), new BlockerAssignment(1, 1)));

        assertThat(gd.playerBattlefields.get(player2.getId())).allMatch(Permanent::isBlocking);
    }

    @Test
    void restrictionEndsWhenTrollLeavesBattlefield() {
        Permanent troll = addCreatureReady(player1, new ChallengerTroll());
        Permanent attacker = addCreatureReady(player1, new ParadiseDruid());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        addCreatureReady(player2, new ParadiseDruid());
        addCreatureReady(player2, new ParadiseDruid());
        declareAttackersAndPrepareBlockers(List.of(1));
        gd.playerBattlefields.get(player1.getId()).remove(troll);
        gd.playerGraveyards.get(player1.getId()).add(troll.getCard());

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId())).allMatch(Permanent::isBlocking);
    }
}
