package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PainsmithTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Painsmith has MayEffect wrapping SpellCastTriggerEffect with boost and deathtouch")
    void hasCorrectStructure() {
        Painsmith card = new Painsmith();

        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect mayEffect = (MayEffect) card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst();
        assertThat(mayEffect.wrapped()).isInstanceOf(SpellCastTriggerEffect.class);
        SpellCastTriggerEffect trigger = (SpellCastTriggerEffect) mayEffect.wrapped();
        assertThat(trigger.resolvedEffects()).hasSize(2);
        assertThat(trigger.resolvedEffects().getFirst()).isInstanceOf(BoostTargetCreatureEffect.class);
    }

    // ===== Trigger fires on artifact cast =====

    @Test
    @DisplayName("Casting an artifact spell triggers may ability prompt")
    void artifactCastTriggersMayPrompt() {
        harness.addToBattlefield(player1, new Painsmith());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
    }

    // ===== Accept: target creature gets +2/+0 and deathtouch =====

    @Test
    @DisplayName("Accepting gives target creature +2/+0 and deathtouch until end of turn")
    void acceptBoostsAndGrantsDeathtouch() {
        harness.addToBattlefield(player1, new Painsmith());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new Spellbook()));

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
                && e.getCard().getName().equals("Painsmith"));

        // Resolve triggered ability
        harness.passBothPriorities();

        // Grizzly Bears should have +2/+0 and deathtouch
        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(bears.getPowerModifier()).isEqualTo(2);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
        assertThat(bears.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
    }

    // ===== Decline =====

    @Test
    @DisplayName("Declining may ability does not boost or grant deathtouch")
    void declineDoesNothing() {
        harness.addToBattlefield(player1, new Painsmith());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        // No triggered ability on stack
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Painsmith"));

        // Grizzly Bears should not be boosted
        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.hasKeyword(Keyword.DEATHTOUCH)).isFalse();
    }

    // ===== Non-artifact does not trigger =====

    @Test
    @DisplayName("Non-artifact spell does not trigger Painsmith")
    void nonArtifactDoesNotTrigger() {
        harness.addToBattlefield(player1, new Painsmith());
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
    @DisplayName("Opponent casting artifact does not trigger Painsmith")
    void opponentArtifactDoesNotTrigger() {
        harness.addToBattlefield(player1, new Painsmith());

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

    // ===== Boost and deathtouch reset at end of turn =====

    @Test
    @DisplayName("Boost and deathtouch reset at end of turn")
    void boostResetsAtEndOfTurn() {
        harness.addToBattlefield(player1, new Painsmith());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(bears.getPowerModifier()).isEqualTo(2);
        assertThat(bears.hasKeyword(Keyword.DEATHTOUCH)).isTrue();

        // Advance to cleanup step (end of turn resets modifiers)
        harness.forceStep(TurnStep.CLEANUP);
        bears.resetModifiers();

        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.hasKeyword(Keyword.DEATHTOUCH)).isFalse();
    }

    // ===== Can target opponent's creature =====

    @Test
    @DisplayName("Can target opponent's creature with the boost")
    void canTargetOpponentCreature() {
        harness.addToBattlefield(player1, new Painsmith());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(bears.getPowerModifier()).isEqualTo(2);
        assertThat(bears.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
    }
}
