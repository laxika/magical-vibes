package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LilianaVess;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.EachOpponentDiscardsEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentSacrificesPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TheEldestRebornTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Chapter I has EachOpponentSacrificesPermanentsEffect for creature or planeswalker")
    void chapterIHasCorrectEffect() {
        TheEldestReborn card = new TheEldestReborn();

        var effects = card.getEffects(EffectSlot.SAGA_CHAPTER_I);
        assertThat(effects).hasSize(1);
        assertThat(effects.getFirst()).isInstanceOf(EachOpponentSacrificesPermanentsEffect.class);
        EachOpponentSacrificesPermanentsEffect effect = (EachOpponentSacrificesPermanentsEffect) effects.getFirst();
        assertThat(effect.count()).isEqualTo(1);
        assertThat(effect.filter()).isInstanceOf(PermanentAnyOfPredicate.class);
        PermanentAnyOfPredicate filter = (PermanentAnyOfPredicate) effect.filter();
        assertThat(filter.predicates()).hasSize(2);
        assertThat(filter.predicates().get(0)).isInstanceOf(PermanentIsCreaturePredicate.class);
        assertThat(filter.predicates().get(1)).isInstanceOf(PermanentIsPlaneswalkerPredicate.class);
    }

    @Test
    @DisplayName("Chapter II has EachOpponentDiscardsEffect for 1 card")
    void chapterIIHasCorrectEffect() {
        TheEldestReborn card = new TheEldestReborn();

        var effects = card.getEffects(EffectSlot.SAGA_CHAPTER_II);
        assertThat(effects).hasSize(1);
        assertThat(effects.getFirst()).isInstanceOf(EachOpponentDiscardsEffect.class);
        EachOpponentDiscardsEffect effect = (EachOpponentDiscardsEffect) effects.getFirst();
        assertThat(effect.amount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Chapter III has ReturnCardFromGraveyardEffect for creature or planeswalker from any graveyard")
    void chapterIIIHasCorrectEffect() {
        TheEldestReborn card = new TheEldestReborn();

        var effects = card.getEffects(EffectSlot.SAGA_CHAPTER_III);
        assertThat(effects).hasSize(1);
        assertThat(effects.getFirst()).isInstanceOf(ReturnCardFromGraveyardEffect.class);
        ReturnCardFromGraveyardEffect effect = (ReturnCardFromGraveyardEffect) effects.getFirst();
        assertThat(effect.destination()).isEqualTo(GraveyardChoiceDestination.BATTLEFIELD);
        assertThat(effect.source()).isEqualTo(GraveyardSearchScope.ALL_GRAVEYARDS);
        assertThat(effect.filter()).isInstanceOf(CardAnyOfPredicate.class);
        CardAnyOfPredicate filter = (CardAnyOfPredicate) effect.filter();
        assertThat(filter.predicates()).hasSize(2);
        assertThat(filter.predicates().get(0)).isInstanceOf(CardTypePredicate.class);
        assertThat(((CardTypePredicate) filter.predicates().get(0)).cardType()).isEqualTo(CardType.CREATURE);
        assertThat(filter.predicates().get(1)).isInstanceOf(CardTypePredicate.class);
        assertThat(((CardTypePredicate) filter.predicates().get(1)).cardType()).isEqualTo(CardType.PLANESWALKER);
    }

    // ===== ETB: first lore counter and chapter I triggers =====

    @Test
    @DisplayName("Casting The Eldest Reborn adds a lore counter and triggers chapter I")
    void castingAddsLoreCounterAndTriggersChapterI() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TheEldestReborn()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment

        GameData gd = harness.getGameData();

        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("The Eldest Reborn"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        assertThat(saga.getLoreCounters()).isEqualTo(1);

        // Chapter I ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getDescription()).contains("chapter I");
    }

    // ===== Chapter I: each opponent sacrifices a creature or planeswalker =====

    @Test
    @DisplayName("Chapter I forces opponent to sacrifice their only creature")
    void chapterIForcesOpponentToSacrificeOnlyCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TheEldestReborn()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers
        harness.passBothPriorities(); // resolve chapter I

        GameData gd = harness.getGameData();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Chapter I forces opponent to sacrifice a planeswalker when they have no creatures")
    void chapterIForcesOpponentToSacrificePlaneswalker() {
        harness.addToBattlefield(player2, new LilianaVess());
        harness.setHand(player1, List.of(new TheEldestReborn()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers
        harness.passBothPriorities(); // resolve chapter I

        GameData gd = harness.getGameData();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Liliana Vess"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Liliana Vess"));
    }

    @Test
    @DisplayName("Chapter I does nothing when opponent has no creatures or planeswalkers")
    void chapterIDoesNothingWhenOpponentHasNone() {
        // Player2 has no creatures or planeswalkers
        harness.setHand(player1, List.of(new TheEldestReborn()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers
        harness.passBothPriorities(); // resolve chapter I

        GameData gd = harness.getGameData();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Chapter I does not affect controller's permanents")
    void chapterIDoesNotAffectController() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TheEldestReborn()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers
        harness.passBothPriorities(); // resolve chapter I

        GameData gd = harness.getGameData();

        // Controller's creature should still be there
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        // Opponent's creature should be gone
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    // ===== Chapter II: each opponent discards a card =====

    @Test
    @DisplayName("Chapter II forces opponent to discard a card")
    void chapterIIForcesOpponentToDiscard() {
        harness.addToBattlefield(player1, new TheEldestReborn());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("The Eldest Reborn"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(1);

        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to precombat main → chapter II triggers

        GameData gd = harness.getGameData();
        assertThat(saga.getLoreCounters()).isEqualTo(2);
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getDescription().contains("chapter II"));

        harness.passBothPriorities(); // resolve chapter II

        gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.DISCARD_CHOICE);
        assertThat(gd.interaction.cardChoice().playerId()).isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        gd = harness.getGameData();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Chapter II does nothing when opponent has no cards in hand")
    void chapterIIDoesNothingWithEmptyHand() {
        harness.addToBattlefield(player1, new TheEldestReborn());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("The Eldest Reborn"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(1);

        harness.setHand(player2, new ArrayList<>());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to precombat main → chapter II triggers
        harness.passBothPriorities(); // resolve chapter II

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    // ===== Chapter III: return creature or planeswalker from graveyard =====

    @Test
    @DisplayName("Chapter III returns a creature from own graveyard to battlefield")
    void chapterIIIReturnsCreatureFromOwnGraveyard() {
        harness.addToBattlefield(player1, new TheEldestReborn());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("The Eldest Reborn"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(2);

        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to precombat main → chapter III triggers
        harness.passBothPriorities(); // resolve chapter III

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.GRAVEYARD_CHOICE);

        harness.handleGraveyardCardChosen(player1, 0);

        gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Chapter III returns a creature from opponent's graveyard under your control")
    void chapterIIIReturnsCreatureFromOpponentGraveyard() {
        harness.addToBattlefield(player1, new TheEldestReborn());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("The Eldest Reborn"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(2);

        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to precombat main → chapter III triggers
        harness.passBothPriorities(); // resolve chapter III

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.GRAVEYARD_CHOICE);

        harness.handleGraveyardCardChosen(player1, 0);

        gd = harness.getGameData();
        // Should be on player1's battlefield (under your control)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Saga lifecycle =====

    @Test
    @DisplayName("Saga is sacrificed after chapter III resolves")
    void sagaSacrificedAfterChapterIII() {
        harness.addToBattlefield(player1, new TheEldestReborn());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("The Eldest Reborn"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(2);

        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter III triggers

        // Chapter III on stack — saga should still be on battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("The Eldest Reborn"));

        harness.passBothPriorities(); // resolve chapter III

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.GRAVEYARD_CHOICE);

        harness.handleGraveyardCardChosen(player1, 0);
        harness.runStateBasedActions(); // SBAs sacrifice the saga (lore counters >= final chapter)

        gd = harness.getGameData();

        // Saga should be sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("The Eldest Reborn"));
        // Saga should be in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("The Eldest Reborn"));
    }

    @Test
    @DisplayName("Saga is not sacrificed while chapter III ability is on the stack")
    void sagaNotSacrificedWhileChapterOnStack() {
        harness.addToBattlefield(player1, new TheEldestReborn());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("The Eldest Reborn"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(2);

        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → lore counter 3, chapter III triggers

        GameData gd = harness.getGameData();

        assertThat(saga.getLoreCounters()).isEqualTo(3);
        assertThat(gd.stack).isNotEmpty();
        // Saga should still be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
    }
}
