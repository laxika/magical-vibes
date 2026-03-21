package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MillControllerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TheMendingOfDominariaTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Chapter I has mill 2 and return creature from graveyard effects")
    void chapterIHasCorrectEffects() {
        TheMendingOfDominaria card = new TheMendingOfDominaria();

        var effects = card.getEffects(EffectSlot.SAGA_CHAPTER_I);
        assertThat(effects).hasSize(2);

        assertThat(effects.get(0)).isInstanceOf(MillControllerEffect.class);
        assertThat(((MillControllerEffect) effects.get(0)).count()).isEqualTo(2);

        assertThat(effects.get(1)).isInstanceOf(ReturnCardFromGraveyardEffect.class);
        ReturnCardFromGraveyardEffect returnEffect = (ReturnCardFromGraveyardEffect) effects.get(1);
        assertThat(returnEffect.destination()).isEqualTo(GraveyardChoiceDestination.HAND);
        assertThat(returnEffect.filter()).isInstanceOf(CardTypePredicate.class);
        assertThat(((CardTypePredicate) returnEffect.filter()).cardType()).isEqualTo(CardType.CREATURE);
    }

    @Test
    @DisplayName("Chapter II has same effects as chapter I")
    void chapterIIHasCorrectEffects() {
        TheMendingOfDominaria card = new TheMendingOfDominaria();

        var effects = card.getEffects(EffectSlot.SAGA_CHAPTER_II);
        assertThat(effects).hasSize(2);

        assertThat(effects.get(0)).isInstanceOf(MillControllerEffect.class);
        assertThat(((MillControllerEffect) effects.get(0)).count()).isEqualTo(2);

        assertThat(effects.get(1)).isInstanceOf(ReturnCardFromGraveyardEffect.class);
        ReturnCardFromGraveyardEffect returnEffect = (ReturnCardFromGraveyardEffect) effects.get(1);
        assertThat(returnEffect.destination()).isEqualTo(GraveyardChoiceDestination.HAND);
        assertThat(returnEffect.filter()).isInstanceOf(CardTypePredicate.class);
        assertThat(((CardTypePredicate) returnEffect.filter()).cardType()).isEqualTo(CardType.CREATURE);
    }

    @Test
    @DisplayName("Chapter III has return all lands and shuffle graveyard effects")
    void chapterIIIHasCorrectEffects() {
        TheMendingOfDominaria card = new TheMendingOfDominaria();

        var effects = card.getEffects(EffectSlot.SAGA_CHAPTER_III);
        assertThat(effects).hasSize(2);

        assertThat(effects.get(0)).isInstanceOf(ReturnCardFromGraveyardEffect.class);
        ReturnCardFromGraveyardEffect returnEffect = (ReturnCardFromGraveyardEffect) effects.get(0);
        assertThat(returnEffect.destination()).isEqualTo(GraveyardChoiceDestination.BATTLEFIELD);
        assertThat(returnEffect.filter()).isInstanceOf(CardTypePredicate.class);
        assertThat(((CardTypePredicate) returnEffect.filter()).cardType()).isEqualTo(CardType.LAND);
        assertThat(returnEffect.returnAll()).isTrue();

        assertThat(effects.get(1)).isInstanceOf(ShuffleGraveyardIntoLibraryEffect.class);
    }

    // ===== ETB: first lore counter and chapter I triggers =====

    @Test
    @DisplayName("Casting The Mending of Dominaria adds a lore counter and triggers chapter I")
    void castingAddsLoreCounterAndTriggersChapterI() {
        harness.setHand(player1, List.of(new TheMendingOfDominaria()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment

        GameData gd = harness.getGameData();

        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("The Mending of Dominaria"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        assertThat(saga.getLoreCounters()).isEqualTo(1);

        // Chapter I ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getDescription()).contains("chapter I");
    }

    @Test
    @DisplayName("Chapter I mills two cards then offers graveyard creature choice")
    void chapterIMillsThenOffersCreatureChoice() {
        // Put creatures on top of library so they get milled
        GrizzlyBears bear = new GrizzlyBears();
        Shock shock = new Shock();
        setupTopCards(List.of(bear, shock));

        harness.setHand(player1, List.of(new TheMendingOfDominaria()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers
        harness.passBothPriorities(); // resolve chapter I → mills 2 cards, then graveyard choice

        GameData gd = harness.getGameData();

        // The 2 cards should have been milled to graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Should be awaiting graveyard choice for the creature
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.GRAVEYARD_CHOICE);
    }

    @Test
    @DisplayName("Chapter I with no creatures milled does not offer graveyard choice")
    void chapterINoCreaturesMilledSkipsChoice() {
        // Put only non-creature cards on top of library
        Shock shock1 = new Shock();
        Shock shock2 = new Shock();
        setupTopCards(List.of(shock1, shock2));

        harness.setHand(player1, List.of(new TheMendingOfDominaria()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers
        harness.passBothPriorities(); // resolve chapter I

        GameData gd = harness.getGameData();

        // No graveyard choice since no creatures were milled (and none were in graveyard)
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.GRAVEYARD_CHOICE);
    }

    @Test
    @DisplayName("Chapter I can return a creature that was already in graveyard before milling")
    void chapterICanReturnCreatureAlreadyInGraveyard() {
        // Put a creature in graveyard and non-creatures on top of library
        GrizzlyBears bear = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bear));
        setupTopCards(List.of(new Shock(), new Shock()));

        harness.setHand(player1, List.of(new TheMendingOfDominaria()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers
        harness.passBothPriorities(); // resolve chapter I → mills 2 non-creatures, then graveyard choice

        GameData gd = harness.getGameData();

        // Should still offer graveyard choice for the creature that was already in graveyard
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.GRAVEYARD_CHOICE);
    }

    // ===== Chapter III: return lands and shuffle =====

    @Test
    @DisplayName("Chapter III returns all land cards from graveyard to battlefield")
    void chapterIIIReturnsLandsToBattlefield() {
        harness.addToBattlefield(player1, new TheMendingOfDominaria());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("The Mending of Dominaria"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(2);

        // Put lands in graveyard
        Forest forest1 = new Forest();
        Forest forest2 = new Forest();
        Plains plains = new Plains();
        GrizzlyBears bear = new GrizzlyBears(); // non-land should not be returned
        harness.setGraveyard(player1, List.of(forest1, forest2, plains, bear));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter III triggers
        harness.passBothPriorities(); // resolve chapter III

        gd = harness.getGameData();

        // All 3 lands should be on the battlefield
        long landCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .count();
        assertThat(landCount).isEqualTo(3);

        // Non-land creature should NOT be on the battlefield
        boolean bearOnBf = gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(bearOnBf).isFalse();
    }

    @Test
    @DisplayName("Chapter III shuffles remaining graveyard into library after returning lands")
    void chapterIIIShufflesGraveyardIntoLibrary() {
        harness.addToBattlefield(player1, new TheMendingOfDominaria());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("The Mending of Dominaria"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(2);

        // Put lands and a creature in graveyard
        Forest forest = new Forest();
        GrizzlyBears bear = new GrizzlyBears();
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(forest, bear, shock));
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter III triggers
        harness.passBothPriorities(); // resolve chapter III

        gd = harness.getGameData();

        // Forest should be on battlefield (returned)
        boolean forestOnBf = gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("Forest"));
        assertThat(forestOnBf).isTrue();

        // Graveyard should only contain the saga itself (sacrificed after chapter III resolves)
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("The Mending of Dominaria");

        // Library should have gained the non-land cards (bear + shock were shuffled in)
        assertThat(gd.playerDecks.get(player1.getId()).size()).isGreaterThan(deckSizeBefore);
    }

    @Test
    @DisplayName("Chapter III with no lands in graveyard still shuffles graveyard into library")
    void chapterIIINoLandsStillShuffles() {
        harness.addToBattlefield(player1, new TheMendingOfDominaria());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("The Mending of Dominaria"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(2);

        // Only non-lands in graveyard
        GrizzlyBears bear = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bear));
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter III triggers
        harness.passBothPriorities(); // resolve chapter III

        gd = harness.getGameData();

        // No lands on battlefield (except any that were already there)
        boolean bearOnBf = gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(bearOnBf).isFalse();

        // Graveyard should only contain the saga itself (sacrificed after chapter III resolves)
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("The Mending of Dominaria");

        // Library should have gained the bear (shuffled in)
        assertThat(gd.playerDecks.get(player1.getId()).size()).isGreaterThan(deckSizeBefore);
    }

    // ===== Saga lifecycle =====

    @Test
    @DisplayName("Saga is sacrificed after chapter III resolves")
    void sagaSacrificedAfterChapterIII() {
        harness.addToBattlefield(player1, new TheMendingOfDominaria());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("The Mending of Dominaria"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter III triggers
        harness.passBothPriorities(); // resolve chapter III

        GameData gd = harness.getGameData();

        // Saga should be sacrificed (no longer on battlefield)
        boolean sagaStillOnBf = gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("The Mending of Dominaria"));
        assertThat(sagaStillOnBf).isFalse();
    }

    // ===== Helper =====

    private void setupTopCards(List<Card> cards) {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(cards);
    }
}
