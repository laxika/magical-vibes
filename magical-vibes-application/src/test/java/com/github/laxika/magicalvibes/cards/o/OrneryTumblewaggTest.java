package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.p.PatientNaturalist;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrneryTumblewagg.class, PatientNaturalist.class})
class OrneryTumblewaggTest extends BaseCardTest {

    @Test
    @DisplayName("Beginning of combat puts a +1/+1 counter on a target creature")
    void putsCounterAtBeginningOfCombat() {
        addCreatureReady(player1, new OrneryTumblewagg());
        Permanent target = addCreatureReady(player1, new PatientNaturalist());

        advanceToBeginningOfCombat();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Saddle 2 taps another creature and saddles Ornery Tumblewagg")
    void saddleTapsAnotherCreature() {
        Permanent tumblewagg = addCreatureReady(player1, new OrneryTumblewagg());
        Permanent helper = addCreatureReady(player1, new PatientNaturalist());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(tumblewagg.isSaddled()).isTrue();
        assertThat(helper.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Attacking while saddled doubles +1/+1 counters on a target creature")
    void attacksWhileSaddledDoublesCounters() {
        Permanent tumblewagg = addCreatureReady(player1, new OrneryTumblewagg());
        Permanent target = addCreatureReady(player1, new PatientNaturalist());
        addCreatureReady(player1, new PatientNaturalist());
        tumblewagg.setSaddled(true);
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("The attack trigger does not happen unless Ornery Tumblewagg was saddled when it attacked")
    void doesNotDoubleCountersWhenNotSaddledAtAttack() {
        Permanent tumblewagg = addCreatureReady(player1, new OrneryTumblewagg());
        Permanent target = addCreatureReady(player1, new PatientNaturalist());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        declareAttackers(player1, List.of(0));
        tumblewagg.setSaddled(true);
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void beginningOfCombatCanTargetOpponentCreature() {
        addCreatureReady(player1, new OrneryTumblewagg());
        Permanent target = addCreatureReady(player2, new PatientNaturalist());

        advanceToBeginningOfCombat();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void beginningOfCombatCanTargetItself() {
        Permanent tumblewagg = addCreatureReady(player1, new OrneryTumblewagg());

        advanceToBeginningOfCombat();
        harness.handlePermanentChosen(player1, tumblewagg.getId());
        resolveAllTriggers();

        assertThat(tumblewagg.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerAtOpponentsBeginningOfCombat() {
        Permanent tumblewagg = addCreatureReady(player1, new OrneryTumblewagg());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.passUntil(player2, TurnStep.BEGINNING_OF_COMBAT);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(tumblewagg.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void attackDoublesCurrentCountersOnOpponentCreatureOnly() {
        Permanent tumblewagg = addCreatureReady(player1, new OrneryTumblewagg());
        Permanent target = addCreatureReady(player2, new PatientNaturalist());
        tumblewagg.setSaddled(true);
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        target.setCounterCount(CounterType.STUN, 1);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    void attackDoesNotCreateCountersOnCreatureWithoutCounters() {
        Permanent tumblewagg = addCreatureReady(player1, new OrneryTumblewagg());
        Permanent target = addCreatureReady(player1, new PatientNaturalist());
        tumblewagg.setSaddled(true);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void attackTriggerResolvesAfterSourceLeavesBattlefield() {
        Permanent tumblewagg = addCreatureReady(player1, new OrneryTumblewagg());
        Permanent target = addCreatureReady(player1, new PatientNaturalist());
        tumblewagg.setSaddled(true);
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(tumblewagg);
        gd.playerGraveyards.get(player1.getId()).add(tumblewagg.getCard());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void cannotSaddleUsingItsOwnPower() {
        Permanent tumblewagg = addCreatureReady(player1, new OrneryTumblewagg());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(tumblewagg.isTapped()).isFalse();
        assertThat(tumblewagg.isSaddled()).isFalse();
    }

    @Test
    void summoningSickCreatureCanPaySaddleCost() {
        Permanent tumblewagg = addCreatureReady(player1, new OrneryTumblewagg());
        Permanent helper = addCreatureReady(player1, new PatientNaturalist());
        helper.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(helper.isTapped()).isTrue();
        assertThat(tumblewagg.isSaddled()).isTrue();
        assertThat(tumblewagg.isTapped()).isFalse();
    }

    @Test
    void cannotSaddleDuringCombat() {
        Permanent tumblewagg = addCreatureReady(player1, new OrneryTumblewagg());
        Permanent helper = addCreatureReady(player1, new PatientNaturalist());
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(helper.isTapped()).isFalse();
        assertThat(tumblewagg.isSaddled()).isFalse();
    }

    @Test
    void attackTriggerDoesNothingWhenTargetLeavesBattlefield() {
        Permanent tumblewagg = addCreatureReady(player1, new OrneryTumblewagg());
        Permanent target = addCreatureReady(player1, new PatientNaturalist());
        tumblewagg.setSaddled(true);
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(tumblewagg.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
    private void advanceToBeginningOfCombat() {
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
    }
}
