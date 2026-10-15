package com.github.laxika.magicalvibes.cards.a;

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

@CardUsed({AlloyMyr.class})
class AlloyMyrTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Alloy Myr prompts for mana color")
    void activateAbilityPromptsManaColor() {
        Permanent myr = addCreatureReady(player1, new AlloyMyr());

        harness.activateAbility(player1, 0, null, null);

        assertThat(myr.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder("WHITE", "BLUE", "BLACK", "RED", "GREEN");
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = "COLORLESS", mode = EnumSource.Mode.EXCLUDE)
    @DisplayName("Choosing a color adds exactly one mana of that color")
    void choosingColorAddsMana(ManaColor manaColor) {
        addCreatureReady(player1, new AlloyMyr());

        harness.activateAbility(player1, 0, null, null);
        int before = gd.playerManaPools.get(player1.getId()).get(manaColor);

        harness.handleListChoice(player1, manaColor.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor)).isEqualTo(before + 1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot activate Alloy Myr with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new AlloyMyr());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Cannot activate Alloy Myr when already tapped")
    void cannotActivateWhileTapped() {
        addCreatureReady(player1, new AlloyMyr());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }
}
