package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessReturnOwnPermanentTypeToHandEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GlintHawkTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has ETB sacrifice-unless-return-artifact effect")
    void hasCorrectEffect() {
        GlintHawk card = new GlintHawk();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(SacrificeUnlessReturnOwnPermanentTypeToHandEffect.class);
        SacrificeUnlessReturnOwnPermanentTypeToHandEffect effect =
                (SacrificeUnlessReturnOwnPermanentTypeToHandEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.permanentType()).isEqualTo(CardType.ARTIFACT);
    }

    // ===== No artifacts — auto-sacrifice =====

    @Test
    @DisplayName("Auto-sacrifices when controller has no artifacts")
    void autoSacrificesWithNoArtifacts() {
        harness.setHand(player1, List.of(new GlintHawk()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → ETB on stack
        harness.passBothPriorities(); // resolve ETB → auto-sacrifice

        // Glint Hawk is NOT on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Glint Hawk"));

        // Glint Hawk is in the graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Glint Hawk"));

        // No prompt — it was automatic
        assertThat(gd.interaction.awaitingInputType()).isNull();
    }

    // ===== With artifact — accept bounce =====

    @Test
    @DisplayName("ETB with artifact on battlefield prompts may ability choice")
    void etbWithArtifactPromptsMayAbility() {
        harness.addToBattlefield(player1, new Spellbook());
        harness.setHand(player1, List.of(new GlintHawk()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → ETB on stack
        harness.passBothPriorities(); // resolve ETB → may ability prompt

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting may ability prompts for permanent choice")
    void acceptingMayAbilityPromptsPermanentChoice() {
        castGlintHawkWithArtifact();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
    }

    @Test
    @DisplayName("Choosing artifact bounces it and keeps Glint Hawk")
    void choosingArtifactBouncesItAndKeepsGlintHawk() {
        castGlintHawkWithArtifact();

        harness.handleMayAbilityChosen(player1, true);

        UUID artifactId = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Spellbook"))
                .findFirst().orElseThrow().getId();

        harness.handlePermanentChosen(player1, artifactId);

        // Glint Hawk is still on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Glint Hawk"));

        // Spellbook is no longer on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Spellbook"));

        // Spellbook is back in hand
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Spellbook"));
    }

    // ===== With artifact — decline bounce =====

    @Test
    @DisplayName("Declining may ability sacrifices Glint Hawk and keeps artifact")
    void decliningMayAbilitySacrificesGlintHawk() {
        castGlintHawkWithArtifact();

        harness.handleMayAbilityChosen(player1, false);

        // Glint Hawk is NOT on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Glint Hawk"));

        // Glint Hawk is in the graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Glint Hawk"));

        // Spellbook is still on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Spellbook"));
    }

    // ===== Multiple artifacts — only chosen one is bounced =====

    @Test
    @DisplayName("With multiple artifacts, only the chosen one is returned")
    void onlyChosenArtifactIsBounced() {
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.setHand(player1, List.of(new GlintHawk()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB
        harness.handleMayAbilityChosen(player1, true);

        UUID scimitarId = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Leonin Scimitar"))
                .findFirst().orElseThrow().getId();

        harness.handlePermanentChosen(player1, scimitarId);

        // Glint Hawk is on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Glint Hawk"));

        // Spellbook is still on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Spellbook"));

        // Leonin Scimitar was returned to hand
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Leonin Scimitar"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Leonin Scimitar"));
    }

    // ===== Opponent's artifacts don't count =====

    @Test
    @DisplayName("Opponent's artifacts don't satisfy the requirement")
    void opponentArtifactsDontCount() {
        harness.addToBattlefield(player2, new Spellbook());
        harness.setHand(player1, List.of(new GlintHawk()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB → auto-sacrifice

        // Auto-sacrificed — no prompt
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Glint Hawk"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Glint Hawk"));

        // Opponent's Spellbook is untouched
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Spellbook"));
    }

    // ===== Helpers =====

    private void castGlintHawkWithArtifact() {
        harness.addToBattlefield(player1, new Spellbook());
        harness.setHand(player1, List.of(new GlintHawk()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → ETB on stack
        harness.passBothPriorities(); // resolve ETB → may ability prompt

        // Sanity check
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
    }
}
