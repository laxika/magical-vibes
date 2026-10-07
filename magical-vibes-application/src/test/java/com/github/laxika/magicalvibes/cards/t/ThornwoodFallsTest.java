package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThornwoodFalls.class})
class ThornwoodFallsTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield tapped gains 1 life")
    void entersTappedAndGainsOneLife() {
        harness.setHand(player1, List.of(new ThornwoodFalls()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        Permanent falls = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(falls.isTapped()).isTrue();
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Tapping for green mana produces one green")
    void tappingProducesGreenMana() {
        Permanent falls = harness.addToBattlefieldAndReturn(player1, new ThornwoodFalls());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(falls.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for blue mana produces one blue")
    void tappingProducesBlueMana() {
        Permanent falls = harness.addToBattlefieldAndReturn(player1, new ThornwoodFalls());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(falls.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Life gain uses the stack and survives its source leaving")
    void lifeGainResolvesAfterSourceLeaves() {
        harness.setHand(player1, List.of(new ThornwoodFalls()));

        harness.playLand(player1, 0);

        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The opposing controller gains life when playing the land")
    void opposingControllerGainsLife() {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new ThornwoodFalls()));

        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().isTapped()).isTrue();
        harness.assertLife(player2, 21);
        harness.assertLife(player1, 20);
    }
}
