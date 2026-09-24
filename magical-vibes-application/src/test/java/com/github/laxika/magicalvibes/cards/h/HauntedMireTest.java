package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(HauntedMire.class)
class HauntedMireTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new HauntedMire()));

        harness.playLand(player1, 0);

        Permanent hauntedMire = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(hauntedMire.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for black mana produces one black")
    void tappingProducesBlackMana() {
        Permanent hauntedMire = addReadyHauntedMire();

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLACK");
        GameData gameData = harness.getGameData();

        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(hauntedMire.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for green mana produces one green")
    void tappingProducesGreenMana() {
        Permanent hauntedMire = addReadyHauntedMire();

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        GameData gameData = harness.getGameData();

        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(hauntedMire.isTapped()).isTrue();
    }

    private Permanent addReadyHauntedMire() {
        Permanent hauntedMire = new Permanent(new HauntedMire());
        hauntedMire.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(hauntedMire);
        return hauntedMire;
    }
}
