package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnclaveEliteTest extends BaseCardTest {

    @Test
    @DisplayName("Enters without +1/+1 counters when not multikicked")
    void entersWithoutCountersWhenNotMultikicked() {
        harness.setHand(player1, List.of(new EnclaveElite()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent elite = findElite();
        assertThat(elite).isNotNull();
        assertThat(elite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Enters with one +1/+1 counter per multikicker payment")
    void entersWithCountersForEachMultikickerPayment() {
        harness.setHand(player1, List.of(new EnclaveElite()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, null, null, null, false, null, null, null, null,
                List.of("{1}{U}", "{1}{U}"), false);
        harness.passBothPriorities();

        Permanent elite = findElite();
        assertThat(elite).isNotNull();
        assertThat(elite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot be blocked when defending player controls an Island")
    void cannotBeBlockedWhenDefenderControlsIsland() {
        harness.addToBattlefield(player2, new Island());

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        Permanent elite = new Permanent(new EnclaveElite());
        elite.setSummoningSick(false);
        elite.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(elite);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(elite);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    private Permanent findElite() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Enclave Elite"))
                .findFirst()
                .orElse(null);
    }
}
