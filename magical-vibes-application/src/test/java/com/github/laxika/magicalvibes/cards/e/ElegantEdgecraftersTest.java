package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BrazenScourge;
import com.github.laxika.magicalvibes.cards.t.ThrivingRhino;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({ElegantEdgecrafters.class, ThrivingRhino.class, BrazenScourge.class})
class ElegantEdgecraftersTest extends BaseCardTest {

    @Test
    @DisplayName("Fabricate mode puts two +1/+1 counters on Elegant Edgecrafters")
    void fabricateCountersMode() {
        castElegantEdgecrafters(0);

        Permanent edgecrafters = findPermanent(player1, "Elegant Edgecrafters");
        assertThat(edgecrafters.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, edgecrafters)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, edgecrafters)).isEqualTo(6);
    }

    @Test
    @DisplayName("Fabricate mode creates two 1/1 colorless Servo artifact creature tokens")
    void fabricateServoMode() {
        castElegantEdgecrafters(1);

        List<Permanent> servos = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SERVO))
                .toList();

        assertThat(servos).hasSize(2);
        assertThat(servos).allSatisfy(servo -> {
            assertThat(servo.getCard().isToken()).isTrue();
            assertThat(servo.getCard().getColor()).isNull();
            assertThat(servo.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(servo.getCard().hasType(CardType.ARTIFACT)).isTrue();
            assertThat(gqs.getEffectivePower(gd, servo)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, servo)).isEqualTo(1);
        });
        assertThat(findPermanent(player1, "Elegant Edgecrafters")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Elegant Edgecrafters can't be blocked by creatures with power 2 or less")
    void cannotBeBlockedByLowPowerCreature() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new ThrivingRhino());
        Permanent attacker = addAttackingElegantEdgecrafters();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Elegant Edgecrafters can be blocked by creatures with power greater than 2")
    void canBeBlockedByHighPowerCreature() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new BrazenScourge());
        Permanent attacker = addAttackingElegantEdgecrafters();

        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    private void castElegantEdgecrafters(int mode) {
        harness.setHand(player1, List.of(new ElegantEdgecrafters()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castCreature(player1, 0, mode);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Fabricate creates Servos if its source leaves before the trigger resolves")
    void fabricateCreatesServosWhenSourceIsGone() {
        harness.setHand(player1, List.of(new ElegantEdgecrafters()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();

        Permanent edgecrafters = findPermanent(player1, "Elegant Edgecrafters");
        gd.playerBattlefields.get(player1.getId()).remove(edgecrafters);
        gd.playerGraveyards.get(player1.getId()).add(edgecrafters.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SERVO))
                .hasSize(2);
    }

    @Test
    @DisplayName("Blocking restriction uses a creature's power including counters")
    void canBeBlockedByCreatureBoostedAboveTwoPower() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new ThrivingRhino());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent attacker = addAttackingElegantEdgecrafters();

        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addAttackingElegantEdgecrafters() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new ElegantEdgecrafters());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        return attacker;
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
    }
}
