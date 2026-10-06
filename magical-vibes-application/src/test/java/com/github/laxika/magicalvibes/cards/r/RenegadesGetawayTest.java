package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FatalPush;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RenegadesGetaway.class, Forest.class, Ornithopter.class, FatalPush.class})
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

    @Test
    void protectsArtifactCreatureFromDestructionWithoutProtectingServo() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        cast(target);

        harness.setHand(player2, List.of(new FatalPush(), new FatalPush()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, target.getId());

        harness.assertOnBattlefield(player1, "Ornithopter");
        Permanent servo = findPermanent(player1, "Servo");
        assertThat(servo.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(countPermanents(player1, "Servo")).isEqualTo(1);
        assertThat(countPermanents(player2, "Servo")).isZero();
        harness.castAndResolveInstant(player2, 0, servo.getId());
        assertThat(countPermanents(player1, "Servo")).isZero();
        harness.assertOnBattlefield(player1, "Ornithopter");
    }

    @Test
    void createsColorlessArtifactCreatureServoTokenUnderSpellController() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        cast(target);

        assertThat(countPermanents(player1, "Servo")).isEqualTo(1);
        assertThat(countPermanents(player2, "Servo")).isZero();
        Permanent servo = findPermanent(player1, "Servo");
        assertThat(servo.getCard().isToken()).isTrue();
        assertThat(servo.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(servo.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(servo.getCard().getColors()).isEmpty();
        assertThat(servo.getCard().getSubtypes()).containsExactly(CardSubtype.SERVO);
        assertThat(servo.getEffectivePower()).isEqualTo(1);
        assertThat(servo.getEffectiveToughness()).isEqualTo(1);
        assertThat(servo.isTapped()).isFalse();
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new RenegadesGetaway()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
