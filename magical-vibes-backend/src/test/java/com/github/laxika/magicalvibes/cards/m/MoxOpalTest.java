package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoxOpalTest extends BaseCardTest {

    @Test
    @DisplayName("Has tap activated ability with metalcraft restriction and any-color mana effect")
    void hasCorrectAbility() {
        MoxOpal card = new MoxOpal();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        ActivatedAbility ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(AwardAnyColorManaEffect.class);
        assertThat(ability.getTimingRestriction()).isEqualTo(ActivationTimingRestriction.METALCRAFT);
    }

    @Test
    @DisplayName("Cannot activate without three artifacts on the battlefield")
    void cannotActivateWithoutThreeArtifacts() {
        harness.addToBattlefield(player1, new MoxOpal());
        harness.addToBattlefield(player1, new Spellbook());

        // Only 2 artifacts (Mox Opal + Spellbook) — metalcraft not met
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Metalcraft");
    }

    @Test
    @DisplayName("Can activate with three artifacts and prompts for color choice")
    void canActivateWithThreeArtifacts() {
        harness.addToBattlefield(player1, new MoxOpal());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        // 3 artifacts (Mox Opal + Spellbook + Leonin Scimitar) — metalcraft met
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.COLOR_CHOICE);
        assertThat(gd.interaction.awaitingColorChoicePlayerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Choosing a color adds one mana of that color")
    void choosingColorAddsMana() {
        for (String color : List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN")) {
            harness = new GameTestHarness();
            player1 = harness.getPlayer1();
            gd = harness.getGameData();
            harness.skipMulligan();

            harness.addToBattlefield(player1, new MoxOpal());
            harness.addToBattlefield(player1, new Spellbook());
            harness.addToBattlefield(player1, new LeoninScimitar());

            ManaColor manaColor = ManaColor.valueOf(color);

            harness.activateAbility(player1, 0, null, null);
            int before = gd.playerManaPools.get(player1.getId()).get(manaColor);

            harness.handleColorChosen(player1, color);

            assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor)).isEqualTo(before + 1);
            assertThat(gd.interaction.awaitingInputType()).isNull();
        }
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhileTapped() {
        harness.addToBattlefield(player1, new MoxOpal());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }
}
