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

@CardUsed(ThrivingHeath.class)
class ThrivingHeathTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and chooses a non-white color")
    void entersTappedAndChoosesNonWhiteColor() {
        harness.setHand(player1, List.of(new ThrivingHeath()));
        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Thriving Heath").isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "GREEN");

        assertThat(findPermanent(player1, "Thriving Heath").getChosenColor()).isEqualTo(CardColor.GREEN);
    }

    @Test
    @DisplayName("Can tap for white mana")
    void tapsForWhite() {
        Permanent heath = addReadyHeath(CardColor.GREEN);

        harness.activateAbility(player1, 0, 0, null, null);

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(pool.get(ManaColor.GREEN)).isZero();
        assertThat(heath.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can tap for its chosen color")
    void tapsForChosenColor() {
        Permanent heath = addReadyHeath(CardColor.BLUE);

        harness.activateAbility(player1, 0, 1, null, null);

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(pool.get(ManaColor.WHITE)).isZero();
        assertThat(heath.isTapped()).isTrue();
    }

    private Permanent addReadyHeath(CardColor chosenColor) {
        Permanent heath = new Permanent(new ThrivingHeath());
        heath.setSummoningSick(false);
        heath.setChosenColor(chosenColor);
        gd.playerBattlefields.get(player1.getId()).add(heath);
        return heath;
    }
}
