package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(BigApple3AM.class)
class BigApple3AMTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and stores the chosen color")
    void entersTappedAndStoresChosenColor() {
        harness.setHand(player1, List.of(new BigApple3AM()));

        harness.playLand(player1, 0);

        Permanent land = findPermanent(player1, "Big Apple, 3 a.m.");
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "BLUE");

        assertThat(land.getChosenColor()).isEqualTo(CardColor.BLUE);
    }

    @Test
    @DisplayName("Tapping adds one mana of the chosen color")
    void tappingAddsChosenColorMana() {
        Permanent land = addReadyLand(CardColor.GREEN);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Creates one black Rat token for the opponent")
    void createsRatForEachOpponent() {
        addReadyLand(CardColor.BLACK);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        List<Permanent> rats = findPermanents(player1, "Rat");
        assertThat(rats).hasSize(1);
        assertThat(rats.getFirst().getCard().getColors()).containsExactly(CardColor.BLACK);
        assertThat(rats.getFirst().getCard().getSubtypes()).contains(CardSubtype.RAT);
        assertThat(rats.getFirst().getEffectivePower()).isEqualTo(1);
        assertThat(rats.getFirst().getEffectiveToughness()).isEqualTo(1);
    }

    private Permanent addReadyLand(CardColor chosenColor) {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new BigApple3AM());
        land.setSummoningSick(false);
        land.setChosenColor(chosenColor);
        return land;
    }
}
