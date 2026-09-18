package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
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
    @DisplayName("Enters tapped and chooses a non-blue color")
    void entersTappedAndChoosesNonBlueColor() {
        harness.setHand(player1, List.of(new ThrivingIsle()));
        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Thriving Isle").isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "RED");

        assertThat(findPermanent(player1, "Thriving Isle").getChosenColor()).isEqualTo(CardColor.RED);
    }

    @Test
    @DisplayName("Can tap for blue mana")
    void tapsForBlue() {
        Permanent isle = addReadyIsle(CardColor.RED);

        harness.activateAbility(player1, 0, 0, null, null);

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(pool.get(ManaColor.RED)).isZero();
        assertThat(isle.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can tap for its chosen color")
    void tapsForChosenColor() {
        Permanent isle = addReadyIsle(CardColor.GREEN);

        harness.activateAbility(player1, 0, 1, null, null);

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(pool.get(ManaColor.BLUE)).isZero();
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
