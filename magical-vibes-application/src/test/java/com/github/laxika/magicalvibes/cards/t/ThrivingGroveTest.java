package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThrivingGrove.class})
class ThrivingGroveTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and chooses a non-green color")
    void entersTappedAndChoosesNonGreenColor() {
        harness.setHand(player1, List.of(new ThrivingGrove()));
        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Thriving Grove").isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "RED");

        assertThat(findPermanent(player1, "Thriving Grove").getChosenColor()).isEqualTo(CardColor.RED);
    }

    @Test
    @DisplayName("Can tap for green mana")
    void tapsForGreen() {
        Permanent grove = addReadyGrove(CardColor.RED);

        harness.activateAbility(player1, 0, 0, null, null);

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(pool.get(ManaColor.RED)).isZero();
        assertThat(grove.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can tap for its chosen color")
    void tapsForChosenColor() {
        Permanent grove = addReadyGrove(CardColor.BLUE);

        harness.activateAbility(player1, 0, 1, null, null);

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(pool.get(ManaColor.GREEN)).isZero();
        assertThat(grove.isTapped()).isTrue();
    }

    private Permanent addReadyGrove(CardColor chosenColor) {
        Permanent grove = new Permanent(new ThrivingGrove());
        grove.setSummoningSick(false);
        grove.setChosenColor(chosenColor);
        gd.playerBattlefields.get(player1.getId()).add(grove);
        return grove;
    }

    @Test
    @DisplayName("Enters tapped and allows choosing any color other than green")
    void entersTappedAndRestrictsColorChoice() {
        harness.setHand(player1, List.of(new ThrivingGrove()));

        harness.playLand(player1, 0);

        Permanent grove = findPermanent(player1, "Thriving Grove");
        assertThat(grove.isTapped()).isTrue();
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("WHITE", "BLUE", "BLACK", "RED");

        harness.handleListChoice(player1, "BLUE");

        assertThat(grove.getChosenColor()).isEqualTo(CardColor.BLUE);
    }

    @Test
    @DisplayName("The two mana abilities add green or the chosen color")
    void addsGreenOrChosenColorMana() {
        Permanent grove = addReadyGroveAlternative(CardColor.RED);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);

        grove.untap();
        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(grove.isTapped()).isTrue();
    }

    private Permanent addReadyGroveAlternative(CardColor chosenColor) {
        Permanent grove = new Permanent(new ThrivingGrove());
        grove.setSummoningSick(false);
        grove.setChosenColor(chosenColor);
        gd.playerBattlefields.get(player1.getId()).add(grove);
        return grove;
    }
}
