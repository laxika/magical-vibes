package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MouserMarkIII;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WeatherMaker.class, Forest.class, MouserMarkIII.class})
class WeatherMakerTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall puts a charge counter on Weather Maker")
    void landfallPutsChargeCounter() {
        Permanent weatherMaker = harness.addToBattlefieldAndReturn(player1, new WeatherMaker());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(weatherMaker.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The first ability adds one mana of the chosen color")
    void addsOneManaOfChosenColor() {
        harness.addToBattlefield(player1, new WeatherMaker());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The second ability removes two charge counters and adds two colorless mana")
    void removesTwoCountersAndAddsTwoColorlessMana() {
        Permanent weatherMaker = harness.addToBattlefieldAndReturn(player1, new WeatherMaker());
        weatherMaker.setCounterCount(CounterType.CHARGE, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(weatherMaker.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    @DisplayName("The third ability removes three charge counters and deals 3 damage")
    void removesThreeCountersAndDealsDamage() {
        Permanent weatherMaker = harness.addToBattlefieldAndReturn(player1, new WeatherMaker());
        weatherMaker.setCounterCount(CounterType.CHARGE, 3);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        assertThat(weatherMaker.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("The damage ability cannot target a land")
    void damageAbilityRejectsLandTarget() {
        Permanent weatherMaker = harness.addToBattlefieldAndReturn(player1, new WeatherMaker());
        weatherMaker.setCounterCount(CounterType.CHARGE, 3);
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(weatherMaker.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent's land does not trigger landfall")
    void opponentLandDoesNotAddCounter() {
        Permanent weatherMaker = harness.addToBattlefieldAndReturn(player1, new WeatherMaker());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Forest()));

        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(weatherMaker.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("The colorless mana ability requires two charge counters")
    void colorlessManaRequiresEnoughCounters() {
        Permanent weatherMaker = harness.addToBattlefieldAndReturn(player1, new WeatherMaker());
        weatherMaker.setCounterCount(CounterType.CHARGE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(weatherMaker.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(weatherMaker.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("The damage ability requires three charge counters")
    void damageRequiresEnoughCounters() {
        Permanent weatherMaker = harness.addToBattlefieldAndReturn(player1, new WeatherMaker());
        weatherMaker.setCounterCount(CounterType.CHARGE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(weatherMaker.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(weatherMaker.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Counter removal is paid immediately and damage waits for resolution")
    void damageCostsArePaidBeforeResolution() {
        Permanent weatherMaker = harness.addToBattlefieldAndReturn(player1, new WeatherMaker());
        weatherMaker.setCounterCount(CounterType.CHARGE, 5);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 2, null, player1.getId());

        assertThat(weatherMaker.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(weatherMaker.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 20);

        harness.passBothPriorities();

        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Colorless mana resolves immediately and tapping prevents another activation")
    void manaResolvesImmediatelyAndTapsSource() {
        Permanent weatherMaker = harness.addToBattlefieldAndReturn(player1, new WeatherMaker());
        weatherMaker.setCounterCount(CounterType.CHARGE, 5);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(weatherMaker.isTapped()).isTrue();
        assertThat(weatherMaker.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(weatherMaker.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("The damage ability can deal lethal damage to a creature")
    void damageCanKillCreature() {
        Permanent weatherMaker = harness.addToBattlefieldAndReturn(player1, new WeatherMaker());
        weatherMaker.setCounterCount(CounterType.CHARGE, 3);
        Permanent mouser = harness.addToBattlefieldAndReturn(player2, new MouserMarkIII());

        harness.activateAbility(player1, 0, 2, null, mouser.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mouser Mark III");
        harness.assertInGraveyard(player2, "Mouser Mark III");
    }
}
