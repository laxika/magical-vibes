package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({GammaGrotesque.class, Shock.class})
class GammaGrotesqueTest extends BaseCardTest {

    @Test
    @DisplayName("Power-up puts three counters on each Gamma and draws for counter-bearing creatures")
    void powerUpAddsCountersAndDrawsForCounterBearingCreatures() {
        Card firstDraw = new Shock();
        Card secondDraw = new Shock();
        Card thirdDraw = new Shock();
        Permanent firstGamma = harness.enterBattlefieldAndReturn(player1, new GammaGrotesque());
        Permanent secondGamma = harness.enterBattlefieldAndReturn(player1, new GammaGrotesque());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, thirdDraw));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(firstGamma.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(secondGamma.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw, thirdDraw);
    }

    @Test
    @DisplayName("Power-up costs its full activation cost after the entry turn")
    void powerUpIsNotDiscountedAfterEntryTurn() {
        Permanent gamma = addCreatureReady(player1, new GammaGrotesque());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gamma.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Power-up can be activated only once")
    void powerUpCanBeActivatedOnlyOnce() {
        addCreatureReady(player1, new GammaGrotesque());
        harness.setLibrary(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    @DisplayName("Power-up costs two generic and one green mana on the entry turn")
    void powerUpIsDiscountedOnEntryTurn() {
        Permanent gamma = harness.enterBattlefieldAndReturn(player1, new GammaGrotesque());
        Card draw = new Shock();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(draw));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gamma.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
    }

    @Test
    @DisplayName("Entry-turn mana is insufficient after the entry turn")
    void discountedManaCannotPayFullCost() {
        Permanent gamma = addCreatureReady(player1, new GammaGrotesque());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gamma.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Draw counts creatures with any counters once and excludes opposing and counterless creatures")
    void drawCountsOnlyControlledCounterBearingCreatures() {
        Permanent gamma = addCreatureReady(player1, new GammaGrotesque());
        Permanent other = addCreatureReady(player1, new GammaGrotesque());
        other.getCounters().put(CounterType.CHARGE, 4);
        other.getCounters().put(CounterType.VIGILANCE, 1);
        addCreatureReady(player1, new GammaGrotesque());
        Permanent opponent = addCreatureReady(player2, new GammaGrotesque());
        opponent.getCounters().put(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Card firstDraw = new Shock();
        Card secondDraw = new Shock();
        Card remaining = new Shock();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, remaining));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gamma.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
    }

    @Test
    @DisplayName("Power-up cannot be activated again while its first activation is on the stack")
    void powerUpLimitAppliesBeforeResolution() {
        Permanent gamma = addCreatureReady(player1, new GammaGrotesque());
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
        assertThat(gamma.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Power-up still draws for remaining creatures if its source dies in response")
    void drawsWhenSourceDiesBeforeResolution() {
        Permanent gamma = addCreatureReady(player1, new GammaGrotesque());
        Permanent other = addCreatureReady(player1, new GammaGrotesque());
        other.getCounters().put(CounterType.CHARGE, 1);
        Card draw = new Shock();
        Card remaining = new Shock();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(draw, remaining));
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.castInstant(player2, 0, gamma.getId());
        harness.castInstant(player2, 0, gamma.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(gamma);
        harness.passBothPriorities();

        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
    }
}
