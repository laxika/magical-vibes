package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(EmpoweredAutogenerator.class)
class EmpoweredAutogeneratorTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped")
    void entersTapped() {
        Permanent generator = harness.enterBattlefieldAndReturn(player1, new EmpoweredAutogenerator());

        assertThat(generator.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Adds mana equal to its charge counters after adding a charge counter")
    void addsManaEqualToNewChargeCounterTotal() {
        Permanent generator = readyGenerator();
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(generator.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(generator.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Adds the full updated charge-counter total on later activations")
    void laterActivationUsesUpdatedChargeCounterTotal() {
        Permanent generator = readyGenerator();
        generator.setCounterCount(CounterType.CHARGE, 2);
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(generator.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(3);
    }

    private Permanent readyGenerator() {
        Permanent generator = harness.addToBattlefieldAndReturn(player1, new EmpoweredAutogenerator());
        generator.untap();
        return generator;
    }
}
