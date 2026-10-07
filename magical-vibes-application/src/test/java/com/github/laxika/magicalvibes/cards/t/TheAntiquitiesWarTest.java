package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.c.ChromaticStar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({TheAntiquitiesWar.class, BottleGnomes.class, ChromaticStar.class,
        GrizzlyBears.class, Ornithopter.class, Shock.class})
class TheAntiquitiesWarTest extends BaseCardTest {

    @Test
    @DisplayName("Casting The Antiquities War adds a lore counter and triggers chapter I")
    void castingAddsLoreCounterAndTriggersChapterI() {
        harness.castFromHand(player1, new TheAntiquitiesWar(), "{3}{U}");
        harness.passBothPriorities(); // resolve enchantment

        GameData gd = harness.getGameData();

        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("The Antiquities War"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);

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
        harness.castFromHand(player1, new TheAntiquitiesWar(), "{3}{U}");
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers
        harness.passBothPriorities(); // resolve chapter I

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().canFailToFind()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().getFirst().getName()).isEqualTo("Chromatic Star");
    }

    @Test
    @DisplayName("Chapter I with no artifacts bottoms all five without an ordering choice")
    void chapterINoArtifactsBottomsWithoutOrderingChoice() {
        setupTopCards(List.of(
                new GrizzlyBears(),
                new GrizzlyBears(),
                new Shock(),
                new GrizzlyBears(),
                new GrizzlyBears()
        ));
        harness.castFromHand(player1, new TheAntiquitiesWar(), "{3}{U}");
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers
        harness.passBothPriorities(); // resolve chapter I

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Chapter III makes noncreature artifacts into 5/5 creatures")
    void chapterIIIAnimatesNoncreatureArtifacts() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheAntiquitiesWar());
        saga.setCounterCount(CounterType.LORE, 2);

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
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheAntiquitiesWar());
        saga.setCounterCount(CounterType.LORE, 2);

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
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheAntiquitiesWar());
        saga.setCounterCount(CounterType.LORE, 2);

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
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheAntiquitiesWar());
        saga.setCounterCount(CounterType.LORE, 2);

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
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheAntiquitiesWar());
        saga.setCounterCount(CounterType.LORE, 2);

        // Add an artifact creature with +1/+1 counters
        harness.addToBattlefield(player1, new BottleGnomes());
        Permanent gnomes = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Bottle Gnomes"))
                .findFirst().orElse(null);
        assertThat(gnomes).isNotNull();
        gnomes.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

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

    @Test
    @DisplayName("Saga is sacrificed after chapter III resolves")
    void sagaSacrificedAfterChapterIII() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheAntiquitiesWar());
        saga.setCounterCount(CounterType.LORE, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter III triggers
        harness.passBothPriorities(); // resolve chapter III

        harness.assertNotOnBattlefield(player1, "The Antiquities War");
        harness.assertInGraveyard(player1, "The Antiquities War");
    }

    @Test
    void chapterIIAllowsDecliningAndBottomsOnlyTheLookedAtCards() {
        harness.setHand(player1, List.of());
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheAntiquitiesWar());
        saga.setCounterCount(CounterType.LORE, 1);
        Card artifact = new ChromaticStar();
        List<Card> topFive = List.of(artifact, new GrizzlyBears(), new Shock(),
                new Ornithopter(), new BottleGnomes());
        Card untouched = new Shock();
        harness.setLibrary(player1, List.of(topFive.get(0), topFive.get(1), topFive.get(2),
                topFive.get(3), topFive.get(4), untouched));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(2);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.LibrarySearch) {
            harness.handleCardChosen(player1, -1);
        } else {
            harness.handleMultipleCardsChosen(player1, List.of());
        }
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 6))
                .containsExactlyInAnyOrderElementsOf(topFive);
    }

    @Test
    void chapterISelectsOnlyOneArtifactAndBottomsTheRestWithoutOrderingChoice() {
        Card selected = new ChromaticStar();
        List<Card> remaining = List.of(new Ornithopter(), new BottleGnomes(),
                new GrizzlyBears(), new Shock());
        Card untouched = new Shock();
        harness.setLibrary(player1, List.of(selected, remaining.get(0), remaining.get(1),
                remaining.get(2), remaining.get(3), untouched));
        harness.castFromHand(player1, new TheAntiquitiesWar(), "{3}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.LibrarySearch) {
            harness.handleCardChosen(player1, 0);
        } else {
            harness.handleMultipleCardsChosen(player1, List.of(selected.getId()));
        }
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(selected);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 5))
                .containsExactlyInAnyOrderElementsOf(remaining);
    }

    @Test
    void chapterIIIDoesNotAnimateArtifactsEnteringAfterResolution() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheAntiquitiesWar());
        saga.setCounterCount(CounterType.LORE, 2);
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new ChromaticStar());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "The Antiquities War");
        harness.passBothPriorities();
        assertThat(existing.getEffectivePower()).isEqualTo(5);
        Permanent later = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        assertThat(later.getEffectivePower()).isZero();
        assertThat(later.getEffectiveToughness()).isEqualTo(2);
    }

    private void setupTopCards(List<Card> cards) {
        harness.setLibrary(player1, cards);
    }
}
