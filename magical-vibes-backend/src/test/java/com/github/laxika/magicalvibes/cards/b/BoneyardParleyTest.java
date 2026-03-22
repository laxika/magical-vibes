package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.ExileTargetGraveyardCardsAndSeparateIntoPilesEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BoneyardParleyTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Boneyard Parley has correct effects")
    void hasCorrectEffects() {
        BoneyardParley card = new BoneyardParley();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(ExileTargetGraveyardCardsAndSeparateIntoPilesEffect.class);
        ExileTargetGraveyardCardsAndSeparateIntoPilesEffect effect =
                (ExileTargetGraveyardCardsAndSeparateIntoPilesEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.filter()).isInstanceOf(CardTypePredicate.class);
        assertThat(((CardTypePredicate) effect.filter()).cardType()).isEqualTo(CardType.CREATURE);
        assertThat(effect.maxTargets()).isEqualTo(5);
    }

    // ===== Casting and targeting =====

    @Test
    @DisplayName("Casting with creature cards in graveyards prompts for target selection from all graveyards")
    void castingPromptsTargetSelectionFromAllGraveyards() {
        Card creature1 = new GrizzlyBears();
        Card creature2 = new LlanowarElves();
        harness.setGraveyard(player1, List.of(creature1));
        harness.setGraveyard(player2, List.of(creature2));
        harness.setHand(player1, List.of(new BoneyardParley()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_GRAVEYARD_CHOICE);
        // Both creatures from both graveyards should be valid targets
        assertThat(gd.interaction.multiSelection().multiGraveyardValidCardIds()).hasSize(2);
        assertThat(gd.interaction.multiSelection().multiGraveyardValidCardIds())
                .contains(creature1.getId(), creature2.getId());
        // Spell is NOT yet on the stack (waiting for target selection)
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Casting with no creature cards in any graveyard skips target prompt")
    void castingWithNoCreaturesSkipsPrompt() {
        harness.setGraveyard(player1, List.of(new LeoninScimitar()));
        harness.setHand(player1, List.of(new BoneyardParley()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Only creature cards appear as valid targets, not artifacts")
    void onlyCreatureCardsAreValidTargets() {
        Card creature = new GrizzlyBears();
        Card artifact = new LeoninScimitar();
        harness.setGraveyard(player1, List.of(creature, artifact));
        harness.setHand(player1, List.of(new BoneyardParley()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.interaction.multiSelection().multiGraveyardValidCardIds()).hasSize(1);
        assertThat(gd.interaction.multiSelection().multiGraveyardValidCardIds()).contains(creature.getId());
    }

    @Test
    @DisplayName("Max targets is capped at 5")
    void maxTargetsCappedAtFive() {
        List<Card> creatures = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            creatures.add(new GrizzlyBears());
        }
        harness.setGraveyard(player1, creatures);
        harness.setHand(player1, List.of(new BoneyardParley()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.interaction.multiSelection().multiGraveyardValidCardIds()).hasSize(6);
        assertThat(gd.interaction.multiSelection().multiGraveyardMaxCount()).isEqualTo(5);
    }

    // ===== Full resolution flow =====

    @Test
    @DisplayName("Full flow: opponent separates into piles, controller chooses pile for battlefield")
    void fullFlowControllerChoosesPile1() {
        Card bears1 = new GrizzlyBears();
        Card bears2 = new GrizzlyBears();
        Card elves = new LlanowarElves();
        harness.setGraveyard(player1, List.of(bears1, elves));
        harness.setGraveyard(player2, List.of(bears2));
        harness.setHand(player1, List.of(new BoneyardParley()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        // Step 1: Cast the spell
        harness.castSorcery(player1, 0, 0);

        // Step 2: Select all 3 creatures as targets
        List<UUID> allTargets = new ArrayList<>(gd.interaction.multiSelection().multiGraveyardValidCardIds());
        harness.handleMultipleGraveyardCardsChosen(player1, allTargets);

        // Spell should be on the stack
        assertThat(gd.stack).hasSize(1);

        // Step 3: Resolve the spell
        harness.passBothPriorities();

        // Cards should be exiled from graveyards
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears") || c.getName().equals("Llanowar Elves"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));

        // Opponent (player2) should be prompted to separate into piles
        assertThat(gd.pendingPileSeparation).isTrue();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_GRAVEYARD_CHOICE);

        // Step 4: Opponent puts bears1 in Pile 1, rest in Pile 2
        harness.handleMultipleGraveyardCardsChosen(player2, List.of(bears1.getId()));

        // Controller (player1) should be prompted to choose a pile
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);

        // Step 5: Controller chooses Pile 1 (bears1)
        harness.handleMayAbilityChosen(player1, true);

        // bears1 should be on the battlefield under player1's control
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears")
                        && p.getCard().getId().equals(bears1.getId()));

        // bears2 and elves should be back in their owners' graveyards
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears") && c.getId().equals(bears2.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));

        // Pending state should be cleaned up
        assertThat(gd.pendingPileSeparation).isFalse();
    }

    @Test
    @DisplayName("Controller chooses Pile 2")
    void fullFlowControllerChoosesPile2() {
        Card bears = new GrizzlyBears();
        Card elves = new LlanowarElves();
        harness.setGraveyard(player1, List.of(bears, elves));
        harness.setHand(player1, List.of(new BoneyardParley()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castSorcery(player1, 0, 0);
        List<UUID> targets = new ArrayList<>(gd.interaction.multiSelection().multiGraveyardValidCardIds());
        harness.handleMultipleGraveyardCardsChosen(player1, targets);
        harness.passBothPriorities();

        // Opponent puts bears in Pile 1, elves in Pile 2
        harness.handleMultipleGraveyardCardsChosen(player2, List.of(bears.getId()));

        // Controller chooses Pile 2 (elves)
        harness.handleMayAbilityChosen(player1, false);

        // Elves should be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Llanowar Elves"));

        // Bears should be back in owner's graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        assertThat(gd.pendingPileSeparation).isFalse();
    }

    @Test
    @DisplayName("Opponent puts all cards in one pile, leaving other pile empty")
    void opponentPutsAllInOnePile() {
        Card bears = new GrizzlyBears();
        Card elves = new LlanowarElves();
        harness.setGraveyard(player1, List.of(bears, elves));
        harness.setHand(player1, List.of(new BoneyardParley()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castSorcery(player1, 0, 0);
        List<UUID> targets = new ArrayList<>(gd.interaction.multiSelection().multiGraveyardValidCardIds());
        harness.handleMultipleGraveyardCardsChosen(player1, targets);
        harness.passBothPriorities();

        // Opponent puts everything in Pile 1 (Pile 2 is empty)
        harness.handleMultipleGraveyardCardsChosen(player2, List.of(bears.getId(), elves.getId()));

        // Controller chooses Pile 1 (both creatures)
        harness.handleMayAbilityChosen(player1, true);

        // Both should be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"))
                .anyMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
    }

    @Test
    @DisplayName("Opponent puts nothing in Pile 1, all in Pile 2")
    void opponentPutsNothingInPile1() {
        Card bears = new GrizzlyBears();
        Card elves = new LlanowarElves();
        harness.setGraveyard(player1, List.of(bears, elves));
        harness.setHand(player1, List.of(new BoneyardParley()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castSorcery(player1, 0, 0);
        List<UUID> targets = new ArrayList<>(gd.interaction.multiSelection().multiGraveyardValidCardIds());
        harness.handleMultipleGraveyardCardsChosen(player1, targets);
        harness.passBothPriorities();

        // Opponent puts nothing in Pile 1 (all in Pile 2)
        harness.handleMultipleGraveyardCardsChosen(player2, List.of());

        // Controller chooses Pile 2 (both creatures)
        harness.handleMayAbilityChosen(player1, false);

        // Both should be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"))
                .anyMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
    }

    @Test
    @DisplayName("Selecting one target out of many works correctly")
    void selectingOneTargetWorks() {
        Card bears = new GrizzlyBears();
        Card elves = new LlanowarElves();
        harness.setGraveyard(player1, List.of(bears, elves));
        harness.setHand(player1, List.of(new BoneyardParley()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castSorcery(player1, 0, 0);

        // Select only bears
        harness.handleMultipleGraveyardCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        // Only bears should be exiled; elves should still be in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));

        // Opponent separates the single card (Pile 1 = bears, Pile 2 = empty)
        harness.handleMultipleGraveyardCardsChosen(player2, List.of(bears.getId()));

        // Controller chooses Pile 1
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cards from opponent's graveyard go to battlefield under controller's control")
    void opponentGraveyardCardsGoUnderControllerControl() {
        Card opponentCreature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(opponentCreature));
        harness.setHand(player1, List.of(new BoneyardParley()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleGraveyardCardsChosen(player1, List.of(opponentCreature.getId()));
        harness.passBothPriorities();

        // Opponent separates: 1 card in Pile 1
        harness.handleMultipleGraveyardCardsChosen(player2, List.of(opponentCreature.getId()));

        // Controller chooses Pile 1
        harness.handleMayAbilityChosen(player1, true);

        // Opponent's creature should be on the battlefield under player1's control
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears")
                        && p.getCard().getId().equals(opponentCreature.getId()));

        // Player2's graveyard should be empty
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Other pile cards return to their owners' graveyards")
    void otherPileReturnsToOwnersGraveyards() {
        Card myCreature = new GrizzlyBears();
        Card opponentCreature = new LlanowarElves();
        harness.setGraveyard(player1, List.of(myCreature));
        harness.setGraveyard(player2, List.of(opponentCreature));
        harness.setHand(player1, List.of(new BoneyardParley()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castSorcery(player1, 0, 0);
        List<UUID> targets = new ArrayList<>(gd.interaction.multiSelection().multiGraveyardValidCardIds());
        harness.handleMultipleGraveyardCardsChosen(player1, targets);
        harness.passBothPriorities();

        // Opponent puts myCreature in Pile 1, opponentCreature in Pile 2
        harness.handleMultipleGraveyardCardsChosen(player2, List.of(myCreature.getId()));

        // Controller chooses Pile 1 (myCreature to battlefield)
        harness.handleMayAbilityChosen(player1, true);

        // myCreature on player1's battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(myCreature.getId()));

        // opponentCreature should return to player2's graveyard (its owner)
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(opponentCreature.getId()));
    }

    @Test
    @DisplayName("Exile log is generated")
    void exileLogIsGenerated() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new BoneyardParley()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castSorcery(player1, 0, 0);
        List<UUID> targets = new ArrayList<>(gd.interaction.multiSelection().multiGraveyardValidCardIds());
        harness.handleMultipleGraveyardCardsChosen(player1, targets);
        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("exiles") && log.contains("Grizzly Bears"));
    }
}
