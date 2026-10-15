package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.model.GameData;
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

@CardUsed({ShimmeringGrotto.class})
class ShimmeringGrottoTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for colorless adds {C} immediately (mana ability)")
    void tapForColorlessAddsMana() {
        Permanent grotto = harness.addToBattlefieldAndReturn(player1, new ShimmeringGrotto());

        harness.activateAbility(player1, 0, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(grotto.isTapped()).isTrue();
        // Mana ability — does not use the stack
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Activating second ability with {1} cost prompts for color choice")
    void secondAbilityPromptsForColorChoice() {
        harness.addToBattlefield(player1, new ShimmeringGrotto());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        GameData gd = harness.getGameData();
        // Should be awaiting color choice (mana ability resolves immediately)
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId()).isEqualTo(player1.getId());
        // Mana ability — does not use the stack
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("Choosing any of the five colors adds exactly one mana and taps the land")
    void choosingColorAddsMana(ManaColor color) {
        Permanent grotto = harness.addToBattlefieldAndReturn(player1, new ShimmeringGrotto());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, color.name());

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(1);
        assertThat(grotto.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Second ability spends {1} from the mana pool")
    void secondAbilitySpendsMana() {
        harness.addToBattlefield(player1, new ShimmeringGrotto());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        GameData gd = harness.getGameData();
        // The {1} cost should have been paid from the white mana
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent grotto = harness.addToBattlefieldAndReturn(player1, new ShimmeringGrotto());
        grotto.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot activate second ability without enough mana")
    void cannotActivateSecondAbilityWithoutMana() {
        Permanent grotto = harness.addToBattlefieldAndReturn(player1, new ShimmeringGrotto());

        // No mana in pool — activation should fail
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(grotto.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Tapped Grotto cannot filter mana and does not spend the attempted cost")
    void cannotFilterManaWhenTapped() {
        Permanent grotto = harness.addToBattlefieldAndReturn(player1, new ShimmeringGrotto());
        grotto.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Grotto cannot use its own colorless mana to filter while it remains tapped")
    void cannotPayFilterCostByTappingSameGrotto() {
        harness.addToBattlefield(player1, new ShimmeringGrotto());
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }
}
