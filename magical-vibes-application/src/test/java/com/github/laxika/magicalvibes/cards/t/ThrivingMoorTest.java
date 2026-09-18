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

@CardUsed({ThrivingMoor.class})
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

    @Test
    @DisplayName("Enters tapped and allows choosing any color other than black")
    void entersTappedAndRestrictsColorChoice() {
        harness.setHand(player1, List.of(new ThrivingMoor()));

        harness.playLand(player1, 0);

        Permanent moor = findPermanent(player1, "Thriving Moor");
        assertThat(moor.isTapped()).isTrue();
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("WHITE", "BLUE", "RED", "GREEN");

        harness.handleListChoice(player1, "BLUE");

        assertThat(moor.getChosenColor()).isEqualTo(CardColor.BLUE);
    }

    @Test
    @DisplayName("The two mana abilities add black or the chosen color")
    void addsBlackOrChosenColorMana() {
        Permanent moor = new Permanent(new ThrivingMoor());
        moor.setSummoningSick(false);
        moor.setChosenColor(CardColor.RED);
        gd.playerBattlefields.get(player1.getId()).add(moor);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);

        moor.untap();
        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(moor.isTapped()).isTrue();
    }
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
