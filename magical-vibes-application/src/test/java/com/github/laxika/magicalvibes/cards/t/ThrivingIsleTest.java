package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ThrivingIsle.class)
class ThrivingIsleTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and lets you choose any color except blue")
    void entersTappedAndRestrictsChosenColor() {
        harness.setHand(player1, List.of(new ThrivingIsle()));

        harness.playLand(player1, 0);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactly("WHITE", "BLACK", "RED", "GREEN");

        harness.handleListChoice(player1, "WHITE");

        Permanent isle = findPermanent(player1, "Thriving Isle");
        assertThat(isle.isTapped()).isTrue();
        assertThat(isle.getChosenColor()).isEqualTo(CardColor.WHITE);
    }

    @Test
    @DisplayName("Tapping adds blue mana")
    void tapsForBlueMana() {
        Permanent isle = addReadyIsle(player1, CardColor.WHITE);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(isle.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping adds one mana of the chosen color")
    void tapsForChosenColorMana() {
        Permanent isle = addReadyIsle(player1, CardColor.RED);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(isle.isTapped()).isTrue();
    }

    private Permanent addReadyIsle(Player player, CardColor chosenColor) {
        Permanent isle = new Permanent(new ThrivingIsle());
        isle.setSummoningSick(false);
        isle.setChosenColor(chosenColor);
        gd.playerBattlefields.get(player.getId()).add(isle);
        return isle;
    }
}
