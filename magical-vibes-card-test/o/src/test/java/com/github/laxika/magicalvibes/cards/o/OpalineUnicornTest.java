package com.github.laxika.magicalvibes.cards.o;

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

@CardUsed({OpalineUnicorn.class})
class OpalineUnicornTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot activate Opaline Unicorn while it has summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new OpalineUnicorn());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Activating Opaline Unicorn taps it and prompts for mana color")
    void activateAbilityPromptsManaColor() {
        Permanent unicorn = addCreatureReady(player1, new OpalineUnicorn());

        harness.activateAbility(player1, 0, null, null);

        assertThat(unicorn.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("Choosing a color adds exactly one mana of that color")
    void choosingColorAddsMana(ManaColor manaColor) {
        Permanent unicorn = addCreatureReady(player1, new OpalineUnicorn());
        int before = gd.playerManaPools.get(player1.getId()).get(manaColor);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, manaColor.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor)).isEqualTo(before + 1);
        assertThat(unicorn.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot activate Opaline Unicorn when it is already tapped")
    void cannotActivateWhileTapped() {
        addCreatureReady(player1, new OpalineUnicorn());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Mana goes to the Unicorn's controller rather than the active player")
    void opponentProducesManaForTheirOwnPool() {
        Permanent unicorn = addCreatureReady(player2, new OpalineUnicorn());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player2, 0, null, null);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleListChoice(player2, "BLUE");

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(unicorn.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
