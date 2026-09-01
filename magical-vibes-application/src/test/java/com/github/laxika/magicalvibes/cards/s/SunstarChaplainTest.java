package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SunstarChaplain.class, GrizzlyBears.class, LeoninScimitar.class, Forest.class})
class SunstarChaplainTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on a creature you control at the end step with two tapped creatures")
    void putsCounterAtEndStepWithTwoTappedCreatures() {
        addCreatureReady(player1, new SunstarChaplain());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        target.tap();
        other.tap();

        advanceToEndStep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger with fewer than two tapped creatures")
    void doesNotTriggerWithFewerThanTwoTappedCreatures() {
        addCreatureReady(player1, new SunstarChaplain());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.tap();
        addCreatureReady(player1, new GrizzlyBears());

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingInteractions).isEmpty();
    }

    @Test
    @DisplayName("Removes a +1/+1 counter from a controlled creature to tap an artifact")
    void removesCounterFromControlledCreatureToTapArtifact() {
        addCreatureReady(player1, new SunstarChaplain());
        Permanent counterBearer = addCreatureReady(player1, new GrizzlyBears());
        counterBearer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(counterBearer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can tap a creature with the activated ability")
    void tapsCreature() {
        addCreatureReady(player1, new SunstarChaplain());
        Permanent counterBearer = addCreatureReady(player1, new GrizzlyBears());
        counterBearer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without a +1/+1 counter on a controlled creature")
    void cannotActivateWithoutCounter() {
        addCreatureReady(player1, new SunstarChaplain());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a land with the activated ability")
    void cannotTargetLand() {
        addCreatureReady(player1, new SunstarChaplain());
        Permanent counterBearer = addCreatureReady(player1, new GrizzlyBears());
        counterBearer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
