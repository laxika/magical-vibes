package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShimmeringGrottoTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Shimmering Grotto has two activated abilities")
    void hasCorrectProperties() {
        ShimmeringGrotto card = new ShimmeringGrotto();

        assertThat(card.getActivatedAbilities()).hasSize(2);
    }

    @Test
    @DisplayName("First ability taps for colorless mana with no mana cost")
    void firstAbilityProperties() {
        ShimmeringGrotto card = new ShimmeringGrotto();

        var ability = card.getActivatedAbilities().get(0);
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().get(0)).isInstanceOf(AwardManaEffect.class);
        assertThat(((AwardManaEffect) ability.getEffects().get(0)).color()).isEqualTo(ManaColor.COLORLESS);
    }

    @Test
    @DisplayName("Second ability costs {1} and taps to add one mana of any color")
    void secondAbilityProperties() {
        ShimmeringGrotto card = new ShimmeringGrotto();

        var ability = card.getActivatedAbilities().get(1);
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isEqualTo("{1}");
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().get(0)).isInstanceOf(AwardAnyColorManaEffect.class);
    }

    // ===== Tapping for colorless mana =====

    @Test
    @DisplayName("Tapping for colorless adds {C} immediately (mana ability)")
    void tapForColorlessAddsMana() {
        Permanent grotto = addReadyGrotto(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(grotto.isTapped()).isTrue();
        // Mana ability — does not use the stack
        assertThat(gd.stack).isEmpty();
    }

    // ===== Tapping for any color mana =====

    @Test
    @DisplayName("Activating second ability with {1} cost prompts for color choice")
    void secondAbilityPromptsForColorChoice() {
        addReadyGrotto(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        GameData gd = harness.getGameData();
        // Should be awaiting color choice (mana ability resolves immediately)
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.COLOR_CHOICE);
        assertThat(gd.interaction.colorChoice().playerId()).isEqualTo(player1.getId());
        // Mana ability — does not use the stack
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Choosing a color adds that mana to pool")
    void choosingColorAddsMana() {
        addReadyGrotto(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "RED");

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.interaction.awaitingInputType()).isNull();
    }

    @Test
    @DisplayName("Second ability spends {1} from the mana pool")
    void secondAbilitySpendsMana() {
        addReadyGrotto(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        GameData gd = harness.getGameData();
        // The {1} cost should have been paid from the white mana
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    // ===== Validation =====

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent grotto = addReadyGrotto(player1);
        grotto.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot activate second ability without enough mana")
    void cannotActivateSecondAbilityWithoutMana() {
        addReadyGrotto(player1);

        // No mana in pool — activation should fail
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private Permanent addReadyGrotto(Player player) {
        ShimmeringGrotto card = new ShimmeringGrotto();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
