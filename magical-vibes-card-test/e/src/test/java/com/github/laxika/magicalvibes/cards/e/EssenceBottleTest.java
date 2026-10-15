package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EssenceBottle.class, Disenchant.class})
class EssenceBottleTest extends BaseCardTest {

    @Test
    @DisplayName("First ability puts an elixir counter on the bottle")
    void firstAbilityAddsElixirCounter() {
        Permanent bottle = addReadyBottle(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(bottle.getCounterCount(CounterType.ELIXIR)).isEqualTo(1);
    }

    @Test
    @DisplayName("Second ability removes all elixir counters and gains 2 life for each")
    void secondAbilityGainsTwoLifePerCounter() {
        Permanent bottle = addReadyBottle(player1);
        bottle.setCounterCount(CounterType.ELIXIR, 3);
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);

        // Counters are removed immediately as a cost
        assertThat(bottle.getCounterCount(CounterType.ELIXIR)).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 6);
    }

    @Test
    @DisplayName("Second ability with no elixir counters gains no life")
    void secondAbilityWithNoCountersGainsNoLife() {
        addReadyBottle(player1);
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife);
    }

    @Test
    @DisplayName("Second ability removes only elixir counters")
    void secondAbilityLeavesOtherCounterTypes() {
        Permanent bottle = addReadyBottle(player1);
        bottle.setCounterCount(CounterType.ELIXIR, 2);
        bottle.setCounterCount(CounterType.CHARGE, 1);
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(bottle.getCounterCount(CounterType.ELIXIR)).isZero();
        assertThat(bottle.getCounterCount(CounterType.CHARGE)).isOne();

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 4);
        assertThat(bottle.getCounterCount(CounterType.CHARGE)).isOne();
    }

    @Test
    @DisplayName("Both abilities require tapping — a tapped bottle cannot activate")
    void tappedBottleCannotActivate() {
        Permanent bottle = addReadyBottle(player1);
        bottle.setCounterCount(CounterType.ELIXIR, 2);
        bottle.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("First ability cannot be activated without paying {3}")
    void firstAbilityRequiresThreeMana() {
        addReadyBottle(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A newly entered noncreature bottle can activate its tap ability")
    void newlyEnteredBottleCanActivate() {
        Permanent bottle = harness.addToBattlefieldAndReturn(player1, new EssenceBottle());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(bottle.isTapped()).isTrue();
        assertThat(bottle.getCounterCount(CounterType.ELIXIR)).isZero();

        harness.passBothPriorities();

        assertThat(bottle.getCounterCount(CounterType.ELIXIR)).isOne();
    }

    @Test
    @DisplayName("Life gain uses the counters paid rather than counters present at resolution")
    void lifeGainUsesCountersRemovedAtActivation() {
        Permanent bottle = addReadyBottle(player1);
        bottle.setCounterCount(CounterType.ELIXIR, 3);
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(bottle.isTapped()).isTrue();
        assertThat(bottle.getCounterCount(CounterType.ELIXIR)).isZero();
        harness.assertLife(player1, startingLife);
        bottle.setCounterCount(CounterType.ELIXIR, 1);

        harness.passBothPriorities();

        harness.assertLife(player1, startingLife + 6);
        assertThat(bottle.getCounterCount(CounterType.ELIXIR)).isOne();
    }

    @Test
    @DisplayName("The activating player gains the life even when they are player two")
    void playerTwoGainsLifeFromTheirBottle() {
        Permanent bottle = addReadyBottle(player2);
        bottle.setCounterCount(CounterType.ELIXIR, 2);
        int playerOneLife = gd.playerLifeTotals.get(player1.getId());
        int playerTwoLife = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player2, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, playerOneLife);
        harness.assertLife(player2, playerTwoLife + 4);
    }

    @Test
    @DisplayName("Life gain still resolves after the bottle is destroyed in response")
    void lifeGainSurvivesBottleDestruction() {
        Permanent bottle = addReadyBottle(player1);
        bottle.setCounterCount(CounterType.ELIXIR, 3);
        int startingLife = gd.playerLifeTotals.get(player1.getId());
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.castInstant(player2, 0, bottle.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Essence Bottle");
        harness.assertLife(player1, startingLife);

        harness.passBothPriorities();

        harness.assertLife(player1, startingLife + 6);
    }

    @Test
    @DisplayName("A counter-adding ability does not affect a replacement bottle")
    void counterAbilityDoesNotAffectReplacementBottle() {
        Permanent bottle = addReadyBottle(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.castInstant(player2, 0, bottle.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Essence Bottle");
        Permanent replacement = harness.addToBattlefieldAndReturn(player1, new EssenceBottle());
        harness.passBothPriorities();

        assertThat(replacement.getCounterCount(CounterType.ELIXIR)).isZero();
    }

    private Permanent addReadyBottle(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new EssenceBottle());
        perm.setSummoningSick(false);
        return perm;
    }
}
