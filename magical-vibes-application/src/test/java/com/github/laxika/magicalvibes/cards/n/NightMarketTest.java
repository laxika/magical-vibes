package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NightMarketTest extends BaseCardTest {

    @Test
    @DisplayName("Night Market enters tapped and chooses a color")
    void entersTappedAndChoosesColor() {
        harness.setHand(player1, List.of(new NightMarket()));
        harness.playLand(player1, 0);

        Permanent market = findPermanent(player1, "Night Market");
        assertThat(market.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "BLUE");

        assertThat(market.getChosenColor()).isEqualTo(CardColor.BLUE);
    }

    @Test
    @DisplayName("Night Market taps for one mana of its chosen color")
    void tapsForChosenColor() {
        Permanent market = harness.addToBattlefieldAndReturn(player1, new NightMarket());
        market.setSummoningSick(false);
        market.setChosenColor(CardColor.RED);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cycling Night Market discards it and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new NightMarket()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Night Market");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
