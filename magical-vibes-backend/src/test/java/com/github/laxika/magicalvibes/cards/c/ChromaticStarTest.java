package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChromaticStarTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Chromatic Star has correct card properties")
    void hasCorrectProperties() {
        ChromaticStar card = new ChromaticStar();

        assertThat(card.getName()).isEqualTo("Chromatic Star");
        assertThat(card.getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(card.getManaCost()).isEqualTo("{1}");
        assertThat(card.getActivatedAbilities()).hasSize(1);

        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isEqualTo("{1}");
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(SacrificeSelfCost.class);
        assertThat(ability.getEffects().get(1)).isInstanceOf(AwardAnyColorManaEffect.class);

        assertThat(card.getEffects(EffectSlot.ON_DEATH)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_DEATH).getFirst()).isInstanceOf(DrawCardEffect.class);
    }

    // ===== Mana ability resolves immediately (CR 605.3a) =====

    @Test
    @DisplayName("Activating Chromatic Star sacrifices it and immediately prompts for mana color (mana ability)")
    void activateAbilityPromptsManaColorImmediately() {
        harness.addToBattlefield(player1, new ChromaticStar());
        harness.addMana(player1, ManaColor.WHITE, 1);

        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, null, null);

        // The permanent should be sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Chromatic Star"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Chromatic Star"));

        // Mana ability resolves immediately — no stack entry for the mana,
        // only the death trigger (draw card) should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Chromatic Star");

        // Should be immediately awaiting color choice (mana ability, no priority pass needed)
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.COLOR_CHOICE);
        assertThat(gd.interaction.awaitingColorChoicePlayerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Choosing a mana color adds mana to pool immediately")
    void resolveAbilityAddsChosenMana() {
        harness.addToBattlefield(player1, new ChromaticStar());
        harness.addMana(player1, ManaColor.WHITE, 1);

        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, null, null);

        // Mana pool should be empty (1 white was spent on {1} cost)
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();

        // Choose red mana
        harness.handleColorChosen(player1, "RED");

        // Red mana should have been added immediately
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.interaction.awaitingInputType()).isNull();
    }

    @Test
    @DisplayName("Choosing different mana colors works correctly")
    void chooseDifferentManaColors() {
        for (String color : List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN")) {
            harness = new GameTestHarness();
            player1 = harness.getPlayer1();
            player2 = harness.getPlayer2();
            harness.skipMulligan();

            harness.addToBattlefield(player1, new ChromaticStar());
            harness.addMana(player1, ManaColor.WHITE, 1);

            GameData gd = harness.getGameData();
            ManaColor manaColor = ManaColor.valueOf(color);

            harness.activateAbility(player1, 0, null, null);
            // Capture after activation cost is paid
            int manaBefore = gd.playerManaPools.get(player1.getId()).get(manaColor);
            harness.handleColorChosen(player1, color);

            assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor))
                    .isEqualTo(manaBefore + 1);
        }
    }

    // ===== Death trigger: draw a card =====

    @Test
    @DisplayName("Full sequence: mana added immediately, then draw trigger resolves via stack")
    void fullActivationSequenceDrawsCard() {
        harness.addToBattlefield(player1, new ChromaticStar());
        harness.addMana(player1, ManaColor.WHITE, 1);

        GameData gd = harness.getGameData();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        // Activate ability — mana ability resolves immediately, prompts for color
        harness.activateAbility(player1, 0, null, null);

        // Choose mana color — mana added immediately
        harness.handleColorChosen(player1, "GREEN");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);

        // The draw trigger should still be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        // Resolve the draw trigger
        harness.passBothPriorities();

        // Player should have drawn a card
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Chromatic Star cannot be activated without enough mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new ChromaticStar());

        GameData gd = harness.getGameData();

        // No mana in pool — activation should fail
        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () ->
                harness.activateAbility(player1, 0, null, null));

        // Chromatic Star should still be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Chromatic Star"));
    }
}


