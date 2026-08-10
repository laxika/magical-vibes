package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CityOfTraitorsTest extends BaseCardTest {

    @Test
    @DisplayName("Playing another land sacrifices City of Traitors")
    void playingAnotherLandSacrifices() {
        harness.addToBattlefield(player1, new CityOfTraitors());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "City of Traitors");
        harness.assertInGraveyard(player1, "City of Traitors");
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Putting another land onto the battlefield does not sacrifice City of Traitors")
    void puttingAnotherLandOntoBattlefieldDoesNotSacrifice() {
        harness.addToBattlefield(player1, new CityOfTraitors());
        harness.addToBattlefield(player1, new Forest());

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "City of Traitors");
    }

    @Test
    @DisplayName("Tapping City of Traitors adds two colorless mana")
    void tappingAddsTwoColorlessMana() {
        harness.addToBattlefield(player1, new CityOfTraitors());

        harness.activateAbility(player1, 0, 0, null, null);

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gameData.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent playing a land does not sacrifice City of Traitors")
    void opponentPlayingLandDoesNotSacrifice() {
        harness.addToBattlefield(player1, new CityOfTraitors());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Forest()));

        harness.playLand(player2, 0);

        harness.assertOnBattlefield(player1, "City of Traitors");
    }
}
