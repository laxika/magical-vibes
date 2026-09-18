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

@CardUsed(ThrivingMoor.class)
class ThrivingMoorTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and chooses a non-black color")
    void entersTappedAndChoosesNonBlackColor() {
        harness.setHand(player1, List.of(new ThrivingMoor()));
        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Thriving Moor").isTapped()).isTrue();
        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactlyInAnyOrder("WHITE", "BLUE", "RED", "GREEN");

        harness.handleListChoice(player1, "GREEN");

        assertThat(findPermanent(player1, "Thriving Moor").getChosenColor()).isEqualTo(CardColor.GREEN);
    }

    @Test
    @DisplayName("Can tap for black mana")
    void tapsForBlack() {
        Permanent moor = addReadyMoor(CardColor.GREEN);

        harness.activateAbility(player1, 0, 0, null, null);

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(pool.get(ManaColor.GREEN)).isZero();
        assertThat(moor.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can tap for its chosen color")
    void tapsForChosenColor() {
        Permanent moor = addReadyMoor(CardColor.BLUE);

        harness.activateAbility(player1, 0, 1, null, null);

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(pool.get(ManaColor.BLACK)).isZero();
        assertThat(moor.isTapped()).isTrue();
    }

    private Permanent addReadyMoor(CardColor chosenColor) {
        Permanent moor = new Permanent(new ThrivingMoor());
        moor.setSummoningSick(false);
        moor.setChosenColor(chosenColor);
        gd.playerBattlefields.get(player1.getId()).add(moor);
        return moor;
    }
}
