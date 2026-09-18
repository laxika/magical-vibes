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

@CardUsed(ThrivingMoor.class)
class ThrivingMoorTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and lets you choose any color except black")
    void entersTappedAndRestrictsChosenColor() {
        harness.setHand(player1, List.of(new ThrivingMoor()));

        harness.playLand(player1, 0);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactly("WHITE", "BLUE", "RED", "GREEN");

        harness.handleListChoice(player1, "BLUE");

        Permanent moor = findPermanent(player1, "Thriving Moor");
        assertThat(moor.isTapped()).isTrue();
        assertThat(moor.getChosenColor()).isEqualTo(CardColor.BLUE);
    }

    @Test
    @DisplayName("Tapping adds black mana")
    void tapsForBlackMana() {
        Permanent moor = addReadyMoor(player1, CardColor.BLUE);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(moor.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping adds one mana of the chosen color")
    void tapsForChosenColorMana() {
        Permanent moor = addReadyMoor(player1, CardColor.RED);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(moor.isTapped()).isTrue();
    }

    private Permanent addReadyMoor(Player player, CardColor chosenColor) {
        Permanent moor = new Permanent(new ThrivingMoor());
        moor.setSummoningSick(false);
        moor.setChosenColor(chosenColor);
        gd.playerBattlefields.get(player.getId()).add(moor);
        return moor;
    }
}
