package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PhyrexianJuggernaut.class, GrizzlyBears.class, SerraAngel.class})
class PhyrexianJuggernautTest extends BaseCardTest {

    @Test
    @DisplayName("Phyrexian Juggernaut resolves onto the battlefield")
    void resolvesOntoBattlefield() {
        harness.setHand(player1, List.of(new PhyrexianJuggernaut()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Phyrexian Juggernaut");
    }

    @Test
    @DisplayName("Declaring no attackers when Phyrexian Juggernaut can attack throws exception")
    void mustAttackWhenAble() {
        Permanent juggernaut = harness.addToBattlefieldAndReturn(player1, new PhyrexianJuggernaut());
        juggernaut.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Phyrexian Juggernaut does not need to attack with summoning sickness")
    void doesNotAttackWithSummoningSickness() {
        Permanent juggernaut = harness.addToBattlefieldAndReturn(player1, new PhyrexianJuggernaut());
        // summoning sick by default

        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        // Only Grizzly Bears can attack, Juggernaut has summoning sickness
        gs.declareAttackers(gd, player1, List.of(1));

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("Unblocked Phyrexian Juggernaut deals 5 poison counters instead of life loss")
    void dealsPoisonCountersWhenUnblocked() {
        harness.setLife(player2, 20);

        Permanent juggernaut = harness.addToBattlefieldAndReturn(player1, new PhyrexianJuggernaut());
        juggernaut.setSummoningSick(false);
        juggernaut.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Life should remain unchanged
        harness.assertLife(player2, 20);
        // Poison counters should equal power (5)
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(5);
    }

    @Test
    @DisplayName("Blocked Phyrexian Juggernaut deals -1/-1 counters to blocker")
    void dealsMinusCountersToBlocker() {
        // Serra Angel is 4/4 with flying + vigilance — can block
        Permanent blockerPerm = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        blockerPerm.setSummoningSick(false);

        Permanent juggernaut = harness.addToBattlefieldAndReturn(player1, new PhyrexianJuggernaut());
        juggernaut.setSummoningSick(false);
        juggernaut.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(juggernaut);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));
        harness.passBothPriorities();

        // Serra Angel (4/4) deals 4 damage to Juggernaut (5/5) — Juggernaut survives
        harness.assertOnBattlefield(player1, "Phyrexian Juggernaut");

        // Serra Angel should have 5 -1/-1 counters (from 5 infect damage), making it -1/-1 — dies
        harness.assertNotOnBattlefield(player2, "Serra Angel");
        harness.assertInGraveyard(player2, "Serra Angel");

        // No poison counters — damage went to a creature
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("A tapped Phyrexian Juggernaut is not required to attack")
    void tappedJuggernautDoesNotHaveToAttack() {
        Permanent juggernaut = harness.addToBattlefieldAndReturn(player1, new PhyrexianJuggernaut());
        juggernaut.setSummoningSick(false);
        juggernaut.setTapped(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of());

        assertThat(juggernaut.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("An untapped Phyrexian Juggernaut must attack again in a second combat")
    void mustAttackInEachCombat() {
        Permanent juggernaut = harness.addToBattlefieldAndReturn(player1, new PhyrexianJuggernaut());
        juggernaut.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));
        assertThat(juggernaut.isAttacking()).isTrue();

        juggernaut.setAttacking(false);
        juggernaut.setTapped(false);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Phyrexian Juggernaut deals infect damage while blocking")
    void infectAppliesWhileBlocking() {
        Permanent juggernaut = harness.addToBattlefieldAndReturn(player2, new PhyrexianJuggernaut());
        Permanent groundAttacker = harness.addToBattlefieldAndReturn(player1, new PhyrexianJuggernaut());
        groundAttacker.setSummoningSick(false);
        groundAttacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(groundAttacker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(5);
        assertThat(juggernaut.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(5);
        harness.assertInGraveyard(player1, "Phyrexian Juggernaut");
        harness.assertInGraveyard(player2, "Phyrexian Juggernaut");
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }
}
