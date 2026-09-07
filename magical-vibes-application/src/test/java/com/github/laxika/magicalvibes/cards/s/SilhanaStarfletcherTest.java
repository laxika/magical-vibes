package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed(SilhanaStarfletcher.class)
class SilhanaStarfletcherTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield asks its controller to choose a color")
    void entersAskingForColor() {
        harness.setHand(player1, List.of(new SilhanaStarfletcher()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "BLUE");

        assertThat(findPermanent(player1, "Silhana Starfletcher").getChosenColor()).isEqualTo(CardColor.BLUE);
    }

    @Test
    @DisplayName("The tap ability adds one mana of the chosen color")
    void addsChosenColorMana() {
        Permanent starfletcher = new Permanent(new SilhanaStarfletcher());
        starfletcher.setSummoningSick(false);
        starfletcher.setChosenColor(CardColor.BLUE);
        gd.playerBattlefields.get(player1.getId()).add(starfletcher);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(starfletcher.isTapped()).isTrue();
    }
}
