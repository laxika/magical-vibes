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

    @Test
    @DisplayName("Adds red mana immediately and taps the land without using the stack")
    void addsRedManaAsManaAbility() {
        var elevator = harness.addToBattlefieldAndReturn(player1, new TemurElevator());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(elevator.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isOne();
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Nine permanents do not grant the city's blessing")
    void ninePermanentsDoNotGrantBlessing() {
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new TemurElevator());
        }
        harness.setHand(player1, List.of(new TemurElevator()));
        harness.setLife(player1, 20);

        harness.playLand(player1, 0);
        harness.activateAbility(player1, 8, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Opponent's permanents do not count toward ascend")
    void opposingPermanentsDoNotCountForAscend() {
        for (int i = 0; i < 9; i++) {
            harness.addToBattlefield(player2, new TemurElevator());
        }
        harness.setHand(player1, List.of(new TemurElevator()));

        harness.playLand(player1, 0);

        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());
    }

    @Test
    @DisplayName("The city's blessing persists below ten permanents and prevents life loss")
    void blessingPersistsAfterLosingPermanents() {
        for (int i = 0; i < 9; i++) {
            harness.addToBattlefield(player1, new TemurElevator());
        }
        harness.setHand(player1, List.of(new TemurElevator()));
        harness.setLife(player1, 20);
        harness.playLand(player1, 0);
        assertThat(gd.playersWithCityBlessing).contains(player1.getId());

        gd.playerBattlefields.get(player1.getId()).subList(1, 10).clear();
        harness.runStateBasedActions();
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isOne();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }
}
