package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.t.Twiddle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CityOfBrass.class, Twiddle.class})
class CityOfBrassTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the mana ability taps City of Brass and prompts for a color")
    void activateAbilityPromptsManaColor() {
        Permanent city = harness.addToBattlefieldAndReturn(player1, new CityOfBrass());

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
            gd = harness.getGameData();
            ManaColor manaColor = ManaColor.valueOf(color);

            int totalBefore = gd.playerManaPools.get(player1.getId()).getTotalAllMana();
            harness.activateAbility(player1, 0, null, null);
            int before = gd.playerManaPools.get(player1.getId()).get(manaColor);

            harness.handleListChoice(player1, color);

            assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor)).isEqualTo(before + 1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(totalBefore + 1);
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

        harness.assertLife(player1, 20);
        resolveAllTriggers();

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Tapping City of Brass for mana deals 1 damage to its controller")
    void becomingTappedDealsOneDamageToControllerUpstreamReview() {
        harness.addToBattlefield(player1, new CityOfBrass());
        harness.forceActivePlayer(player1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");

        resolveAllTriggers();

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Tapping an opponent's City of Brass damages that land's controller")
    void tappingOpponentsCityDamagesItsController() {
        Permanent city = harness.addToBattlefieldAndReturn(player2, new CityOfBrass());
        harness.setHand(player1, List.of(new Twiddle()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, city.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Tapping one City of Brass by another ability deals damage only for that City")
    void tappingOneCityByAnotherAbilityTriggersOnlyThatCity() {
        Permanent city = harness.addToBattlefieldAndReturn(player1, new CityOfBrass());
        Permanent otherCity = harness.addToBattlefieldAndReturn(player1, new CityOfBrass());
        harness.setHand(player1, List.of(new Twiddle()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setLife(player1, 20);

        harness.castAndResolveInstant(player1, 0, city.getId());
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(city.isTapped()).isTrue();
        assertThat(otherCity.isTapped()).isFalse();
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Tapping a City of Brass controlled by another player damages its controller")
    void tappingCityByOpponentAbilityDamagesItsController() {
        Permanent city = harness.addToBattlefieldAndReturn(player1, new CityOfBrass());
        harness.setHand(player2, List.of(new Twiddle()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.castAndResolveInstant(player2, 0, city.getId());
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(city.isTapped()).isTrue();
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Untapping City of Brass does not trigger its damage ability")
    void untappingCityDoesNotTrigger() {
        Permanent city = harness.addToBattlefieldAndReturn(player1, new CityOfBrass());
        city.tap();
        harness.setHand(player1, List.of(new Twiddle()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setLife(player1, 20);

        harness.castAndResolveInstant(player1, 0, city.getId());
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(city.isTapped()).isFalse();
        harness.assertLife(player1, 20);
    }
}
