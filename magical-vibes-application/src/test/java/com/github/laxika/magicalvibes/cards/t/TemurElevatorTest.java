package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TemurElevator.class, Forest.class})
class TemurElevatorTest extends BaseCardTest {

    @Test
    @DisplayName("Adds a chosen Temur color and costs 1 life without the city's blessing")
    void tapsForChosenColorAndLosesLifeWithoutBlessing() {
        harness.addToBattlefield(player1, new TemurElevator());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isOne();
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Does not cost life once the city's blessing is received")
    void doesNotLoseLifeWithBlessing() {
        gd.playersWithCityBlessing.add(player1.getId());
        harness.addToBattlefield(player1, new TemurElevator());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isOne();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Ascend grants the city's blessing at ten permanents")
    void ascendGrantsCityBlessing() {
        for (int i = 0; i < 9; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.setHand(player1, List.of(new TemurElevator()));

        harness.playLand(player1, 0);

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
    }
}
