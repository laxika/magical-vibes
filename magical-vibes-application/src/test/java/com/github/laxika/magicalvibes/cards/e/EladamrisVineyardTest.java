package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(EladamrisVineyard.class)
class EladamrisVineyardTest extends BaseCardTest {

    @Test
    @DisplayName("The controller adds {G}{G} at the beginning of their first main phase")
    void addsTwoGreenOnControllersFirstMain() {
        harness.addToBattlefield(player1, new EladamrisVineyard());

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("The opponent also adds {G}{G} on their own first main phase")
    void addsTwoGreenOnOpponentsFirstMain() {
        harness.addToBattlefield(player1, new EladamrisVineyard());

        advanceToPrecombatMain(player2);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("The mana trigger waits on the stack before adding mana")
    void triggerResolvesFromTheStack() {
        harness.addToBattlefield(player1, new EladamrisVineyard());

        advanceToPrecombatMain(player1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each Vineyard adds {G}{G} independently")
    void eachVineyardAddsTwoGreen() {
        harness.addToBattlefield(player1, new EladamrisVineyard());
        harness.addToBattlefield(player1, new EladamrisVineyard());

        advanceToPrecombatMain(player1);
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(4);
    }

    private void advanceToPrecombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(player, TurnStep.PRECOMBAT_MAIN);
    }
}
