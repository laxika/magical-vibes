package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(TCRIBuilding.class)
class TCRIBuildingTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and gains 1 life")
    void entersTappedAndGainsLife() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new TCRIBuilding()));

        harness.playLand(player1, 0);

        Permanent building = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(building.isTapped()).isTrue();

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Mana ability adds blue mana when blue is chosen")
    void manaAbilityAddsBlueMana() {
        Permanent building = addReadyBuilding(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(building.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana ability adds red mana when red is chosen")
    void manaAbilityAddsRedMana() {
        Permanent building = addReadyBuilding(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(building.isTapped()).isTrue();
    }

    private Permanent addReadyBuilding(Player player) {
        Permanent building = new Permanent(new TCRIBuilding());
        building.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(building);
        return building;
    }
}
