package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.p.ProwessOfTheFair;
import com.github.laxika.magicalvibes.cards.w.WallOfMulch;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElvishVanguard.class, ElvishWarrior.class, WallOfMulch.class, ProwessOfTheFair.class})
class ElvishVanguardTest extends BaseCardTest {

    @Test
    @DisplayName("Another Elf entering under your control puts a +1/+1 counter on it")
    void anotherElfEnteringPutsCounterOnIt() {
        Permanent vanguard = harness.addToBattlefieldAndReturn(player1, new ElvishVanguard());
        harness.castFromHand(player1, new ElvishWarrior(), "{G}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(vanguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A non-Elf creature entering does not trigger it")
    void nonElfEnteringDoesNotTrigger() {
        Permanent vanguard = harness.addToBattlefieldAndReturn(player1, new ElvishVanguard());
        harness.castFromHand(player1, new WallOfMulch(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(vanguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Elf entering triggers it")
    void opponentElfEnteringTriggers() {
        Permanent vanguard = harness.addToBattlefieldAndReturn(player1, new ElvishVanguard());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new ElvishWarrior(), "{G}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(vanguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Elvish Vanguard's own entry does not trigger it")
    void ownEntryDoesNotTrigger() {
        harness.castFromHand(player1, new ElvishVanguard(), "{1}{G}");
        harness.passBothPriorities();

        Permanent vanguard = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(vanguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A noncreature Elf entering also puts a counter on Elvish Vanguard")
    void noncreatureElfEnteringTriggers() {
        Permanent vanguard = harness.addToBattlefieldAndReturn(player1, new ElvishVanguard());
        harness.castFromHand(player1, new ProwessOfTheFair(), "{1}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(vanguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The counter is added when the entry trigger resolves, not when the Elf is cast or enters")
    void counterWaitsForTriggerResolution() {
        Permanent vanguard = harness.addToBattlefieldAndReturn(player1, new ElvishVanguard());
        harness.castFromHand(player1, new ElvishWarrior(), "{G}{G}");
        assertThat(vanguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();
        assertThat(vanguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(vanguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A second Elvish Vanguard triggers the first but does not trigger itself")
    void secondVanguardTriggersOnlyTheFirst() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ElvishVanguard());
        harness.castFromHand(player1, new ElvishVanguard(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent second = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getId().equals(first.getId()))
                .findFirst().orElseThrow();
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
