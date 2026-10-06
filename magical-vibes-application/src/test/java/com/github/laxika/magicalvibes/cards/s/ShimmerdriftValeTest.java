package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShimmerdriftVale.class})
class ShimmerdriftValeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and stores the chosen color")
    void entersTappedAndStoresChosenColor() {
        harness.setHand(player1, List.of(new ShimmerdriftVale()));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        Permanent vale = findPermanent(player1, "Shimmerdrift Vale");
        assertThat(vale.isTapped()).isTrue();
        assertThat(vale.getChosenColor()).isEqualTo(CardColor.BLUE);
    }

    @Test
    @DisplayName("Tapping adds one mana of the chosen color")
    void tappingAddsChosenColorMana() {
        Permanent vale = addReadyVale(player1, CardColor.GREEN);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(vale.isTapped()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(CardColor.class)
    @DisplayName("Each chosen color produces immediate snow mana and remains fixed")
    void eachChosenColorProducesImmediateSnowMana(CardColor color) {
        harness.setHand(player1, List.of(new ShimmerdriftVale()));
        harness.playLand(player1, 0);
        harness.handleListChoice(player1, color.name());
        Permanent vale = findPermanent(player1, "Shimmerdrift Vale");
        harness.performUntapStep(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        ManaColor manaColor = ManaColor.valueOf(color.name());
        assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getSnowMana(manaColor)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(vale.isTapped()).isTrue();
        for (ManaColor other : ManaColor.values()) {
            if (other != manaColor) {
                assertThat(gd.playerManaPools.get(player1.getId()).get(other)).isZero();
            }
        }

        harness.performUntapStep(player1);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor)).isEqualTo(2);
        assertThat(vale.getChosenColor()).isEqualTo(color);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The land cannot produce mana while still tapped from entering")
    void cannotActivateWhileTapped() {
        harness.setHand(player1, List.of(new ShimmerdriftVale()));
        harness.playLand(player1, 0);
        harness.handleListChoice(player1, "RED");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Colorless is not a legal color choice")
    void cannotChooseColorless() {
        harness.setHand(player1, List.of(new ShimmerdriftVale()));
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.handleListChoice(player1, "COLORLESS"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "BLACK");
        assertThat(findPermanent(player1, "Shimmerdrift Vale").getChosenColor()).isEqualTo(CardColor.BLACK);
    }

    private Permanent addReadyVale(Player player, CardColor chosenColor) {
        Permanent vale = harness.addToBattlefieldAndReturn(player, new ShimmerdriftVale());
        vale.setSummoningSick(false);
        vale.setChosenColor(chosenColor);
        return vale;
    }
}
