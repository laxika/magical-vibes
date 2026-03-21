package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LlanowarEnvoyTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Llanowar Envoy has one activated ability that costs {1}{G} and does not tap")
    void hasCorrectAbility() {
        LlanowarEnvoy card = new LlanowarEnvoy();

        assertThat(card.getActivatedAbilities()).hasSize(1);

        var ability = card.getActivatedAbilities().get(0);
        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.getManaCost()).isEqualTo("{1}{G}");
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().get(0)).isInstanceOf(AwardAnyColorManaEffect.class);
    }

    // ===== Mana production =====

    @Test
    @DisplayName("Activating ability prompts for color choice")
    void activatingAbilityPromptsForColorChoice() {
        addReadyEnvoy(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.COLOR_CHOICE);
        assertThat(gd.interaction.colorChoice().playerId()).isEqualTo(player1.getId());
        // Mana ability — does not use the stack
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Choosing a color adds that mana to pool")
    void choosingColorAddsMana() {
        addReadyEnvoy(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.interaction.awaitingInputType()).isNull();
    }

    @Test
    @DisplayName("Activating ability spends {1}{G} from the mana pool")
    void activatingAbilitySpendsMana() {
        addReadyEnvoy(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        GameData gd = harness.getGameData();
        // The {G} portion should have been paid from green mana
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        // The {1} portion should have been paid from white mana
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("Can activate ability multiple times if enough mana is available")
    void canActivateMultipleTimes() {
        addReadyEnvoy(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    // ===== Validation =====

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyEnvoy(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private Permanent addReadyEnvoy(Player player) {
        LlanowarEnvoy card = new LlanowarEnvoy();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
