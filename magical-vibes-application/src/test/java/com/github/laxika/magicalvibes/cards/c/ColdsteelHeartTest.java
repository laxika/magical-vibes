package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ColdsteelHeart.class)
class ColdsteelHeartTest extends BaseCardTest {

    @Test
    @DisplayName("Coldsteel Heart enters tapped and asks its controller to choose a color")
    void entersTappedAndChoosesColor() {
        harness.castFromHand(player1, new ColdsteelHeart(), "{2}");
        harness.passBothPriorities();

        Permanent heart = findPermanent(player1, "Coldsteel Heart");
        assertThat(heart.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "BLUE");

        assertThat(heart.getChosenColor()).isEqualTo(CardColor.BLUE);
    }

    @Test
    @DisplayName("Coldsteel Heart taps for one mana of its chosen color")
    void tapsForChosenColor() {
        Permanent heart = harness.addToBattlefieldAndReturn(player1, new ColdsteelHeart());
        heart.setChosenColor(CardColor.RED);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(heart.isTapped()).isTrue();
    }
}
