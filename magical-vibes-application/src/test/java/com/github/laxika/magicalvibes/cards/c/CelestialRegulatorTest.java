package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CelestialRegulator.class, CivilServant.class})
class CelestialRegulatorTest extends BaseCardTest {

    @Test
    @DisplayName("Taps the target creature and skips its next untap when you control a creature with a counter")
    void tapsAndSkipsNextUntapWithCounter() {
        Permanent counterCreature = harness.addToBattlefieldAndReturn(player1, new CivilServant());
        counterCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CivilServant());

        castRegulator(target);

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getSkipUntapCount()).isEqualTo(1);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Taps the target creature without skipping its next untap when you control no creature with a counter")
    void tapsWithoutSkippingNextUntapWithoutCounter() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CivilServant());

        castRegulator(target);

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getSkipUntapCount()).isZero();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature you control")
    void cannotTargetOwnCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new CivilServant());
        harness.setHand(player1, List.of(new CelestialRegulator()));
        addRegulatorMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void counterGainedBeforeResolutionQualifies() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CivilServant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CivilServant());
        castRegulatorWithTriggerPending(target);
        creature.setCounterCount(CounterType.SHIELD, 1);

        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void counterLostBeforeResolutionDoesNotQualify() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CivilServant());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CivilServant());
        castRegulatorWithTriggerPending(target);
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        harness.passBothPriorities();
        assertThat(target.isTapped()).isTrue();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void regulatorItselfWithCounterQualifiesAndLockExpiresAfterOneUntap() {
        harness.setHand(player2, List.of());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CivilServant());
        target.tap();
        castRegulatorWithTriggerPending(target);
        Permanent regulator = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof CelestialRegulator)
                .findFirst().orElseThrow();
        regulator.setCounterCount(CounterType.SHIELD, 1);

        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        assertThat(target.isTapped()).isTrue();
        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void opponentsCounterDoesNotQualify() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CivilServant());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        castRegulator(target);
        assertThat(target.isTapped()).isTrue();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(target.isTapped()).isFalse();
    }

    private void castRegulator(Permanent target) {
        castRegulatorWithTriggerPending(target);
        harness.passBothPriorities();
    }

    private void castRegulatorWithTriggerPending(Permanent target) {
        harness.setHand(player1, List.of(new CelestialRegulator()));
        addRegulatorMana();
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addRegulatorMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

}
