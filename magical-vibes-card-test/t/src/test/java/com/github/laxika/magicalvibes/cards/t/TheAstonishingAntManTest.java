package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheAstonishingAntMan.class})
class TheAstonishingAntManTest extends BaseCardTest {

    @Test
    @DisplayName("Drawing a card puts a +1/+1 counter on The Astonishing Ant-Man")
    void drawingCardAddsCounter() {
        Permanent antMan = addCreatureReady(player1, new TheAstonishingAntMan());
        harness.setLibrary(player1, List.of(new TheAstonishingAntMan()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.passBothPriorities();

        assertThat(antMan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removing two +1/+1 counters creates two green Insects")
    void removesChosenCountersAndCreatesTokens() {
        Permanent antMan = addCreatureReady(player1, new TheAstonishingAntMan());
        antMan.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();

        assertThat(antMan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Insect")).hasSize(2);
    }

    @Test
    @DisplayName("The ability allows removing zero counters and creates no tokens")
    void allowsRemovingZeroCounters() {
        Permanent antMan = addCreatureReady(player1, new TheAstonishingAntMan());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();

        assertThat(antMan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanents(player1, "Insect")).isEmpty();
    }

    @Test
    void eachDrawTriggersSeparately() {
        Permanent antMan = addCreatureReady(player1, new TheAstonishingAntMan());
        harness.setLibrary(player1, List.of(new TheAstonishingAntMan(), new TheAstonishingAntMan()));

        harness.inMutationScope(() -> {
            harness.getDrawService().resolveDrawCard(gd, player1.getId());
            harness.getDrawService().resolveDrawCard(gd, player1.getId());
        });

        assertThat(antMan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(antMan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(antMan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void opponentDrawingDoesNotAddCounters() {
        Permanent antMan = addCreatureReady(player1, new TheAstonishingAntMan());
        harness.setLibrary(player2, List.of(new TheAstonishingAntMan()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        assertThat(gd.stack).isEmpty();
        assertThat(antMan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void countersAndTapArePaidBeforeTokensAreCreated() {
        Permanent antMan = addCreatureReady(player1, new TheAstonishingAntMan());
        antMan.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, 2, null);

        assertThat(antMan.isTapped()).isTrue();
        assertThat(antMan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanents(player1, "Insect")).isEmpty();
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Insect")).hasSize(2);
        assertThat(findPermanents(player2, "Insect")).isEmpty();
    }

    @Test
    void cannotRemoveMoreCountersThanAvailable() {
        Permanent antMan = addCreatureReady(player1, new TheAstonishingAntMan());
        antMan.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(antMan.isTapped()).isFalse();
        assertThat(antMan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
