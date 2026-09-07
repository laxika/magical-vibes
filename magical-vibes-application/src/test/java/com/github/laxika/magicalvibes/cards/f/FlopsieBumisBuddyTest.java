package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlopsieBumisBuddy.class, GrizzlyBears.class})
class FlopsieBumisBuddyTest extends BaseCardTest {

    @Test
    @DisplayName("When Flopsie enters, it puts a +1/+1 counter on each creature you control")
    void putsCountersOnEachCreatureYouControl() {
        Permanent existing = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new FlopsieBumisBuddy()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent flopsie = findPermanent(player1, "Flopsie, Bumi's Buddy");

        assertThat(existing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(flopsie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Creatures you control with power 4 or greater can't be blocked by more than one creature")
    void highPowerCreatureCannotBeBlockedByTwoCreatures() {
        addCreatureReady(player1, new FlopsieBumisBuddy());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        attacker.setAttacking(true);
        Permanent blockerOne = addCreatureReady(player2, new GrizzlyBears());
        Permanent blockerTwo = addCreatureReady(player2, new GrizzlyBears());

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
    @DisplayName("Creatures you control with power less than 4 can still be blocked by two creatures")
    void lowerPowerCreatureCanBeBlockedByTwoCreatures() {
        addCreatureReady(player1, new FlopsieBumisBuddy());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent blockerOne = addCreatureReady(player2, new GrizzlyBears());
        Permanent blockerTwo = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(gd.playerBattlefields.get(player2.getId()).indexOf(blockerOne), attackerIndex),
                new BlockerAssignment(gd.playerBattlefields.get(player2.getId()).indexOf(blockerTwo), attackerIndex)));

        assertThat(blockerOne.isBlocking()).isTrue();
        assertThat(blockerTwo.isBlocking()).isTrue();
    }
}
