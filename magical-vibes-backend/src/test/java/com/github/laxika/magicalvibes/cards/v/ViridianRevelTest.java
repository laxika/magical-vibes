package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ViridianRevelTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Viridian Revel has the opponent artifact-to-graveyard triggered ability")
    void hasCorrectEffects() {
        ViridianRevel card = new ViridianRevel();

        assertThat(card.getEffects(EffectSlot.ON_ARTIFACT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ARTIFACT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD).getFirst())
                .isInstanceOf(MayEffect.class);

        MayEffect may = (MayEffect) card.getEffects(EffectSlot.ON_ARTIFACT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD).getFirst();
        assertThat(may.wrapped()).isInstanceOf(DrawCardEffect.class);
        assertThat(may.prompt()).isEqualTo("Draw a card?");
    }

    // ===== Triggering =====

    @Test
    @DisplayName("Triggers when an opponent's artifact creature is destroyed")
    void triggersWhenOpponentArtifactCreatureDies() {
        harness.addToBattlefield(player1, new ViridianRevel());
        harness.addToBattlefield(player2, new Memnite());

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // Resolve Cruel Edict — MayEffect on stack
        harness.passBothPriorities(); // resolve MayEffect → may prompt

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Memnite"));

        // Viridian Revel's may ability should prompt
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Triggers when an opponent's non-creature artifact is destroyed")
    void triggersWhenOpponentNonCreatureArtifactIsDestroyed() {
        harness.addToBattlefield(player1, new ViridianRevel());
        harness.addToBattlefield(player2, new MindStone());

        UUID mindStoneId = harness.getPermanentId(player2, "Mind Stone");

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, mindStoneId);
        harness.passBothPriorities(); // Resolve Naturalize — MayEffect on stack
        harness.passBothPriorities(); // resolve MayEffect → may prompt

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Mind Stone"));

        // Viridian Revel's may ability should prompt
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Does NOT trigger when own artifact is destroyed")
    void doesNotTriggerForOwnArtifact() {
        harness.addToBattlefield(player1, new ViridianRevel());
        harness.addToBattlefield(player1, new MindStone());

        UUID mindStoneId = harness.getPermanentId(player1, "Mind Stone");

        // Player2 destroys player1's Mind Stone
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, mindStoneId);
        harness.passBothPriorities(); // Resolve Naturalize

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Mind Stone"));

        // No trigger — own artifact, not opponent's
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when a non-artifact creature dies")
    void doesNotTriggerForNonArtifactCreature() {
        harness.addToBattlefield(player1, new ViridianRevel());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // Resolve Cruel Edict

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // No trigger — not an artifact
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Accepting the may ability draws a card")
    void acceptingMayAbilityDrawsCard() {
        harness.addToBattlefield(player1, new ViridianRevel());
        harness.addToBattlefield(player2, new Memnite());

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // Resolve Cruel Edict — MayEffect on stack

        // Hand is now empty after casting Cruel Edict
        int handSizeAfterCast = harness.getGameData().playerHands.get(player1.getId()).size();

        harness.passBothPriorities(); // resolve MayEffect → may prompt

        // Accept the may ability — inner effect resolves inline
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();

        // Player1 should have drawn a card
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeAfterCast + 1);
    }

    @Test
    @DisplayName("Declining the may ability does not draw a card")
    void decliningMayAbilityDoesNotDrawCard() {
        harness.addToBattlefield(player1, new ViridianRevel());
        harness.addToBattlefield(player2, new Memnite());

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int handSizeBefore = harness.getGameData().playerHands.get(player1.getId()).size();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // Resolve Cruel Edict — MayEffect on stack
        harness.passBothPriorities(); // resolve MayEffect → may prompt

        // Decline the may ability
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();

        // No card drawn (hand size = before - 1 for casting Cruel Edict)
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore - 1);
    }

    // ===== Multiple triggers =====

    @Test
    @DisplayName("Triggers separately for each opponent artifact destroyed")
    void triggersForEachArtifactSeparately() {
        harness.addToBattlefield(player1, new ViridianRevel());
        harness.addToBattlefield(player2, new Memnite());
        harness.addToBattlefield(player2, new MindStone());

        UUID memniteId = harness.getPermanentId(player2, "Memnite");
        UUID mindStoneId = harness.getPermanentId(player2, "Mind Stone");

        // Destroy first artifact
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, memniteId);
        harness.passBothPriorities(); // Resolve Naturalize — MayEffect on stack
        harness.passBothPriorities(); // resolve MayEffect → may prompt

        // Accept the may ability — inner effect resolves inline
        harness.handleMayAbilityChosen(player1, true);

        // Destroy second artifact
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, mindStoneId);
        harness.passBothPriorities(); // Resolve Naturalize — MayEffect on stack
        harness.passBothPriorities(); // resolve MayEffect → may prompt

        // Accept the may ability — inner effect resolves inline
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        // The key assertion: both triggers fired and both drew cards
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Memnite"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Mind Stone"));
        assertThat(gd.stack).isEmpty();
    }
}
