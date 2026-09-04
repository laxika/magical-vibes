package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.i.IronTuskElephant;
import com.github.laxika.magicalvibes.cards.s.Solemnity;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MindbenderSpores.class, IronTuskElephant.class})
class MindbenderSporesTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking a creature puts four fungus counters on it")
    void blockingPutsFourFungusCounters() {
        Permanent attacker = blockWithSpores();

        assertThat(attacker.getCounterCount(CounterType.FUNGUS)).isEqualTo(4);
    }

    @Test
    @DisplayName("The blocked creature stays tapped through its untap step and loses one fungus counter at upkeep")
    void blockedCreatureStaysTappedAndLosesOneCounterPerUpkeep() {
        Permanent attacker = blockWithSpores();
        attacker.tap();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(attacker.isTapped()).isTrue();
        assertThat(attacker.getCounterCount(CounterType.FUNGUS)).isEqualTo(3);
    }

    @Test
    @DisplayName("Once the last fungus counter is gone the creature untaps again")
    void untapsAfterLastFungusCounterRemoved() {
        Permanent attacker = blockWithSpores();
        attacker.tap();
        attacker.setCounterCount(CounterType.FUNGUS, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.FUNGUS)).isZero();
        assertThat(attacker.isTapped()).isTrue();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(attacker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("No fungus counters are placed when Mindbender Spores never blocks")
    void noCountersWithoutBlocking() {
        Permanent attacker = addCreatureReady(player1, new IronTuskElephant());
        attacker.setAttacking(true);
        addCreatureReady(player2, new MindbenderSpores());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.FUNGUS)).isZero();
    }

    @Test
    @DisplayName("Each Mindbender Spores trigger removes a fungus counter at upkeep")
    void multipleSporesGrantMultipleUpkeepAbilities() {
        Permanent attacker = addCreatureReady(player1, new IronTuskElephant());
        attacker.setAttacking(true);
        addCreatureReady(player2, new MindbenderSpores());
        addCreatureReady(player2, new MindbenderSpores());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(attacker.getCounterCount(CounterType.FUNGUS)).isEqualTo(8);

        attacker.tap();
        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(attacker.getCounterCount(CounterType.FUNGUS)).isEqualTo(6);
        assertThat(attacker.isTapped()).isTrue();
    }

    @Test
    @CardUsed(Solemnity.class)
    @DisplayName("The granted abilities apply even when fungus counters cannot be placed")
    void grantsAbilitiesWhenCountersCannotBePlaced() {
        Permanent attacker = addCreatureReady(player1, new IronTuskElephant());
        attacker.setAttacking(true);
        addCreatureReady(player2, new MindbenderSpores());
        harness.addToBattlefield(player2, new Solemnity());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.FUNGUS)).isZero();

        gd.playerBattlefields.get(player2.getId()).removeIf(permanent -> permanent.getCard() instanceof Solemnity);
        attacker.setCounterCount(CounterType.FUNGUS, 1);
        attacker.tap();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.FUNGUS)).isZero();
        assertThat(attacker.isTapped()).isTrue();
    }

    /** Player 1's Iron Tusk Elephant attacks and is blocked by player 2's Mindbender Spores. */
    private Permanent blockWithSpores() {
        Permanent attacker = addCreatureReady(player1, new IronTuskElephant());
        attacker.setAttacking(true);
        addCreatureReady(player2, new MindbenderSpores());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        return attacker;
    }
}
