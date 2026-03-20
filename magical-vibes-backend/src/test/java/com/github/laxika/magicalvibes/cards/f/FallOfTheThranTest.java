package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FallOfTheThranTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Chapter I has destroy all lands effect")
    void chapterIHasDestroyAllLandsEffect() {
        FallOfTheThran card = new FallOfTheThran();

        var effects = card.getEffects(EffectSlot.SAGA_CHAPTER_I);
        assertThat(effects).hasSize(1);
        assertThat(effects.getFirst()).isInstanceOf(DestroyAllPermanentsEffect.class);
        DestroyAllPermanentsEffect effect = (DestroyAllPermanentsEffect) effects.getFirst();
        assertThat(effect.filter()).isInstanceOf(PermanentIsLandPredicate.class);
    }

    @Test
    @DisplayName("Chapter II has each player returns up to two land cards effect")
    void chapterIIHasReturnLandsEffect() {
        FallOfTheThran card = new FallOfTheThran();

        var effects = card.getEffects(EffectSlot.SAGA_CHAPTER_II);
        assertThat(effects).hasSize(1);
        assertThat(effects.getFirst()).isInstanceOf(EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect.class);
        EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect effect =
                (EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect) effects.getFirst();
        assertThat(effect.maxCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Chapter III has same effect as chapter II")
    void chapterIIIHasReturnLandsEffect() {
        FallOfTheThran card = new FallOfTheThran();

        var effects = card.getEffects(EffectSlot.SAGA_CHAPTER_III);
        assertThat(effects).hasSize(1);
        assertThat(effects.getFirst()).isInstanceOf(EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect.class);
        EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect effect =
                (EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect) effects.getFirst();
        assertThat(effect.maxCount()).isEqualTo(2);
    }

    // ===== Chapter I: Destroy all lands =====

    @Test
    @DisplayName("Chapter I destroys all lands on the battlefield for both players")
    void chapterIDestroysAllLands() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Forest());

        harness.setHand(player1, List.of(new FallOfTheThran()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers

        GameData gd = harness.getGameData();
        // Chapter I should be on the stack
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getDescription().contains("chapter I"));

        harness.passBothPriorities(); // resolve chapter I

        gd = harness.getGameData();

        // All lands should be destroyed
        long p1Lands = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .count();
        long p2Lands = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .count();
        assertThat(p1Lands).isZero();
        assertThat(p2Lands).isZero();

        // Lands should be in graveyards
        long p1GraveyardLands = gd.playerGraveyards.get(player1.getId()).stream()
                .filter(c -> c.hasType(CardType.LAND))
                .count();
        long p2GraveyardLands = gd.playerGraveyards.get(player2.getId()).stream()
                .filter(c -> c.hasType(CardType.LAND))
                .count();
        assertThat(p1GraveyardLands).isEqualTo(2);
        assertThat(p2GraveyardLands).isEqualTo(2);
    }

    // ===== Chapters II/III: Each player returns up to two land cards =====

    @Test
    @DisplayName("Chapter II returns up to two lands from each player's graveyard when they have 2 or fewer")
    void chapterIIReturnsLandsAutoWhenTwoOrFewer() {
        harness.addToBattlefield(player1, new FallOfTheThran());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fall of the Thran"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(1);

        // Put 2 lands in each player's graveyard
        harness.setGraveyard(player1, new ArrayList<>(List.of(new Plains(), new Island())));
        harness.setGraveyard(player2, new ArrayList<>(List.of(new Mountain(), new Forest())));

        // Advance to precombat main to trigger chapter II
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to precombat main → chapter II triggers

        GameData gd = harness.getGameData();
        assertThat(saga.getLoreCounters()).isEqualTo(2);
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getDescription().contains("chapter II"));

        // Resolve chapter II
        harness.passBothPriorities();

        gd = harness.getGameData();

        // Both players should have their lands back on the battlefield
        long p1Lands = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .count();
        long p2Lands = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .count();
        assertThat(p1Lands).isEqualTo(2);
        assertThat(p2Lands).isEqualTo(2);

        // Graveyards should have no lands
        long p1GraveyardLands = gd.playerGraveyards.get(player1.getId()).stream()
                .filter(c -> c.hasType(CardType.LAND))
                .count();
        long p2GraveyardLands = gd.playerGraveyards.get(player2.getId()).stream()
                .filter(c -> c.hasType(CardType.LAND))
                .count();
        assertThat(p1GraveyardLands).isZero();
        assertThat(p2GraveyardLands).isZero();
    }

    @Test
    @DisplayName("Chapter II returns only one land when player has exactly one in graveyard")
    void chapterIIReturnsOneLandWhenOnlyOneAvailable() {
        harness.addToBattlefield(player1, new FallOfTheThran());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fall of the Thran"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(1);

        // Put 1 land in player1's graveyard, none in player2's
        harness.setGraveyard(player1, new ArrayList<>(List.of(new Plains())));
        harness.setGraveyard(player2, new ArrayList<>());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter II triggers
        harness.passBothPriorities(); // resolve chapter II

        GameData gd = harness.getGameData();

        long p1Lands = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .count();
        assertThat(p1Lands).isEqualTo(1);
    }

    @Test
    @DisplayName("Chapter II does nothing when no lands in any graveyard")
    void chapterIIDoesNothingWhenNoLandsInGraveyard() {
        harness.addToBattlefield(player1, new FallOfTheThran());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fall of the Thran"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(1);

        harness.setGraveyard(player1, new ArrayList<>());
        harness.setGraveyard(player2, new ArrayList<>());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter II triggers
        harness.passBothPriorities(); // resolve chapter II

        GameData gd = harness.getGameData();

        long p1Lands = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .count();
        long p2Lands = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .count();
        assertThat(p1Lands).isZero();
        assertThat(p2Lands).isZero();
    }

    @Test
    @DisplayName("Chapter II prompts choice when player has more than two lands in graveyard")
    void chapterIIPromptsChoiceWhenMoreThanTwoLands() {
        harness.addToBattlefield(player1, new FallOfTheThran());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fall of the Thran"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(1);

        // Put 3 lands in player1's graveyard (more than maxCount of 2)
        harness.setGraveyard(player1, new ArrayList<>(List.of(new Plains(), new Island(), new Forest())));
        harness.setGraveyard(player2, new ArrayList<>(List.of(new Mountain())));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter II triggers
        harness.passBothPriorities(); // resolve chapter II — player1 must choose (3 > 2), player2 auto-returns 1

        GameData gd = harness.getGameData();

        // Player1 should be prompted to choose (graveyard choice awaiting input)
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        // Player2's 1 land should already be on the battlefield (auto-returned)
        long p2Lands = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .count();
        assertThat(p2Lands).isEqualTo(1);

        // Player1 chooses first land (index 0)
        harness.handleGraveyardCardChosen(player1, 0);

        gd = harness.getGameData();
        // Player1 should be prompted again for second land
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        // Player1 chooses second land (now index 0 since previous was removed)
        harness.handleGraveyardCardChosen(player1, 0);

        gd = harness.getGameData();

        // Player1 should have 2 lands on the battlefield
        long p1Lands = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .count();
        assertThat(p1Lands).isEqualTo(2);

        // Player1 should still have 1 land in graveyard
        long p1GraveyardLands = gd.playerGraveyards.get(player1.getId()).stream()
                .filter(c -> c.hasType(CardType.LAND))
                .count();
        assertThat(p1GraveyardLands).isEqualTo(1);
    }

    @Test
    @DisplayName("Only land cards are returned, not other card types")
    void onlyLandCardsAreReturned() {
        harness.addToBattlefield(player1, new FallOfTheThran());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fall of the Thran"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(1);

        // Put a mix of lands and non-lands in graveyard
        Card creature = new com.github.laxika.magicalvibes.cards.g.GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(new Plains(), creature)));
        harness.setGraveyard(player2, new ArrayList<>());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter II triggers
        harness.passBothPriorities(); // resolve chapter II

        GameData gd = harness.getGameData();

        // Only the land should have been returned
        long p1Lands = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .count();
        assertThat(p1Lands).isEqualTo(1);

        // Creature should still be in graveyard
        boolean creatureInGraveyard = gd.playerGraveyards.get(player1.getId()).stream()
                .anyMatch(c -> c.hasType(CardType.CREATURE));
        assertThat(creatureInGraveyard).isTrue();
    }

    // ===== Saga lifecycle =====

    @Test
    @DisplayName("Saga is sacrificed after chapter III resolves")
    void sagaSacrificedAfterChapterIII() {
        harness.addToBattlefield(player1, new FallOfTheThran());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fall of the Thran"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(2);

        harness.setGraveyard(player1, new ArrayList<>(List.of(new Plains())));
        harness.setGraveyard(player2, new ArrayList<>());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter III triggers

        // Chapter III on stack — saga should still be on battlefield
        boolean sagaOnBf = gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("Fall of the Thran"));
        assertThat(sagaOnBf).isTrue();

        harness.passBothPriorities(); // resolve chapter III

        GameData gd = harness.getGameData();

        // Saga should be sacrificed
        boolean sagaStillOnBf = gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("Fall of the Thran"));
        assertThat(sagaStillOnBf).isFalse();

        // Saga should be in graveyard
        boolean sagaInGraveyard = gd.playerGraveyards.get(player1.getId()).stream()
                .anyMatch(c -> c.getName().equals("Fall of the Thran"));
        assertThat(sagaInGraveyard).isTrue();
    }
}
