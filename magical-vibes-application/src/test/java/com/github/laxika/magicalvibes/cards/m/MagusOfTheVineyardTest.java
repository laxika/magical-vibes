package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(MagusOfTheVineyard.class)
class MagusOfTheVineyardTest extends BaseCardTest {

    @Test
    @DisplayName("The active player adds {G}{G} at the beginning of their first main phase")
    void addsTwoGreenToActivePlayer() {
        harness.addToBattlefield(player1, new MagusOfTheVineyard());

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("The ability gives mana to an opponent when it is their first main phase")
    void addsTwoGreenToOpponentOnOpponentsFirstMain() {
        harness.addToBattlefield(player1, new MagusOfTheVineyard());

        advanceToPrecombatMain(player2);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    private void advanceToPrecombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
