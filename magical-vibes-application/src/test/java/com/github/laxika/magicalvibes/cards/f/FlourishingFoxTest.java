package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.c.Compulsion;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlourishingFox.class, Censor.class, Compulsion.class, GrizzlyBears.class})
class FlourishingFoxTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling another card puts a +1/+1 counter on Flourishing Fox")
    void cyclingAnotherCardAddsCounter() {
        Permanent fox = harness.addToBattlefieldAndReturn(player1, new FlourishingFox());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(fox.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Censor");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A normal discard does not trigger Flourishing Fox")
    void normalDiscardDoesNotAddCounter() {
        Permanent fox = harness.addToBattlefieldAndReturn(player1, new FlourishingFox());
        harness.addToBattlefield(player1, new Compulsion());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(fox.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Censor");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
