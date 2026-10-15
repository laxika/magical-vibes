package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnknownShores.class})
class UnknownShoresTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for colorless adds {C} immediately (mana ability)")
    void tapForColorlessAddsMana() {
        Permanent shores = addReadyShores(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(shores.isTapped()).isTrue();
        // Mana ability — does not use the stack
        assertThat(gd.stack).isEmpty();
    }


    @Test
    @DisplayName("Activating second ability with {1} cost prompts for color choice")
    void secondAbilityPromptsForColorChoice() {
        addReadyShores(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        GameData gd = harness.getGameData();
        // Should be awaiting color choice (mana ability resolves immediately)
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId()).isEqualTo(player1.getId());
        // Mana ability — does not use the stack
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Choosing a color adds that mana to pool")
    void choosingColorAddsMana() {
        addReadyShores(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "RED");

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Second ability spends {1} from the mana pool")
    void secondAbilitySpendsMana() {
        addReadyShores(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        GameData gd = harness.getGameData();
        // The {1} cost should have been paid from the white mana
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }


    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent shores = addReadyShores(player1);
        shores.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot activate second ability without enough mana")
    void cannotActivateSecondAbilityWithoutMana() {
        Permanent shores = addReadyShores(player1);

        // No mana in pool — activation should fail
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(shores.isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }


    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("Filtering produces exactly one mana of any of the five colors")
    void filtersIntoEachColor(ManaColor color) {
        Permanent shores = harness.addToBattlefieldAndReturn(player1, new UnknownShores());
        shores.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(shores.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder("WHITE", "BLUE", "BLACK", "RED", "GREEN");
        harness.handleListChoice(player1, color.name());

        for (ManaColor manaColor : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor))
                    .isEqualTo(manaColor == color ? 1 : 0);
        }
        assertThat(gd.playerManaPools.get(player2.getId()).get(color)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A land can tap for colorless on the turn it enters")
    void canTapForColorlessWhileSummoningSick() {
        Permanent shores = harness.addToBattlefieldAndReturn(player1, new UnknownShores());
        shores.setSummoningSick(true);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(shores.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped land cannot filter mana even when its mana cost is available")
    void cannotFilterWhenTapped() {
        Permanent shores = addReadyShores(player1);
        shores.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyShores(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new UnknownShores());
        perm.setSummoningSick(false);
        return perm;
    }
}
