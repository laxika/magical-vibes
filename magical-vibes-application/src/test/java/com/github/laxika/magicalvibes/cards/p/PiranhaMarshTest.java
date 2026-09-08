package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PiranhaMarshTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and makes the chosen player lose 1 life")
    void entersTappedAndMakesChosenPlayerLoseLife() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new PiranhaMarsh()));

        harness.playLand(player1, 0);

        Permanent marsh = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(marsh.isTapped()).isTrue();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Can target its controller with the enters-the-battlefield ability")
    void canTargetItsController() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new PiranhaMarsh()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Tapping adds one black mana")
    void tapsForBlackMana() {
        Permanent marsh = new Permanent(new PiranhaMarsh());
        marsh.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(marsh);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(marsh.isTapped()).isTrue();
    }
}
