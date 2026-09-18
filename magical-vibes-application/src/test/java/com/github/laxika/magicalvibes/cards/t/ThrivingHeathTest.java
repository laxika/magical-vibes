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

@CardUsed(ThrivingHeath.class)
class ThrivingHeathTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and lets you choose any color except white")
    void entersTappedAndRestrictsChosenColor() {
        harness.setHand(player1, List.of(new ThrivingHeath()));

        harness.playLand(player1, 0);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactly("BLUE", "BLACK", "RED", "GREEN");

        harness.handleListChoice(player1, "BLUE");

        Permanent heath = findPermanent(player1, "Thriving Heath");
        assertThat(heath.isTapped()).isTrue();
        assertThat(heath.getChosenColor()).isEqualTo(CardColor.BLUE);
    }

    @Test
    @DisplayName("Tapping adds white mana")
    void tapsForWhiteMana() {
        Permanent heath = addReadyHeath(player1, CardColor.BLUE);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(heath.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping adds one mana of the chosen color")
    void tapsForChosenColorMana() {
        Permanent heath = addReadyHeath(player1, CardColor.RED);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(heath.isTapped()).isTrue();
    }

    private Permanent addReadyHeath(Player player, CardColor chosenColor) {
        Permanent heath = new Permanent(new ThrivingHeath());
        heath.setSummoningSick(false);
        heath.setChosenColor(chosenColor);
        gd.playerBattlefields.get(player.getId()).add(heath);
        return heath;
    }
}
