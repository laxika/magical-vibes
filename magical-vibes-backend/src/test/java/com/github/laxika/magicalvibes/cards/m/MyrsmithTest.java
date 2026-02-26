package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CreateTokenOnOwnSpellCastWithCostEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MyrsmithTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Myrsmith has MayEffect wrapping CreateTokenOnOwnSpellCastWithCostEffect")
    void hasCorrectStructure() {
        Myrsmith card = new Myrsmith();

        assertThat(card.getEffects(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect mayEffect = (MayEffect) card.getEffects(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL).getFirst();
        assertThat(mayEffect.wrapped()).isInstanceOf(CreateTokenOnOwnSpellCastWithCostEffect.class);
        CreateTokenOnOwnSpellCastWithCostEffect trigger =
                (CreateTokenOnOwnSpellCastWithCostEffect) mayEffect.wrapped();
        assertThat(trigger.manaCost()).isEqualTo(1);
        assertThat(trigger.tokenEffect().tokenName()).isEqualTo("Myr");
        assertThat(trigger.tokenEffect().power()).isEqualTo(1);
        assertThat(trigger.tokenEffect().toughness()).isEqualTo(1);
        assertThat(trigger.tokenEffect().additionalTypes()).contains(CardType.ARTIFACT);
    }

    // ===== Trigger fires on artifact cast =====

    @Test
    @DisplayName("Casting an artifact spell triggers may ability prompt")
    void artifactCastTriggersMayPrompt() {
        harness.addToBattlefield(player1, new Myrsmith());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
    }

    // ===== Accept, pay, create token =====

    @Test
    @DisplayName("Accepting pays {1} and creates a 1/1 Myr artifact creature token")
    void acceptPaysAndCreatesToken() {
        harness.addToBattlefield(player1, new Myrsmith());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);

        // Accept the may ability
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();

        // Triggered ability should be on the stack
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Myrsmith"));

        // Resolve triggered ability
        harness.passBothPriorities();

        // A Myr token should be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Myr")
                        && p.getCard().isToken()
                        && p.getCard().getType() == CardType.CREATURE
                        && p.getCard().getAdditionalTypes().contains(CardType.ARTIFACT)
                        && p.getCard().getPower() == 1
                        && p.getCard().getToughness() == 1);

        // Mana should have been spent
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
    }

    // ===== Decline =====

    @Test
    @DisplayName("Declining may ability does not create token or spend mana")
    void declineDoesNothing() {
        harness.addToBattlefield(player1, new Myrsmith());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        // No triggered ability on stack
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Myrsmith"));

        // No Myr token created
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Myr"));

        // Mana not spent
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    // ===== Non-artifact does not trigger =====

    @Test
    @DisplayName("Non-artifact spell does not trigger Myrsmith")
    void nonArtifactDoesNotTrigger() {
        harness.addToBattlefield(player1, new Myrsmith());
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
    @DisplayName("Opponent casting artifact does not trigger Myrsmith")
    void opponentArtifactDoesNotTrigger() {
        harness.addToBattlefield(player1, new Myrsmith());

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
        harness.addToBattlefield(player1, new Myrsmith());
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
                && e.getCard().getName().equals("Myrsmith"));

        // No token created
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Myr"));
    }
}
