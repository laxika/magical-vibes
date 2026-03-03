package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LifesmithTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Lifesmith has MayEffect wrapping SpellCastTriggerEffect with cost")
    void hasCorrectStructure() {
        Lifesmith card = new Lifesmith();

        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect mayEffect = (MayEffect) card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst();
        assertThat(mayEffect.wrapped()).isInstanceOf(SpellCastTriggerEffect.class);
        SpellCastTriggerEffect trigger = (SpellCastTriggerEffect) mayEffect.wrapped();
        assertThat(trigger.spellFilter()).isInstanceOf(CardTypePredicate.class);
        assertThat(((CardTypePredicate) trigger.spellFilter()).cardType()).isEqualTo(CardType.ARTIFACT);
        assertThat(trigger.manaCost()).isEqualTo("{1}");
        assertThat(trigger.resolvedEffects()).hasSize(1);
        assertThat(trigger.resolvedEffects().getFirst()).isInstanceOf(GainLifeEffect.class);
    }

    // ===== Trigger fires on artifact cast =====

    @Test
    @DisplayName("Casting an artifact spell triggers may ability prompt")
    void artifactCastTriggersMayPrompt() {
        harness.addToBattlefield(player1, new Lifesmith());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
    }

    // ===== Accept and pay gains life =====

    @Test
    @DisplayName("Accepting pays {1} and gains 3 life")
    void acceptPaysAndGainsLife() {
        harness.addToBattlefield(player1, new Lifesmith());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castArtifact(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        // Triggered ability should be on the stack
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Lifesmith"));

        // Resolve triggered ability
        harness.passBothPriorities();

        // Resolve Spellbook
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 3);

        // Mana should have been spent
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
    }

    // ===== Decline =====

    @Test
    @DisplayName("Declining may ability does not gain life or spend mana")
    void declineDoesNothing() {
        harness.addToBattlefield(player1, new Lifesmith());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castArtifact(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        // No triggered ability on stack
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Lifesmith"));

        // Mana not spent
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);

        // No life gained
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    // ===== Non-artifact does not trigger =====

    @Test
    @DisplayName("Non-artifact spell does not trigger Lifesmith")
    void nonArtifactDoesNotTrigger() {
        harness.addToBattlefield(player1, new Lifesmith());
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
    @DisplayName("Opponent casting artifact does not trigger Lifesmith")
    void opponentArtifactDoesNotTrigger() {
        harness.addToBattlefield(player1, new Lifesmith());

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
        harness.addToBattlefield(player1, new Lifesmith());
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
                && e.getCard().getName().equals("Lifesmith"));
    }
}
