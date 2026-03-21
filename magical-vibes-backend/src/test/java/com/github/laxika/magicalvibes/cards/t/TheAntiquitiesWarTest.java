package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.c.ChromaticStar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AnimateControlledPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TheAntiquitiesWarTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Chapter I looks at top 5 cards for an artifact")
    void chapterIHasCorrectEffect() {
        TheAntiquitiesWar card = new TheAntiquitiesWar();

        var effects = card.getEffects(EffectSlot.SAGA_CHAPTER_I);
        assertThat(effects).hasSize(1);
        assertThat(effects.getFirst()).isInstanceOf(LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect.class);
        LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect effect =
                (LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect) effects.getFirst();
        assertThat(effect.count()).isEqualTo(5);
        assertThat(effect.predicate()).isInstanceOf(CardTypePredicate.class);
        assertThat(((CardTypePredicate) effect.predicate()).cardType()).isEqualTo(CardType.ARTIFACT);
    }

    @Test
    @DisplayName("Chapter II has same effect as chapter I")
    void chapterIIHasCorrectEffect() {
        TheAntiquitiesWar card = new TheAntiquitiesWar();

        var effects = card.getEffects(EffectSlot.SAGA_CHAPTER_II);
        assertThat(effects).hasSize(1);
        assertThat(effects.getFirst()).isInstanceOf(LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect.class);
        LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect effect =
                (LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect) effects.getFirst();
        assertThat(effect.count()).isEqualTo(5);
        assertThat(effect.predicate()).isInstanceOf(CardTypePredicate.class);
        assertThat(((CardTypePredicate) effect.predicate()).cardType()).isEqualTo(CardType.ARTIFACT);
    }

    @Test
    @DisplayName("Chapter III animates controlled artifacts as 5/5 creatures")
    void chapterIIIHasCorrectEffect() {
        TheAntiquitiesWar card = new TheAntiquitiesWar();

        var effects = card.getEffects(EffectSlot.SAGA_CHAPTER_III);
        assertThat(effects).hasSize(1);
        assertThat(effects.getFirst()).isInstanceOf(AnimateControlledPermanentsEffect.class);
        AnimateControlledPermanentsEffect effect = (AnimateControlledPermanentsEffect) effects.getFirst();
        assertThat(effect.power()).isEqualTo(5);
        assertThat(effect.toughness()).isEqualTo(5);
        assertThat(effect.filter()).isInstanceOf(PermanentIsArtifactPredicate.class);
    }

    // ===== ETB: first lore counter and chapter I triggers =====

    @Test
    @DisplayName("Casting The Antiquities War adds a lore counter and triggers chapter I")
    void castingAddsLoreCounterAndTriggersChapterI() {
        harness.setHand(player1, List.of(new TheAntiquitiesWar()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment

        GameData gd = harness.getGameData();

        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("The Antiquities War"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        assertThat(saga.getLoreCounters()).isEqualTo(1);

        // Chapter I ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getDescription()).contains("chapter I");
    }

    @Test
    @DisplayName("Chapter I offers artifact cards from among top five")
    void chapterIOffersArtifactFromTopFive() {
        setupTopCards(List.of(
                new ChromaticStar(),
                new GrizzlyBears(),
                new Shock(),
                new GrizzlyBears(),
                new GrizzlyBears()
        ));
        harness.setHand(player1, List.of(new TheAntiquitiesWar()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers
        harness.passBothPriorities(); // resolve chapter I

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.librarySearch().canFailToFind()).isTrue();
        assertThat(gd.interaction.librarySearch().cards()).hasSize(1);
        assertThat(gd.interaction.librarySearch().cards().getFirst().getName()).isEqualTo("Chromatic Star");
    }

    @Test
    @DisplayName("Chapter I with no artifacts in top five goes directly to reorder")
    void chapterINoArtifactsGoesToReorder() {
        setupTopCards(List.of(
                new GrizzlyBears(),
                new GrizzlyBears(),
                new Shock(),
                new GrizzlyBears(),
                new GrizzlyBears()
        ));
        harness.setHand(player1, List.of(new TheAntiquitiesWar()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers
        harness.passBothPriorities(); // resolve chapter I

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REORDER);
        assertThat(gd.interaction.libraryView().reorderCards()).hasSize(5);
    }

    // ===== Chapter III: animate artifacts =====

    @Test
    @DisplayName("Chapter III makes noncreature artifacts into 5/5 creatures")
    void chapterIIIAnimatesNoncreatureArtifacts() {
        harness.addToBattlefield(player1, new TheAntiquitiesWar());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("The Antiquities War"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(2);

        // Add a noncreature artifact
        harness.addToBattlefield(player1, new ChromaticStar());

        Permanent star = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Chromatic Star"))
                .findFirst().orElse(null);
        assertThat(star).isNotNull();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter III triggers
        harness.passBothPriorities(); // resolve chapter III

        gd = harness.getGameData();

        star = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Chromatic Star"))
                .findFirst().orElse(null);
        assertThat(star).isNotNull();
        assertThat(star.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(star.getAnimatedPower()).isEqualTo(5);
        assertThat(star.getAnimatedToughness()).isEqualTo(5);
        assertThat(star.getGrantedCardTypes()).contains(CardType.CREATURE);
    }

    @Test
    @DisplayName("Chapter III sets artifact creatures to 5/5 base P/T")
    void chapterIIISetsArtifactCreatureBasePT() {
        harness.addToBattlefield(player1, new TheAntiquitiesWar());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("The Antiquities War"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(2);

        // Add an artifact creature (Ornithopter is 0/2)
        harness.addToBattlefield(player1, new Ornithopter());

        Permanent ornithopter = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Ornithopter"))
                .findFirst().orElse(null);
        assertThat(ornithopter).isNotNull();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter III triggers
        harness.passBothPriorities(); // resolve chapter III

        gd = harness.getGameData();

        ornithopter = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Ornithopter"))
                .findFirst().orElse(null);
        assertThat(ornithopter).isNotNull();
        assertThat(ornithopter.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(ornithopter.getAnimatedPower()).isEqualTo(5);
        assertThat(ornithopter.getAnimatedToughness()).isEqualTo(5);
        // Effective P/T should be 5/5 (base overridden by animation)
        assertThat(ornithopter.getEffectivePower()).isEqualTo(5);
        assertThat(ornithopter.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Chapter III does not affect non-artifact permanents")
    void chapterIIIDoesNotAffectNonArtifacts() {
        harness.addToBattlefield(player1, new TheAntiquitiesWar());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("The Antiquities War"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(2);

        // Add a non-artifact creature
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter III triggers
        harness.passBothPriorities(); // resolve chapter III

        gd = harness.getGameData();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElse(null);
        assertThat(bears).isNotNull();
        assertThat(bears.isAnimatedUntilEndOfTurn()).isFalse();
        // Grizzly Bears should remain 2/2
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Chapter III does not affect opponent's artifacts")
    void chapterIIIDoesNotAffectOpponentArtifacts() {
        harness.addToBattlefield(player1, new TheAntiquitiesWar());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("The Antiquities War"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(2);

        // Add an artifact to opponent's battlefield
        harness.addToBattlefield(player2, new ChromaticStar());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter III triggers
        harness.passBothPriorities(); // resolve chapter III

        gd = harness.getGameData();

        Permanent opponentStar = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Chromatic Star"))
                .findFirst().orElse(null);
        assertThat(opponentStar).isNotNull();
        assertThat(opponentStar.isAnimatedUntilEndOfTurn()).isFalse();
    }

    @Test
    @DisplayName("Chapter III preserves +1/+1 counters on top of 5/5 base")
    void chapterIIIPreservesPlusOnePlusOneCounters() {
        harness.addToBattlefield(player1, new TheAntiquitiesWar());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("The Antiquities War"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(2);

        // Add an artifact creature with +1/+1 counters
        harness.addToBattlefield(player1, new BottleGnomes());
        Permanent gnomes = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Bottle Gnomes"))
                .findFirst().orElse(null);
        assertThat(gnomes).isNotNull();
        gnomes.setPlusOnePlusOneCounters(2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter III triggers
        harness.passBothPriorities(); // resolve chapter III

        gd = harness.getGameData();

        gnomes = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Bottle Gnomes"))
                .findFirst().orElse(null);
        assertThat(gnomes).isNotNull();
        // Base 5/5 + 2 from +1/+1 counters = 7/7
        assertThat(gnomes.getEffectivePower()).isEqualTo(7);
        assertThat(gnomes.getEffectiveToughness()).isEqualTo(7);
    }

    // ===== Saga lifecycle =====

    @Test
    @DisplayName("Saga is sacrificed after chapter III resolves")
    void sagaSacrificedAfterChapterIII() {
        harness.addToBattlefield(player1, new TheAntiquitiesWar());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("The Antiquities War"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter III triggers
        harness.passBothPriorities(); // resolve chapter III

        GameData gd = harness.getGameData();

        // Saga should be sacrificed
        boolean sagaStillOnBf = gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("The Antiquities War"));
        assertThat(sagaStillOnBf).isFalse();

        // Saga should be in graveyard
        boolean sagaInGraveyard = gd.playerGraveyards.get(player1.getId()).stream()
                .anyMatch(c -> c.getName().equals("The Antiquities War"));
        assertThat(sagaInGraveyard).isTrue();
    }

    // ===== Helper =====

    private void setupTopCards(List<Card> cards) {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(cards);
    }
}
