package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ThrivingIsle.class)
class ThrivingIsleTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and allows choosing any color other than blue")
    void entersTappedAndRestrictsColorChoice() {
        harness.setHand(player1, List.of(new ThrivingIsle()));

        harness.playLand(player1, 0);

        Permanent isle = findPermanent(player1, "Thriving Isle");
        assertThat(isle.isTapped()).isTrue();
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("WHITE", "BLACK", "RED", "GREEN");

        harness.handleListChoice(player1, "RED");

        assertThat(isle.getChosenColor()).isEqualTo(CardColor.RED);
    }

    @Test
    @DisplayName("The two mana abilities add blue or the chosen color")
    void addsBlueOrChosenColorMana() {
        Permanent isle = addReadyIsle(CardColor.GREEN);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);

        isle.untap();
        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(isle.isTapped()).isTrue();
    }

    private Permanent addReadyIsle(CardColor chosenColor) {
        Permanent isle = new Permanent(new ThrivingIsle());
        isle.setSummoningSick(false);
        isle.setChosenColor(chosenColor);
        gd.playerBattlefields.get(player1.getId()).add(isle);
        return isle;
    }
}
