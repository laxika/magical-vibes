package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ThrivingBluff.class)
class ThrivingBluffTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and chooses a non-red color")
    void entersTappedAndChoosesNonRedColor() {
        harness.setHand(player1, List.of(new ThrivingBluff()));
        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Thriving Bluff").isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "GREEN");

        assertThat(findPermanent(player1, "Thriving Bluff").getChosenColor()).isEqualTo(CardColor.GREEN);
    }

    @Test
    @DisplayName("Can tap for red mana")
    void tapsForRed() {
        Permanent bluff = addReadyBluff(CardColor.GREEN);

        harness.activateAbility(player1, 0, 0, null, null);

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.RED)).isEqualTo(1);
        assertThat(pool.get(ManaColor.GREEN)).isZero();
        assertThat(bluff.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can tap for its chosen color")
    void tapsForChosenColor() {
        Permanent bluff = addReadyBluff(CardColor.BLUE);

        harness.activateAbility(player1, 0, 1, null, null);

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(pool.get(ManaColor.RED)).isZero();
        assertThat(bluff.isTapped()).isTrue();
    }

    private Permanent addReadyBluff(CardColor chosenColor) {
        Permanent bluff = new Permanent(new ThrivingBluff());
        bluff.setSummoningSick(false);
        bluff.setChosenColor(chosenColor);
        gd.playerBattlefields.get(player1.getId()).add(bluff);
        return bluff;
    }
    @Test
    @DisplayName("Enters tapped and lets you choose any color except red")
    void entersTappedAndRestrictsChosenColor() {
        harness.setHand(player1, List.of(new ThrivingBluff()));

        harness.playLand(player1, 0);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactly("WHITE", "BLUE", "BLACK", "GREEN");

        harness.handleListChoice(player1, "BLUE");

        Permanent bluff = findPermanent(player1, "Thriving Bluff");
        assertThat(bluff.isTapped()).isTrue();
        assertThat(bluff.getChosenColor()).isEqualTo(CardColor.BLUE);
    }

    @Test
    @DisplayName("Tapping adds red mana")
    void tapsForRedMana() {
        Permanent bluff = addReadyBluff(player1, CardColor.BLUE);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(bluff.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping adds one mana of the chosen color")
    void tapsForChosenColorMana() {
        Permanent bluff = addReadyBluff(player1, CardColor.GREEN);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(bluff.isTapped()).isTrue();
    }

    private Permanent addReadyBluff(Player player, CardColor chosenColor) {
        Permanent bluff = new Permanent(new ThrivingBluff());
        bluff.setSummoningSick(false);
        bluff.setChosenColor(chosenColor);
        gd.playerBattlefields.get(player.getId()).add(bluff);
        return bluff;
    }
}
