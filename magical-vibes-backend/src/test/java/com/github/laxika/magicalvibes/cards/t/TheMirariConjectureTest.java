package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GrantInstantSorceryCopyUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TheMirariConjectureTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Chapter I returns target instant from graveyard to hand")
    void chapterIHasCorrectEffects() {
        TheMirariConjecture card = new TheMirariConjecture();

        var effects = card.getEffects(EffectSlot.SAGA_CHAPTER_I);
        assertThat(effects).hasSize(1);
        assertThat(effects.getFirst()).isInstanceOf(ReturnCardFromGraveyardEffect.class);
        ReturnCardFromGraveyardEffect effect = (ReturnCardFromGraveyardEffect) effects.getFirst();
        assertThat(effect.targetGraveyard()).isTrue();
    }

    @Test
    @DisplayName("Chapter II returns target sorcery from graveyard to hand")
    void chapterIIHasCorrectEffects() {
        TheMirariConjecture card = new TheMirariConjecture();

        var effects = card.getEffects(EffectSlot.SAGA_CHAPTER_II);
        assertThat(effects).hasSize(1);
        assertThat(effects.getFirst()).isInstanceOf(ReturnCardFromGraveyardEffect.class);
        ReturnCardFromGraveyardEffect effect = (ReturnCardFromGraveyardEffect) effects.getFirst();
        assertThat(effect.targetGraveyard()).isTrue();
    }

    @Test
    @DisplayName("Chapter III grants spell copy until end of turn")
    void chapterIIIHasCorrectEffects() {
        TheMirariConjecture card = new TheMirariConjecture();

        var effects = card.getEffects(EffectSlot.SAGA_CHAPTER_III);
        assertThat(effects).hasSize(1);
        assertThat(effects.getFirst()).isInstanceOf(GrantInstantSorceryCopyUntilEndOfTurnEffect.class);
    }

    // ===== Chapter I: graveyard targeting for instants =====

    @Test
    @DisplayName("Casting The Mirari Conjecture prompts for instant target in graveyard")
    void castingPromptsForInstantGraveyardTarget() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(new TheMirariConjecture()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers

        // Should prompt for graveyard target selection (instant in graveyard)
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_GRAVEYARD_CHOICE);
    }

    @Test
    @DisplayName("Chapter I returns selected instant from graveyard to hand")
    void chapterIReturnsInstantToHand() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(new TheMirariConjecture()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers

        // Select the instant from graveyard
        harness.handleMultipleGraveyardCardsChosen(player1, List.of(shock.getId()));

        // Triggered ability should be on the stack
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getDescription().contains("chapter I"));

        // Resolve the chapter ability
        harness.passBothPriorities();

        // Shock should be in hand, not in graveyard
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Shock"));
    }

    @Test
    @DisplayName("Chapter I skips when no instants in graveyard")
    void chapterISkipsWithNoInstants() {
        // Only a sorcery in graveyard - chapter I should skip
        Divination divination = new Divination();
        harness.setGraveyard(player1, List.of(divination));
        harness.setHand(player1, List.of(new TheMirariConjecture()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers but no valid targets

        // Should NOT prompt for graveyard choice (no instants in graveyard)
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.MULTI_GRAVEYARD_CHOICE);
    }

    @Test
    @DisplayName("Chapter I does not show sorceries as valid targets")
    void chapterIDoesNotTargetSorceries() {
        // Sorcery + instant in graveyard
        Divination divination = new Divination();
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(divination, shock));
        harness.setHand(player1, List.of(new TheMirariConjecture()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers

        // Should prompt for graveyard target selection
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_GRAVEYARD_CHOICE);

        // Select the instant
        harness.handleMultipleGraveyardCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();

        // Only Shock returned, Divination still in graveyard
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Divination"));
    }

    // ===== Chapter II: graveyard targeting for sorceries =====

    @Test
    @DisplayName("Chapter II prompts for sorcery target in graveyard")
    void chapterIIPromptsForSorceryTarget() {
        Divination divination = new Divination();
        harness.addToBattlefield(player1, new TheMirariConjecture());
        Permanent saga = findSaga(player1);
        saga.setLoreCounters(1);
        harness.setGraveyard(player1, List.of(divination));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to precombat main → chapter II triggers

        assertThat(saga.getLoreCounters()).isEqualTo(2);
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_GRAVEYARD_CHOICE);
    }

    @Test
    @DisplayName("Chapter II returns selected sorcery from graveyard to hand")
    void chapterIIReturnsSorceryToHand() {
        Divination divination = new Divination();
        harness.addToBattlefield(player1, new TheMirariConjecture());
        Permanent saga = findSaga(player1);
        saga.setLoreCounters(1);
        harness.setGraveyard(player1, List.of(divination));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter II triggers

        // Select the sorcery from graveyard
        harness.handleMultipleGraveyardCardsChosen(player1, List.of(divination.getId()));
        harness.passBothPriorities(); // resolve chapter II

        // Divination should be in hand
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Divination"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Divination"));
    }

    @Test
    @DisplayName("Chapter II skips when no sorceries in graveyard")
    void chapterIISkipsWithNoSorceries() {
        // Only an instant in graveyard - chapter II should skip
        Shock shock = new Shock();
        harness.addToBattlefield(player1, new TheMirariConjecture());
        Permanent saga = findSaga(player1);
        saga.setLoreCounters(1);
        harness.setGraveyard(player1, List.of(shock));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter II triggers but no valid targets

        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.MULTI_GRAVEYARD_CHOICE);
    }

    // ===== Chapter III: copy instant/sorcery spells =====

    @Test
    @DisplayName("Chapter III adds controller to spell copy set")
    void chapterIIIGrantsSpellCopy() {
        harness.addToBattlefield(player1, new TheMirariConjecture());
        Permanent saga = findSaga(player1);
        saga.setLoreCounters(2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter III triggers

        assertThat(saga.getLoreCounters()).isEqualTo(3);

        harness.passBothPriorities(); // resolve chapter III

        assertThat(gd.playersWithSpellCopyUntilEndOfTurn).contains(player1.getId());
    }

    @Test
    @DisplayName("After chapter III, casting an instant creates a copy on the stack")
    void chapterIIICopiesInstantSpell() {
        // Set up the copy effect directly
        gd.playersWithSpellCopyUntilEndOfTurn.add(player1.getId());

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        // Stack should have: original Lightning Bolt + copy triggered ability
        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getDescription().contains("Copy"));
    }

    @Test
    @DisplayName("After chapter III, casting a sorcery creates a copy on the stack")
    void chapterIIICopiesSorcerySpell() {
        gd.playersWithSpellCopyUntilEndOfTurn.add(player1.getId());

        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);

        // Stack should have: original Divination + copy triggered ability
        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getDescription().contains("Copy"));
    }

    @Test
    @DisplayName("Copy triggered ability resolves and creates a spell copy")
    void copyTriggerResolvesCreatingCopy() {
        gd.playersWithSpellCopyUntilEndOfTurn.add(player1.getId());

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        // Resolve the copy trigger (top of stack)
        harness.passBothPriorities();

        // Now the stack should have the copy + original
        long spellCount = gd.stack.stream()
                .filter(e -> e.getCard().getName().equals("Lightning Bolt"))
                .count();
        assertThat(spellCount).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Spell copy effect does not apply to creature spells")
    void spellCopyDoesNotApplyToCreatures() {
        gd.playersWithSpellCopyUntilEndOfTurn.add(player1.getId());

        Card creature = new com.github.laxika.magicalvibes.cards.g.GrizzlyBears();
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        // Stack should only have the creature spell, no copy trigger
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getDescription().contains("Copy"));
    }

    @Test
    @DisplayName("Spell copy effect is cleared at end of turn")
    void spellCopyIsClearedAtEndOfTurn() {
        gd.playersWithSpellCopyUntilEndOfTurn.add(player1.getId());
        assertThat(gd.playersWithSpellCopyUntilEndOfTurn).isNotEmpty();

        // Simulate end-of-turn by advancing through the end step → cleanup
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance through end step → cleanup clears the set

        assertThat(gd.playersWithSpellCopyUntilEndOfTurn).isEmpty();
    }

    // ===== Saga lifecycle =====

    @Test
    @DisplayName("Saga is sacrificed after chapter III resolves")
    void sagaSacrificedAfterChapterIII() {
        harness.addToBattlefield(player1, new TheMirariConjecture());
        Permanent saga = findSaga(player1);
        saga.setLoreCounters(2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter III triggers

        // Saga should still be on battlefield while chapter is on stack
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("The Mirari Conjecture"));

        harness.passBothPriorities(); // resolve chapter III

        // Saga should be sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("The Mirari Conjecture"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("The Mirari Conjecture"));
    }

    @Test
    @DisplayName("Saga with lore counter 1 enters and chapter I triggers on ETB")
    void sagaEntersWithLoreCounterOne() {
        harness.setHand(player1, List.of(new TheMirariConjecture()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment

        Permanent saga = findSaga(player1);
        assertThat(saga).isNotNull();
        assertThat(saga.getLoreCounters()).isEqualTo(1);
    }

    // ===== Helpers =====

    private Permanent findSaga(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("The Mirari Conjecture"))
                .findFirst().orElse(null);
    }
}
