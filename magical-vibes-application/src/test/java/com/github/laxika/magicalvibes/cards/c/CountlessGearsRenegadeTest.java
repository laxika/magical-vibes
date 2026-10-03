package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CountlessGearsRenegade.class, Ornithopter.class})
class CountlessGearsRenegadeTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Servo if a permanent you controlled left the battlefield this turn")
    void createsServoAfterYourPermanentLeaves() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, permanent));

        castRenegade();
        resolveAllTriggers();

        List<Permanent> servos = findPermanents(player1, "Servo");

        assertThat(servos).hasSize(1);
        Permanent servo = servos.getFirst();
        assertThat(servo.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(servo.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(servo.getCard().isToken()).isTrue();
        assertThat(servo.getCard().getColors()).isEmpty();
        assertThat(servo.getCard().getSubtypes()).containsExactly(CardSubtype.SERVO);
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
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, permanent));

        castRenegade();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(candidate -> candidate.getCard().getSubtypes().contains(CardSubtype.SERVO));
    }

    @Test
    @DisplayName("Multiple departures to different zones still create only one Servo")
    void createsOnlyOneServoAfterMultipleDepartures() {
        Permanent dying = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent exiled = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, dying);
            harness.getPermanentRemovalService().removePermanentToExile(gd, exiled);
        });

        castRenegade();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Servo")).isEqualTo(1);
        assertThat(countPermanents(player2, "Servo")).isZero();
    }

    @Test
    @DisplayName("A departure after entry does not retroactively trigger revolt")
    void departureAfterEntryDoesNotTriggerRevolt() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        castRenegade();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Countless Gears Renegade")).hasSize(1);
        assertThat(gd.stack).isEmpty();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, permanent));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Servo")).isZero();
    }

    @Test
    @DisplayName("The revolt trigger still creates a Servo after its source leaves")
    void createsServoAfterRenegadeLeavesWithTriggerOnStack() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, permanent));
        castRenegade();
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        Permanent renegade = findPermanent(player1, "Countless Gears Renegade");
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, renegade));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Countless Gears Renegade")).isZero();
        assertThat(countPermanents(player1, "Servo")).isEqualTo(1);
    }

    private void castRenegade() {
        harness.setHand(player1, List.of(new CountlessGearsRenegade()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
    }
}
