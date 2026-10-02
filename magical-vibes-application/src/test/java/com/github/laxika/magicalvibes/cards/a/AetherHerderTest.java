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

@CardUsed({AetherHerder.class})
class AetherHerderTest extends BaseCardTest {

    @Test
    void entersWithTwoEnergyCounters() {
        harness.setHand(player1, List.of(new AetherHerder()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void mayPayEnergyOnAttackToCreateServo() {
        addCreatureReady(player1, new AetherHerder());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        Permanent servo = findPermanent(player1, "Servo");
        assertThat(countPermanents(player1, "Servo")).isEqualTo(1);
        assertThat(servo.getCard().isToken()).isTrue();
        assertThat(servo.getCard().getSubtypes()).contains(CardSubtype.SERVO);
        assertThat(servo.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(servo.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(servo.getCard().getColors()).isEmpty();
        assertThat(servo.isTapped()).isFalse();
        assertThat(servo.isAttacking()).isFalse();
        assertThat(gqs.getEffectivePower(gd, servo)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, servo)).isEqualTo(1);
    }

    @Test
    void decliningEnergyPaymentCreatesNoServo() {
        addCreatureReady(player1, new AetherHerder());
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
        addCreatureReady(player1, new AetherHerder());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void oneEnergyCannotBePaidAsPartialPayment() {
        addCreatureReady(player1, new AetherHerder());
        gd.playerEnergyCounters.put(player1.getId(), 1);
        gd.playerEnergyCounters.put(player2.getId(), 4);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(4);
        assertThat(countPermanents(player1, "Servo")).isZero();
        assertThat(countPermanents(player2, "Servo")).isZero();
    }

    @Test
    void paymentUsesEnergyAvailableAtResolutionRatherThanAtAttack() {
        addCreatureReady(player1, new AetherHerder());

        declareAttackers(List.of(0));
        gd.playerEnergyCounters.put(player1.getId(), 5);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
        assertThat(countPermanents(player1, "Servo")).isEqualTo(1);
        assertThat(countPermanents(player2, "Servo")).isZero();
    }

    @Test
    void losingEnergyBeforeResolutionPreventsPayment() {
        addCreatureReady(player1, new AetherHerder());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        declareAttackers(List.of(0));
        gd.playerEnergyCounters.put(player1.getId(), 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(countPermanents(player1, "Servo")).isZero();
    }

    @Test
    void enteringAddsEnergyOnlyToController() {
        gd.playerEnergyCounters.put(player1.getId(), 3);
        gd.playerEnergyCounters.put(player2.getId(), 1);
        harness.setHand(player1, List.of(new AetherHerder()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(5);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(1);
        assertThat(countPermanents(player1, "Servo")).isZero();
    }
}
