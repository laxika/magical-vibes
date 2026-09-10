package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheAstonishingAntMan.class, GrizzlyBears.class})
class TheAstonishingAntManTest extends BaseCardTest {

    @Test
    @DisplayName("Drawing a card puts a +1/+1 counter on The Astonishing Ant-Man")
    void drawingCardAddsCounter() {
        Permanent antMan = addCreatureReady(player1, new TheAstonishingAntMan());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

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
}
