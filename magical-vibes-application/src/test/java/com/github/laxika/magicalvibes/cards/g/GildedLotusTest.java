package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GildedLotus.class})
class GildedLotusTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Gilded Lotus prompts for mana color")
    void activateAbilityPromptsManaColor() {
        Permanent lotus = harness.addToBattlefieldAndReturn(player1, new GildedLotus());

        harness.activateAbility(player1, 0, null, null);

        assertThat(lotus.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.options()).containsExactly("WHITE", "BLUE", "BLACK", "RED", "GREEN");
    }

    @ParameterizedTest(name = "Choosing {0} adds exactly three mana of that color")
    @EnumSource(value = ManaColor.class, mode = EnumSource.Mode.EXCLUDE, names = "COLORLESS")
    @DisplayName("Choosing a color adds exactly three mana of that color")
    void choosingColorAddsThreeMana(ManaColor manaColor) {
        Permanent lotus = harness.addToBattlefieldAndReturn(player1, new GildedLotus());
        var manaPool = gd.playerManaPools.get(player1.getId());
        int before = manaPool.get(manaColor);
        int totalBefore = manaPool.getTotalAllMana();

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, manaColor.name());

        assertThat(manaPool.get(manaColor)).isEqualTo(before + 3);
        assertThat(manaPool.getTotalAllMana()).isEqualTo(totalBefore + 3);
        assertThat(lotus.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot choose colorless mana")
    void cannotChooseColorlessMana() {
        harness.addToBattlefield(player1, new GildedLotus());

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.handleListChoice(player1, ManaColor.COLORLESS.name()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Cannot activate Gilded Lotus when already tapped")
    void cannotActivateWhileTapped() {
        harness.addToBattlefield(player1, new GildedLotus());

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }
}
