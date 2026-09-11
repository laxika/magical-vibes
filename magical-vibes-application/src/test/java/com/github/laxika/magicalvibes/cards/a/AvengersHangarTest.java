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

@CardUsed(AvengersHangar.class)
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

    private void producesChosenMana(String choice, ManaColor manaColor) {
        Permanent hangar = new Permanent(new AvengersHangar());
        hangar.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(hangar);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, choice);

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerManaPools.get(player1.getId()).get(manaColor)).isEqualTo(1);
        assertThat(hangar.isTapped()).isTrue();
    }
}
