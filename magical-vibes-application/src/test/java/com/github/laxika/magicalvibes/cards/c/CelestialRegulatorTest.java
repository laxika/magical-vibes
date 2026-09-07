package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CelestialRegulator.class, GrizzlyBears.class})
class CelestialRegulatorTest extends BaseCardTest {

    @Test
    @DisplayName("Taps the target creature and skips its next untap when you control a creature with a counter")
    void tapsAndSkipsNextUntapWithCounter() {
        Permanent counterCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        counterCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castRegulator(target);

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getSkipUntapCount()).isEqualTo(1);

        advanceToNextTurn(player1);

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Taps the target creature without skipping its next untap when you control no creature with a counter")
    void tapsWithoutSkippingNextUntapWithoutCounter() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castRegulator(target);

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getSkipUntapCount()).isZero();

        advanceToNextTurn(player1);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature you control")
    void cannotTargetOwnCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CelestialRegulator()));
        addRegulatorMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castRegulator(Permanent target) {
        harness.setHand(player1, List.of(new CelestialRegulator()));
        addRegulatorMana();
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addRegulatorMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
