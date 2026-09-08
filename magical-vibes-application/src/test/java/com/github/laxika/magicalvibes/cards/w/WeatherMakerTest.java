package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({WeatherMaker.class, Forest.class})
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
}
