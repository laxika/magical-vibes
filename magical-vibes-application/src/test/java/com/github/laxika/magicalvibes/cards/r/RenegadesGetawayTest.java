package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RenegadesGetawayTest extends BaseCardTest {

    @Test
    void grantsIndestructibleToTargetPermanentAndCreatesServo() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        cast(target);

        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        Permanent servo = findPermanent(player1, "Servo");
        assertThat(servo.getEffectivePower()).isEqualTo(1);
        assertThat(servo.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    void indestructibleWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());

        cast(target);
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void spellFizzesAndDoesNotCreateServoIfTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new RenegadesGetaway()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Servo")).isZero();
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new RenegadesGetaway()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
