package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NissaVitalForce;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkysovereignConsulFlagship.class, GrizzlyBears.class, SerraAngel.class, NissaVitalForce.class})
class SkysovereignConsulFlagshipTest extends BaseCardTest {

    @Test
    void entersAndDealsDamageToOpponentCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SkysovereignConsulFlagship()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castArtifact(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void entersAndDealsDamageToOpponentPlaneswalker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NissaVitalForce());
        target.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new SkysovereignConsulFlagship()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castArtifact(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    void attacksAndDealsDamageToOpponentCreature() {
        Permanent flagship = addCreatureReady(player1, new SkysovereignConsulFlagship());
        addCreatureReady(player1, new SerraAngel());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(flagship.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void cannotTargetCreatureItsControllerControls() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SkysovereignConsulFlagship()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent controls");
    }

    @Test
    void attacksAndDealsDamageToOpponentPlaneswalker() {
        addCreatureReady(player1, new SkysovereignConsulFlagship());
        addCreatureReady(player1, new SerraAngel());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NissaVitalForce());
        target.setCounterCount(CounterType.LOYALTY, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    void crewTapsMultipleSummoningSickCreaturesAndExpiresAtEndOfTurn() {
        Permanent flagship = harness.addToBattlefieldAndReturn(player1, new SkysovereignConsulFlagship());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, flagship)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, flagship)).isTrue();
        assertThat(flagship.isTapped()).isFalse();
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        assertThat(gqs.isCreature(gd, flagship)).isFalse();
    }

    @Test
    void cannotCrewWithInsufficientPower() {
        harness.addToBattlefield(player1, new SkysovereignConsulFlagship());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power");
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void cannotTargetUncrewedOpponentVehicle() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SkysovereignConsulFlagship());
        harness.setHand(player1, List.of(new SkysovereignConsulFlagship()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void attackTriggerCannotTargetItsControllersCreature() {
        addCreatureReady(player1, new SkysovereignConsulFlagship());
        Permanent crew = addCreatureReady(player1, new SerraAngel());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, crew.getId());
        harness.passBothPriorities();
        declareAttackers(player1, List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();
        assertThat(ownCreature.getMarkedDamage()).isZero();
        assertThat(opponentCreature.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void canEnterWithoutAnyLegalDamageTarget() {
        harness.setHand(player1, List.of(new SkysovereignConsulFlagship()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Skysovereign, Consul Flagship");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player2, 20);
    }
}
