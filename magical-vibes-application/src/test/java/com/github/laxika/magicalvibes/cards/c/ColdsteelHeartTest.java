package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ColdsteelHeartTest extends BaseCardTest {

    @Test
    @DisplayName("Coldsteel Heart enters tapped and asks its controller to choose a color")
    void entersTappedAndChoosesColor() {
        harness.setHand(player1, List.of(new ColdsteelHeart()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
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
        heart.setSummoningSick(false);
        heart.setChosenColor(CardColor.RED);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }
}
