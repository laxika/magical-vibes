package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CityOfBrassTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the mana ability taps City of Brass and prompts for a color")
    void activateAbilityPromptsManaColor() {
        harness.addToBattlefield(player1, new CityOfBrass());
        GameData gd = harness.getGameData();
        Permanent city = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.activateAbility(player1, 0, null, null);

        assertThat(city.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    @Test
    @DisplayName("Choosing a color adds exactly one mana of that color")
    void choosingColorAddsOneMana() {
        for (String color : List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN")) {
            harness = new GameTestHarness();
            player1 = harness.getPlayer1();
            harness.skipMulligan();

            harness.addToBattlefield(player1, new CityOfBrass());
            GameData gd = harness.getGameData();
            ManaColor manaColor = ManaColor.valueOf(color);

            harness.activateAbility(player1, 0, null, null);
            int before = gd.playerManaPools.get(player1.getId()).get(manaColor);

            harness.handleListChoice(player1, color);

            assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor)).isEqualTo(before + 1);
            assertThat(gd.interaction.activeInteraction()).isNull();
        }
    }

    @Test
    @DisplayName("Tapping City of Brass for mana deals 1 damage to its controller")
    void becomingTappedDealsOneDamageToController() {
        harness.addToBattlefield(player1, new CityOfBrass());
        harness.forceActivePlayer(player1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");

        // The "becomes tapped" trigger is deferred (CR 603.3) until a player next receives
        // priority; passing priority puts it on the stack and resolves it.
        for (int i = 0; i < 4 && gd.playerLifeTotals.get(player1.getId()) != 19; i++) {
            harness.passBothPriorities();
        }

        harness.assertLife(player1, 19);
    }
}
