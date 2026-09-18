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

@CardUsed(ThrivingGrove.class)
class ThrivingGroveTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and lets you choose any color except green")
    void entersTappedAndRestrictsChosenColor() {
        harness.setHand(player1, List.of(new ThrivingGrove()));

        harness.playLand(player1, 0);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactly("WHITE", "BLUE", "BLACK", "RED");

        harness.handleListChoice(player1, "BLUE");

        Permanent grove = findPermanent(player1, "Thriving Grove");
        assertThat(grove.isTapped()).isTrue();
        assertThat(grove.getChosenColor()).isEqualTo(CardColor.BLUE);
    }

    @Test
    @DisplayName("Tapping adds green mana")
    void tapsForGreenMana() {
        Permanent grove = addReadyGrove(player1, CardColor.BLUE);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(grove.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping adds one mana of the chosen color")
    void tapsForChosenColorMana() {
        Permanent grove = addReadyGrove(player1, CardColor.RED);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(grove.isTapped()).isTrue();
    }

    private Permanent addReadyGrove(Player player, CardColor chosenColor) {
        Permanent grove = new Permanent(new ThrivingGrove());
        grove.setSummoningSick(false);
        grove.setChosenColor(chosenColor);
        gd.playerBattlefields.get(player.getId()).add(grove);
        return grove;
    }
}
