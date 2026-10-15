package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AetherPoisoner.class})
class AetherPoisonerTest extends BaseCardTest {

    @Test
    void entersWithTwoEnergyCounters() {
        harness.setHand(player1, List.of(new AetherPoisoner()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void mayPayEnergyOnAttackToCreateServo() {
        addCreatureReady(player1, new AetherPoisoner());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        Permanent servo = findPermanent(player1, "Servo");
        assertThat(servo.getCard().isToken()).isTrue();
        assertThat(servo.getCard().getSubtypes()).contains(CardSubtype.SERVO);
        assertThat(servo.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(gqs.getEffectivePower(gd, servo)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, servo)).isEqualTo(1);
    }

    @Test
    void decliningEnergyPaymentCreatesNoServo() {
        addCreatureReady(player1, new AetherPoisoner());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void cannotPayEnergyWithoutTwoEnergyCounters() {
        addCreatureReady(player1, new AetherPoisoner());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void cannotMakePartialEnergyPayment() {
        addCreatureReady(player1, new AetherPoisoner());
        gd.playerEnergyCounters.put(player1.getId(), 1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(countPermanents(player1, "Servo")).isZero();
    }

    @Test
    void paysExactlyTwoEnergyAndCreatesOnlyOneServo() {
        addCreatureReady(player1, new AetherPoisoner());
        gd.playerEnergyCounters.put(player1.getId(), 6);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(4);
        assertThat(countPermanents(player1, "Servo")).isEqualTo(1);
        Permanent servo = findPermanent(player1, "Servo");
        assertThat(servo.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(servo.getCard().getColors()).isEmpty();
        assertThat(servo.isTapped()).isFalse();
        assertThat(servo.isAttacking()).isFalse();
    }

    @Test
    void opponentControlledPoisonerUsesOpponentsEnergyAndCreatesTheirServo() {
        addCreatureReady(player2, new AetherPoisoner());
        gd.playerEnergyCounters.put(player1.getId(), 5);
        gd.playerEnergyCounters.put(player2.getId(), 2);

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(5);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isZero();
        assertThat(countPermanents(player1, "Servo")).isZero();
        assertThat(countPermanents(player2, "Servo")).isEqualTo(1);
    }
}
