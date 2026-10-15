package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AvengersHangar.class})
class AvengersHangarTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and gains 1 life")
    void entersTappedAndGainsLife() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new AvengersHangar()));

        harness.playLand(player1, 0);

        Permanent hangar = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(hangar.isTapped()).isTrue();

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Choosing white produces one white mana")
    void choosingWhiteProducesMana() {
        producesChosenMana("WHITE", ManaColor.WHITE);
    }

    @Test
    @DisplayName("Choosing blue produces one blue mana")
    void choosingBlueProducesMana() {
        producesChosenMana("BLUE", ManaColor.BLUE);
    }

    @Test
    @DisplayName("Life gain waits for resolution and survives the land leaving")
    void lifeGainResolvesAfterLandLeaves() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new AvengersHangar()));
        harness.playLand(player1, 0);

        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);
        Permanent hangar = gd.playerBattlefields.get(player1.getId()).removeFirst();
        harness.setGraveyard(player1, List.of(hangar.getCard()));

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Entering under the other player's control gains life for that player")
    void gainsLifeForEnteringController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 10);

        Permanent hangar = harness.enterBattlefieldAndReturn(player2, new AvengersHangar());
        assertThat(hangar.isTapped()).isTrue();
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 11);
    }

    @Test
    @DisplayName("A land that entered tapped cannot activate its mana ability")
    void cannotProduceManaWhileTapped() {
        harness.setHand(player1, List.of(new AvengersHangar()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    private void producesChosenMana(String choice, ManaColor manaColor) {
        Permanent hangar = harness.addToBattlefieldAndReturn(player1, new AvengersHangar());
        hangar.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, choice);

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerManaPools.get(player1.getId()).get(manaColor)).isEqualTo(1);
        assertThat(hangar.isTapped()).isTrue();
    }
}
