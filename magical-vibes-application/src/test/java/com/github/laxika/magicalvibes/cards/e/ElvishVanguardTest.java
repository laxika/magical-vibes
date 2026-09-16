package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.w.WallOfMulch;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElvishVanguard.class, ElvishWarrior.class, WallOfMulch.class})
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
}
