package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NightMarket.class})
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
        harness.setLibrary(player1, List.of(new NightMarket()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Night Market");
        harness.assertInHand(player1, "Night Market");
    }

    @ParameterizedTest
    @EnumSource(value = CardColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void chosenColorProducesManaImmediatelyWithoutUsingTheStack(CardColor color) {
        harness.setHand(player1, List.of(new NightMarket()));
        harness.playLand(player1, 0);
        harness.handleListChoice(player1, color.name());
        Permanent market = findPermanent(player1, "Night Market");
        market.untap();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(market.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.valueOf(color.name()))).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateManaAbilityWhileTapped() {
        harness.setHand(player1, List.of(new NightMarket()));
        harness.playLand(player1, 0);
        harness.handleListChoice(player1, "GREEN");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void cyclingPaysGenericManaAndDiscardsBeforeDrawing() {
        NightMarket market = new NightMarket();
        NightMarket drawnCard = new NightMarket();
        harness.setHand(player1, List.of(market));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(market);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotCycleWithoutThreeMana() {
        NightMarket market = new NightMarket();
        harness.setHand(player1, List.of(market));
        harness.setLibrary(player1, List.of(new NightMarket()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(market);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }
}
