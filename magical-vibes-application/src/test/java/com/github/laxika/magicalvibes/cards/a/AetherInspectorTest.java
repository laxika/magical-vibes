package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AetherInspector.class})
class AetherInspectorTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two energy counters")
    void entersWithTwoEnergyCounters() {
        harness.setHand(player1, List.of(new AetherInspector()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("May pay two energy to create a Servo token when it attacks")
    void paysEnergyToCreateServoTokenWhenAttacking() {
        addCreatureReady(player1, new AetherInspector());
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
        assertThat(servo.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(servo.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(gqs.getEffectivePower(gd, servo)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, servo)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the energy payment creates no Servo token")
    void decliningEnergyPaymentCreatesNoToken() {
        addCreatureReady(player1, new AetherInspector());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void insufficientEnergyCannotCreateToken(int energy) {
        addCreatureReady(player1, new AetherInspector());
        gd.playerEnergyCounters.put(player1.getId(), energy);

        declareAttackers(List.of(0));
        resolveAllTriggers();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
        }

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(energy);
        assertThat(countPermanents(player1, "Servo")).isZero();
    }

    @Test
    void paymentUsesExactlyTwoEnergyAndCreatesOnlyOneToken() {
        addCreatureReady(player1, new AetherInspector());
        gd.playerEnergyCounters.put(player1.getId(), 6);
        gd.playerEnergyCounters.put(player2.getId(), 3);

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(4);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(3);
        assertThat(countPermanents(player1, "Servo")).isEqualTo(1);
        assertThat(countPermanents(player2, "Servo")).isZero();
    }

    @Test
    void multipleInspectorsCannotSpendTheSameEnergyTwice() {
        addCreatureReady(player1, new AetherInspector());
        addCreatureReady(player1, new AetherInspector());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
        }

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(countPermanents(player1, "Servo")).isEqualTo(1);
    }
}
