package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElvishVanguard.class, ElvishWarrior.class, GrizzlyBears.class})
class ElvishVanguardTest extends BaseCardTest {

    @Test
    @DisplayName("Another Elf entering under your control puts a +1/+1 counter on it")
    void anotherElfEnteringPutsCounterOnIt() {
        Permanent vanguard = harness.addToBattlefieldAndReturn(player1, new ElvishVanguard());
        harness.setHand(player1, List.of(new ElvishWarrior()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(vanguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A non-Elf creature entering does not trigger it")
    void nonElfEnteringDoesNotTrigger() {
        Permanent vanguard = harness.addToBattlefieldAndReturn(player1, new ElvishVanguard());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(vanguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Elf entering does not trigger it")
    void opponentElfEnteringDoesNotTrigger() {
        Permanent vanguard = harness.addToBattlefieldAndReturn(player1, new ElvishVanguard());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new ElvishWarrior()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(vanguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Elvish Vanguard's own entry does not trigger it")
    void ownEntryDoesNotTrigger() {
        harness.setHand(player1, List.of(new ElvishVanguard()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent vanguard = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(vanguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
