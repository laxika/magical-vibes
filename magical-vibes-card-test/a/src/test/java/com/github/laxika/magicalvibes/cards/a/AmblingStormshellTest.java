package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FuriousForebear;
import com.github.laxika.magicalvibes.cards.r.RebelliousStrike;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AmblingStormshell.class, FuriousForebear.class, RebelliousStrike.class})
class AmblingStormshellTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking puts three stun counters on Ambling Stormshell and draws three cards")
    void attackPutsStunCountersAndDrawsCards() {
        Permanent shell = addCreatureReady(player1, new AmblingStormshell());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new FuriousForebear(), new FuriousForebear(), new FuriousForebear()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(shell.getCounterCount(CounterType.STUN)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Casting a Turtle spell untaps Ambling Stormshell")
    void turtleSpellUntapsShell() {
        Permanent shell = addCreatureReady(player1, new AmblingStormshell());
        shell.tap();
        harness.castFromHand(player1, new AmblingStormshell(), "{3}{U}{U}");
        harness.passBothPriorities();

        assertThat(shell.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Casting a non-Turtle spell does not untap Ambling Stormshell")
    void nonTurtleSpellDoesNotUntapShell() {
        Permanent shell = addCreatureReady(player1, new AmblingStormshell());
        shell.tap();
        harness.castFromHand(player1, new FuriousForebear(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(shell.isTapped()).isTrue();
    }

    @Test
    void turtleCastRemovesOnlyOneStunCounterBeforeSpellResolves() {
        Permanent shell = addCreatureReady(player1, new AmblingStormshell());
        shell.tap();
        shell.setCounterCount(CounterType.STUN, 3);

        harness.castFromHand(player1, new AmblingStormshell(), "{3}{U}{U}");
        harness.passBothPriorities();

        assertThat(shell.isTapped()).isTrue();
        assertThat(shell.getCounterCount(CounterType.STUN)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void removingLastStunCounterDoesNotUntapUntilAnotherUntapAttempt() {
        Permanent shell = addCreatureReady(player1, new AmblingStormshell());
        shell.tap();
        shell.setCounterCount(CounterType.STUN, 1);

        harness.castFromHand(player1, new AmblingStormshell(), "{3}{U}{U}");
        harness.passBothPriorities();

        assertThat(shell.isTapped()).isTrue();
        assertThat(shell.getCounterCount(CounterType.STUN)).isZero();

        harness.passBothPriorities();
        harness.castFromHand(player1, new AmblingStormshell(), "{3}{U}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(shell.isTapped()).isFalse();
    }

    @Test
    void untappedShellKeepsStunCountersWhenTurtleIsCast() {
        Permanent shell = addCreatureReady(player1, new AmblingStormshell());
        shell.setCounterCount(CounterType.STUN, 3);

        harness.castFromHand(player1, new AmblingStormshell(), "{3}{U}{U}");
        harness.passBothPriorities();

        assertThat(shell.isTapped()).isFalse();
        assertThat(shell.getCounterCount(CounterType.STUN)).isEqualTo(3);
    }

    @Test
    void opponentsTurtleSpellDoesNotUntapShell() {
        Permanent shell = addCreatureReady(player1, new AmblingStormshell());
        shell.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new AmblingStormshell(), "{3}{U}{U}");
        harness.passBothPriorities();

        assertThat(shell.isTapped()).isTrue();
    }

    @Test
    void turtleEnteringWithoutBeingCastDoesNotUntapShell() {
        Permanent shell = addCreatureReady(player1, new AmblingStormshell());
        shell.tap();

        harness.enterBattlefieldAndReturn(player1, new AmblingStormshell());
        harness.passBothPriorities();

        assertThat(shell.isTapped()).isTrue();
    }

    @Test
    void stunCountersReplaceThreeUntapStepsBeforeShellUntaps() {
        Permanent shell = addCreatureReady(player1, new AmblingStormshell());
        shell.tap();
        shell.setCounterCount(CounterType.STUN, 3);

        for (int remaining = 2; remaining >= 0; remaining--) {
            harness.performUntapStep(player1);
            assertThat(shell.isTapped()).isTrue();
            assertThat(shell.getCounterCount(CounterType.STUN)).isEqualTo(remaining);
        }
        harness.performUntapStep(player1);

        assertThat(shell.isTapped()).isFalse();
    }

    @Test
    void wardCountersOpponentsSpellWhenTwoManaCannotBePaid() {
        Permanent shell = addCreatureReady(player1, new AmblingStormshell());
        harness.setHand(player2, List.of(new RebelliousStrike()));
        harness.setLibrary(player2, List.of(new AmblingStormshell()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, shell.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Rebellious Strike");
        assertThat(gqs.getEffectivePower(gd, shell)).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void payingTwoManaForWardAllowsOpponentsSpellToResolve() {
        Permanent shell = addCreatureReady(player1, new AmblingStormshell());
        harness.setHand(player2, List.of(new RebelliousStrike()));
        harness.setLibrary(player2, List.of(new AmblingStormshell()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castInstant(player2, 0, shell.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, shell)).isEqualTo(8);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }
}
