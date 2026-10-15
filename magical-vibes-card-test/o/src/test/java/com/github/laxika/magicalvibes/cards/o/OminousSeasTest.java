package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.p.PhaseDolphin;
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

@CardUsed({OminousSeas.class, PhaseDolphin.class})
class OminousSeasTest extends BaseCardTest {

    @Test
    @DisplayName("Drawing a card puts a foreshadow counter on Ominous Seas")
    void drawingAddsForeshadowCounter() {
        Permanent seas = harness.addToBattlefieldAndReturn(player1, new OminousSeas());
        harness.setLibrary(player1, List.of(new PhaseDolphin()));

        draw();
        harness.passBothPriorities();

        assertThat(seas.getCounterCount(CounterType.FORESHADOW)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent drawing a card does not put a foreshadow counter on Ominous Seas")
    void opponentDrawingDoesNotAddCounter() {
        Permanent seas = harness.addToBattlefieldAndReturn(player1, new OminousSeas());
        harness.setLibrary(player2, List.of(new PhaseDolphin()));

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
        harness.setLibrary(player1, List.of(new PhaseDolphin()));

        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(seas.getCounterCount(CounterType.FORESHADOW)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Ominous Seas");
        harness.assertInHand(player1, "Phase Dolphin");
    }

    @Test
    @DisplayName("Each card drawn creates a separate counter trigger")
    void multipleDrawsAddSeparateCounters() {
        Permanent seas = harness.addToBattlefieldAndReturn(player1, new OminousSeas());
        harness.setLibrary(player1, List.of(new PhaseDolphin(), new PhaseDolphin()));

        draw();
        draw();

        assertThat(seas.getCounterCount(CounterType.FORESHADOW)).isZero();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(seas.getCounterCount(CounterType.FORESHADOW)).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(seas.getCounterCount(CounterType.FORESHADOW)).isEqualTo(2);
    }

    @Test
    @DisplayName("Seven foreshadow counters are insufficient even with other counters")
    void cannotPayWithOtherCounterTypes() {
        Permanent seas = harness.addToBattlefieldAndReturn(player1, new OminousSeas());
        seas.setCounterCount(CounterType.FORESHADOW, 7);
        seas.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(seas.getCounterCount(CounterType.FORESHADOW)).isEqualTo(7);
        assertThat(seas.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Counters are paid immediately and the ability can be activated repeatedly")
    void repeatedActivationsPayCountersBeforeResolution() {
        Permanent seas = harness.addToBattlefieldAndReturn(player1, new OminousSeas());
        seas.setCounterCount(CounterType.FORESHADOW, 17);

        harness.activateAbility(player1, 0, null, null);
        assertThat(seas.getCounterCount(CounterType.FORESHADOW)).isEqualTo(9);
        assertThat(countPermanents(player1, "Kraken")).isZero();
        harness.activateAbility(player1, 0, null, null);
        assertThat(seas.getCounterCount(CounterType.FORESHADOW)).isEqualTo(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Kraken")).isEqualTo(2);
        assertThat(seas.getCounterCount(CounterType.FORESHADOW)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cycling discards as a cost and works without a battlefield copy")
    void cyclingWithoutBattlefieldCopy() {
        harness.setHand(player1, List.of(new OminousSeas()));
        harness.setLibrary(player1, List.of(new PhaseDolphin()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Ominous Seas");
        harness.assertNotInHand(player1, "Phase Dolphin");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Phase Dolphin");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cycling requires two mana and does not discard when payment fails")
    void cyclingRequiresTwoMana() {
        harness.setHand(player1, List.of(new OminousSeas()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Ominous Seas");
        harness.assertNotInGraveyard(player1, "Ominous Seas");
        assertThat(gd.stack).isEmpty();
    }

    private void draw() {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
    }

}
