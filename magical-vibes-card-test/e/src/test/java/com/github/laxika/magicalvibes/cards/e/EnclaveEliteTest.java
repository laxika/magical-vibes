package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.i.Island;
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

@CardUsed({EnclaveElite.class, Island.class})
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

        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{1}{U}", "{1}{U}"));
        harness.passBothPriorities();

        Permanent elite = findElite();
        assertThat(elite).isNotNull();
        assertThat(elite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot be blocked when defending player controls an Island")
    void cannotBeBlockedWhenDefenderControlsIsland() {
        harness.addToBattlefield(player2, new Island());

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new EnclaveElite());

        Permanent elite = harness.addToBattlefieldAndReturn(player1, new EnclaveElite());
        elite.setSummoningSick(false);
        elite.setAttacking(true);

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

    @Test
    @DisplayName("Enters with one counter after one multikicker payment")
    void entersWithOneCounterWhenKickedOnce() {
        harness.setHand(player1, List.of(new EnclaveElite()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{1}{U}"));
        harness.passBothPriorities();

        assertThat(findElite().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot multikick without paying the full additional mana cost")
    void cannotMultikickWithInsufficientMana() {
        harness.setHand(player1, List.of(new EnclaveElite()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castCreatureWithRepeatedCosts(
                player1, 0, List.of("{1}{U}")))
                .isInstanceOf(IllegalStateException.class);
        harness.assertNotOnBattlefield(player1, "Enclave Elite");
    }

    @Test
    @DisplayName("Entering without being cast grants no multikicker counters")
    void entersWithoutCountersWhenNotCast() {
        Permanent elite = harness.enterBattlefieldAndReturn(player1, new EnclaveElite());

        assertThat(elite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Can be blocked when only the attacking player controls an Island")
    void canBeBlockedWhenOnlyAttackerControlsIsland() {
        harness.addToBattlefield(player1, new Island());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new EnclaveElite());
        Permanent elite = harness.addToBattlefieldAndReturn(player1, new EnclaveElite());
        elite.setSummoningSick(false);
        elite.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(elite))));

        assertThat(blocker.getBlockingTargetIds()).contains(elite.getId());
    }

    private Permanent findElite() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Enclave Elite"))
                .findFirst()
                .orElse(null);
    }
}
