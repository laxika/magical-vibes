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
        return addCreatureReady(player, new TCRIBuilding());
    }

    @Test
    @DisplayName("Life gain waits for the enter trigger to resolve")
    void lifeGainUsesTheStack() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new TCRIBuilding()));

        harness.playLand(player1, 0);

        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Enter trigger gains life even after the land leaves the battlefield")
    void lifeGainSurvivesSourceLeaving() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new TCRIBuilding()));
        harness.playLand(player1, 0);

        Permanent building = gd.playerBattlefields.get(player1.getId()).removeFirst();
        gd.playerGraveyards.get(player1.getId()).add(building.getCard());

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A newly entered noncreature land can produce mana once untapped")
    void newlyEnteredLandCanProduceManaOnceUntapped() {
        harness.setHand(player1, List.of(new TCRIBuilding()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
        Permanent building = gd.playerBattlefields.get(player1.getId()).getFirst();
        building.untap();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(building.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
