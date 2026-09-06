package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OminousSeas.class, GrizzlyBears.class})
class OminousSeasTest extends BaseCardTest {

    @Test
    @DisplayName("Drawing a card puts a foreshadow counter on Ominous Seas")
    void drawingAddsForeshadowCounter() {
        Permanent seas = harness.addToBattlefieldAndReturn(player1, new OminousSeas());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        draw();
        resolveTopOfStack();

        assertThat(seas.getCounterCount(CounterType.FORESHADOW)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent drawing a card does not put a foreshadow counter on Ominous Seas")
    void opponentDrawingDoesNotAddCounter() {
        Permanent seas = harness.addToBattlefieldAndReturn(player1, new OminousSeas());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        assertThat(seas.getCounterCount(CounterType.FORESHADOW)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Removing eight foreshadow counters creates an 8/8 blue Kraken")
    void removingCountersCreatesKraken() {
        Permanent seas = harness.addToBattlefieldAndReturn(player1, new OminousSeas());
        seas.setCounterCount(CounterType.FORESHADOW, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent kraken = findPermanent(player1, "Kraken");
        assertThat(kraken.getEffectivePower()).isEqualTo(8);
        assertThat(kraken.getEffectiveToughness()).isEqualTo(8);
        assertThat(kraken.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(kraken.getCard().getSubtypes()).containsExactly(CardSubtype.KRAKEN);
        assertThat(seas.getCounterCount(CounterType.FORESHADOW)).isZero();
    }

    @Test
    @DisplayName("The token ability cannot be activated without eight foreshadow counters")
    void cannotActivateWithoutEightCounters() {
        harness.addToBattlefield(player1, new OminousSeas());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling Ominous Seas draws a card and triggers foreshadow")
    void cyclingDrawsAndAddsCounter() {
        Permanent seas = harness.addToBattlefieldAndReturn(player1, new OminousSeas());
        harness.setHand(player1, List.of(new OminousSeas()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(seas.getCounterCount(CounterType.FORESHADOW)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Ominous Seas");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private void draw() {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
    }

    private void resolveTopOfStack() {
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }
}
