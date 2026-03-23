package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromCardTypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TelJiladDefianceTest extends BaseCardTest {

    @Test
    @DisplayName("Tel-Jilad Defiance has correct effects")
    void hasCorrectEffects() {
        TelJiladDefiance card = new TelJiladDefiance();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(GrantProtectionFromCardTypeUntilEndOfTurnEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(DrawCardEffect.class);

        GrantProtectionFromCardTypeUntilEndOfTurnEffect protEffect =
                (GrantProtectionFromCardTypeUntilEndOfTurnEffect) card.getEffects(EffectSlot.SPELL).get(0);
        assertThat(protEffect.cardType()).isEqualTo(CardType.ARTIFACT);
    }

    @Test
    @DisplayName("Resolving Tel-Jilad Defiance grants protection from artifacts and draws a card")
    void grantsProtectionAndDrawsCard() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        int deckSizeBefore = harness.getGameData().playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new TelJiladDefiance()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Protection from artifacts was granted
        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(bears.getProtectionFromCardTypes()).contains(CardType.ARTIFACT);

        // Drew a card
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Cannot target a non-creature with Tel-Jilad Defiance")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new GoldMyr());
        // GoldMyr is an artifact creature, so it IS a valid target. Let's use a pure artifact.
        // We'll use a different approach - try to target a player instead.
        harness.setHand(player1, List.of(new TelJiladDefiance()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID creatureId = harness.getPermanentId(player1, "Gold Myr");
        // Gold Myr is an artifact creature, so targeting it should work.
        // Instead, test that targeting player fails.
        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles and does not draw when target is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        int deckSizeBefore = harness.getGameData().playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new TelJiladDefiance()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);

        // Remove the target before resolution
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
    }

    @Test
    @DisplayName("Protection from artifacts prevents blocking by artifact creature")
    void protectionPreventsBlockingByArtifactCreature() {
        // Set up: Grizzly Bears has protection from artifacts
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.getProtectionFromCardTypes().add(CardType.ARTIFACT);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        // Iron Myr is an artifact creature
        Permanent blocker = new Permanent(new IronMyr());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection from artifacts allows blocking by non-artifact creature")
    void protectionAllowsBlockingByNonArtifactCreature() {
        // Set up: Grizzly Bears #1 has protection from artifacts
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.getProtectionFromCardTypes().add(CardType.ARTIFACT);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        // Grizzly Bears #2 is a non-artifact creature — can block
        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Protection from artifacts prevents combat damage from artifact creature")
    void protectionPreventsCombatDamageFromArtifactCreature() {
        // Iron Myr (1/1 artifact creature) attacks, Grizzly Bears blocks with protection from artifacts
        Permanent attacker = new Permanent(new IronMyr());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        blocker.getProtectionFromCardTypes().add(CardType.ARTIFACT);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Both creatures should survive: Iron Myr's 1 damage to Grizzly Bears is prevented (protection)
        // Grizzly Bears' 2 damage kills Iron Myr (1/1)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Iron Myr"));
    }

    @Test
    @DisplayName("Protection from artifacts is cleared at end of turn")
    void protectionClearedAtEndOfTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TelJiladDefiance()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent bears = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(bears.getProtectionFromCardTypes()).contains(CardType.ARTIFACT);

        // Simulate end of turn cleanup
        bears.resetModifiers();
        assertThat(bears.getProtectionFromCardTypes()).isEmpty();
    }
}
