package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThornwoodFallsTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield tapped gains 1 life")
    void entersTappedAndGainsOneLife() {
        harness.setHand(player1, List.of(new ThornwoodFalls()));

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent falls = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(falls.isTapped()).isTrue();
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Tapping for green mana produces one green")
    void tappingProducesGreenMana() {
        Permanent falls = addFallsReady(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        GameData gameData = harness.getGameData();

        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(falls.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for blue mana produces one blue")
    void tappingProducesBlueMana() {
        Permanent falls = addFallsReady(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");
        GameData gameData = harness.getGameData();

        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(falls.isTapped()).isTrue();
    }

    private Permanent addFallsReady(Player player) {
        Permanent perm = new Permanent(new ThornwoodFalls());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
