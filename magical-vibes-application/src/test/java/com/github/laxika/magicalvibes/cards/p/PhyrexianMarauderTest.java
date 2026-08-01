package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

class PhyrexianMarauderTest extends BaseCardTest {

    private Permanent findMarauder() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Marauder"))
                .findFirst()
                .orElse(null);
    }

    private void beginDeclareAttackers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    @Test
    @DisplayName("Casting with X=3 enters with 3 +1/+1 counters")
    void entersWith3Counters() {
        harness.setHand(player1, List.of(new PhyrexianMarauder()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        Permanent marauder = findMarauder();
        assertThat(marauder).isNotNull();
        assertThat(marauder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Casting with X=0 enters as 0/0 and dies to state-based actions")
    void entersWith0CountersAndDies() {
        harness.setHand(player1, List.of(new PhyrexianMarauder()));

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Phyrexian Marauder");
    }

    @Test
    @DisplayName("Cannot be declared as a blocker")
    void cannotBlock() {
        Permanent marauder = new Permanent(new PhyrexianMarauder());
        marauder.setSummoningSick(false);
        marauder.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        gd.playerBattlefields.get(player2.getId()).add(marauder);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Attacks when controller pays {1} per +1/+1 counter")
    void attacksWhenPaid() {
        harness.setLife(player2, 20);
        Permanent marauder = new Permanent(new PhyrexianMarauder());
        marauder.setSummoningSick(false);
        marauder.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        gd.playerBattlefields.get(player1.getId()).add(marauder);

        harness.addMana(player1, ManaColor.WHITE, 3);
        beginDeclareAttackers();

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Cannot attack when controller cannot pay the per-counter tax")
    void cannotAttackWithoutPayment() {
        harness.setLife(player2, 20);
        Permanent marauder = new Permanent(new PhyrexianMarauder());
        marauder.setSummoningSick(false);
        marauder.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        gd.playerBattlefields.get(player1.getId()).add(marauder);

        harness.addMana(player1, ManaColor.WHITE, 2);
        beginDeclareAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
