package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CityOfTraitors.class, Forest.class})
class CityOfTraitorsTest extends BaseCardTest {

    @Test
    @DisplayName("Playing City of Traitors does not sacrifice itself")
    void playingCityOfTraitorsDoesNotSacrificeItself() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CityOfTraitors()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "City of Traitors");
        harness.assertNotInGraveyard(player1, "City of Traitors");
    }

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
        harness.enterBattlefieldAndReturn(player1, new Forest());

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

    @Test
    @DisplayName("City of Traitors can produce mana before its sacrifice trigger resolves")
    void canTapForManaInResponseToSacrificeTrigger() {
        harness.addToBattlefield(player1, new CityOfTraitors());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        harness.assertOnBattlefield(player1, "City of Traitors");
        assertThat(gd.stack).hasSize(1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(findPermanent(player1, "City of Traitors").isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "City of Traitors");
        harness.assertInGraveyard(player1, "City of Traitors");
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Playing a second City of Traitors sacrifices only the first City")
    void playingSecondCitySacrificesOnlyFirstCity() {
        var firstCity = harness.addToBattlefieldAndReturn(player1, new CityOfTraitors());
        var secondCity = new CityOfTraitors();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(secondCity));

        harness.playLand(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(countPermanents(player1, "City of Traitors")).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(findPermanent(player1, "City of Traitors").getCard().getId()).isEqualTo(secondCity.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firstCity.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(secondCity);
    }

    @Test
    @DisplayName("Playing another land sacrifices every City of Traitors controlled by that player")
    void playingLandSacrificesBothExistingCities() {
        harness.addToBattlefield(player1, new CityOfTraitors());
        harness.addToBattlefield(player1, new CityOfTraitors());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "City of Traitors")).isEqualTo(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "City of Traitors");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("City of Traitors"))
                .hasSize(2);
        harness.assertOnBattlefield(player1, "Forest");
    }
}
