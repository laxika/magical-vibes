package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EmbersmithTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Embersmith has MayEffect wrapping SpellCastTriggerEffect with cost and damage")
    void hasCorrectStructure() {
        Embersmith card = new Embersmith();

        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect mayEffect = (MayEffect) card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst();
        assertThat(mayEffect.wrapped()).isInstanceOf(SpellCastTriggerEffect.class);
        SpellCastTriggerEffect trigger = (SpellCastTriggerEffect) mayEffect.wrapped();
        assertThat(trigger.manaCost()).isEqualTo("{1}");
        assertThat(trigger.resolvedEffects()).hasSize(1);
        assertThat(trigger.resolvedEffects().getFirst()).isInstanceOf(DealDamageToAnyTargetEffect.class);
    }

    // ===== Trigger fires on artifact cast =====

    @Test
    @DisplayName("Casting an artifact spell triggers may ability prompt")
    void artifactCastTriggersMayPrompt() {
        harness.addToBattlefield(player1, new Embersmith());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
    }

    // ===== Accept, pay, target creature =====

    @Test
    @DisplayName("Accepting pays {1} and deals 1 damage to target creature, killing a 1/1")
    void acceptPaysDamageToCreature() {
        harness.addToBattlefield(player1, new Embersmith());
        harness.addToBattlefield(player2, new SuntailHawk());
        UUID targetId = harness.getPermanentId(player2, "Suntail Hawk");
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);

        // Accept the may ability
        harness.handleMayAbilityChosen(player1, true);

        // Should be prompting for target selection
        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

        // Choose the creature target
        harness.handlePermanentChosen(player1, targetId);

        // Triggered ability should be on the stack
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Embersmith"));

        // Resolve triggered ability
        harness.passBothPriorities();

        // Suntail Hawk (1/1) should be destroyed by 1 damage
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Suntail Hawk"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Suntail Hawk"));

        // Mana should have been spent
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
    }

    // ===== Accept, pay, target player =====

    @Test
    @DisplayName("Accepting pays {1} and deals 1 damage to target player")
    void acceptPaysDamageToPlayer() {
        harness.addToBattlefield(player1, new Embersmith());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castArtifact(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        // Choose the opponent as the target
        harness.handlePermanentChosen(player1, player2.getId());

        // Resolve triggered ability
        harness.passBothPriorities();

        // Resolve Spellbook
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    // ===== Decline =====

    @Test
    @DisplayName("Declining may ability does not deal damage or spend mana")
    void declineDoesNothing() {
        harness.addToBattlefield(player1, new Embersmith());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castArtifact(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        // No triggered ability on stack
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Embersmith"));

        // Mana not spent
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);

        // No damage dealt
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    // ===== Non-artifact does not trigger =====

    @Test
    @DisplayName("Non-artifact spell does not trigger Embersmith")
    void nonArtifactDoesNotTrigger() {
        harness.addToBattlefield(player1, new Embersmith());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isNull();
        // Stack should only have the creature spell
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    // ===== Opponent's artifact does not trigger =====

    @Test
    @DisplayName("Opponent casting artifact does not trigger Embersmith")
    void opponentArtifactDoesNotTrigger() {
        harness.addToBattlefield(player1, new Embersmith());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Spellbook()));

        harness.castArtifact(player2, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
    }

    // ===== Cannot pay =====

    @Test
    @DisplayName("Accepting with no mana treats as decline")
    void cannotPayTreatsAsDecline() {
        harness.addToBattlefield(player1, new Embersmith());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Spellbook()));
        // No mana added — cannot pay {1}

        harness.castArtifact(player1, 0);

        // May prompt fires
        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());

        // Accept, but cannot pay
        harness.handleMayAbilityChosen(player1, true);

        // No triggered ability on stack
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Embersmith"));
    }
}
