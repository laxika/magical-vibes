package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CountlessGearsRenegadeTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Servo if a permanent you controlled left the battlefield this turn")
    void createsServoAfterYourPermanentLeaves() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, permanent));

        castRenegade();
        resolveAllTriggers();

        List<Permanent> servos = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(candidate -> candidate.getCard().getSubtypes().contains(CardSubtype.SERVO))
                .toList();

        assertThat(servos).hasSize(1);
        Permanent servo = servos.getFirst();
        assertThat(servo.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(servo.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, servo)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, servo)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not create a Servo if no permanent left the battlefield")
    void doesNotCreateServoWithoutRevolt() {
        castRenegade();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SERVO));
    }

    @Test
    @DisplayName("Does not create a Servo when only an opponent's permanent left the battlefield")
    void doesNotCreateServoAfterOpponentsPermanentLeaves() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, permanent));

        castRenegade();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(candidate -> candidate.getCard().getSubtypes().contains(CardSubtype.SERVO));
    }

    private void castRenegade() {
        harness.setHand(player1, List.of(new CountlessGearsRenegade()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
    }
}
