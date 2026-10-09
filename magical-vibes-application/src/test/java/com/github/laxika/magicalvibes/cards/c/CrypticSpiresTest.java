package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrypticSpires.class})
class CrypticSpiresTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new CrypticSpires(List.of(ManaColor.WHITE, ManaColor.BLUE))));

        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate its mana ability immediately after entering tapped")
    void cannotActivateOnEnteringTapped() {
        harness.setHand(player1, List.of(new CrypticSpires(List.of(ManaColor.WHITE, ManaColor.BLUE))));
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Permanent is already tapped");
        for (ManaColor color : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isZero();
        }
    }

    @Test
    @DisplayName("Adds one mana of a circled color")
    void addsManaOfCircledColor() {
        harness.addToBattlefield(player1, new CrypticSpires(List.of(ManaColor.WHITE, ManaColor.BLUE)));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        var choice = (PendingInteraction.ColorChoice) gd.interaction.activeInteraction();
        ManaColor chosenColor = ManaColor.valueOf(choice.options().getFirst());
        harness.handleListChoice(player1, chosenColor.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(chosenColor)).isEqualTo(1);
        for (ManaColor color : ManaColor.values()) {
            if (color != chosenColor) {
                assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isZero();
            }
        }
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Mana choice is restricted to the two colors circled during deck construction")
    void manaChoiceOffersOnlyTwoColors() {
        harness.addToBattlefield(player1, new CrypticSpires(List.of(ManaColor.WHITE, ManaColor.BLUE)));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        var choice = (PendingInteraction.ColorChoice) gd.interaction.activeInteraction();
        assertThat(choice.options()).containsExactly(ManaColor.WHITE.name(), ManaColor.BLUE.name())
                .doesNotContain(ManaColor.COLORLESS.name());
    }
}
