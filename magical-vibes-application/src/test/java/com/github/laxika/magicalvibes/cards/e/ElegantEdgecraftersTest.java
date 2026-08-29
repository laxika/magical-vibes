package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
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
            assertThat(servo.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(servo.getCard().hasType(CardType.ARTIFACT)).isTrue();
            assertThat(gqs.getEffectivePower(gd, servo)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, servo)).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Elegant Edgecrafters can't be blocked by creatures with power 2 or less")
    void cannotBeBlockedByLowPowerCreature() {
        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);
        Permanent attacker = addAttackingElegantEdgecrafters();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Elegant Edgecrafters can be blocked by creatures with power greater than 2")
    void canBeBlockedByHighPowerCreature() {
        Permanent blocker = new Permanent(new HillGiant());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);
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

    private Permanent addAttackingElegantEdgecrafters() {
        Permanent attacker = new Permanent(new ElegantEdgecrafters());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);
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
