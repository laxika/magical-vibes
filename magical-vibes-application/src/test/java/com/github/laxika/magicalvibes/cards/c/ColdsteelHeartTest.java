package com.github.laxika.magicalvibes.cards.c;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @ParameterizedTest
    @EnumSource(CardColor.class)
    @DisplayName("The color chosen on entry determines the immediately produced snow mana")
    void producesChosenSnowManaAfterUntapping(CardColor color) {
        harness.castFromHand(player1, new ColdsteelHeart(), "{2}");
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice =
                (PendingInteraction.ColorChoice) gd.interaction.activeInteraction();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.options()).containsExactlyInAnyOrder("WHITE", "BLUE", "BLACK", "RED", "GREEN");
        harness.handleListChoice(player1, color.name());
        harness.performUntapStep(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        ManaColor manaColor = ManaColor.valueOf(color.name());
        assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getSnowMana(manaColor)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Coldsteel Heart").isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).get(manaColor)).isZero();
    }

    @Test
    @DisplayName("Coldsteel Heart cannot produce mana while tapped from entering")
    void cannotActivateWhileTapped() {
        harness.castFromHand(player1, new ColdsteelHeart(), "{2}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }
}
