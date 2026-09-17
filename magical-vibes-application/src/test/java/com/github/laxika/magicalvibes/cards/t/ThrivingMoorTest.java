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

@CardUsed(ThrivingMoor.class)
class ThrivingMoorTest extends BaseCardTest {

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
}
