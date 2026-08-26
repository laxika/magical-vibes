package com.github.laxika.magicalvibes.cards.m;

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

@CardUsed(MirageMesa.class)
class MirageMesaTest extends BaseCardTest {

    @Test
    @DisplayName("Mirage Mesa enters tapped and lets its controller choose a color")
    void entersTappedAndChoosesColor() {
        harness.setHand(player1, List.of(new MirageMesa()));
        harness.playLand(player1, 0);

        Permanent mesa = findPermanent(player1, "Mirage Mesa");
        assertThat(mesa.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "BLUE");

        assertThat(mesa.getChosenColor()).isEqualTo(CardColor.BLUE);
    }

    @Test
    @DisplayName("Mirage Mesa taps for one mana of its chosen color")
    void tapsForChosenColor() {
        Permanent mesa = harness.addToBattlefieldAndReturn(player1, new MirageMesa());
        mesa.setSummoningSick(false);
        mesa.setChosenColor(CardColor.RED);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(mesa.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
