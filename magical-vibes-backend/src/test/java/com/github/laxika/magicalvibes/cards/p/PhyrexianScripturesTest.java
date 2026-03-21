package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AddCardTypeToTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllOpponentsGraveyardsEffect;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PhyrexianScripturesTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Chapter I has +1/+1 counter and artifact type grant effects")
    void chapterIHasCorrectEffects() {
        PhyrexianScriptures card = new PhyrexianScriptures();

        var effects = card.getEffects(EffectSlot.SAGA_CHAPTER_I);
        assertThat(effects).hasSize(2);
        assertThat(effects.get(0)).isInstanceOf(PutPlusOnePlusOneCounterOnTargetCreatureEffect.class);
        PutPlusOnePlusOneCounterOnTargetCreatureEffect counterEffect =
                (PutPlusOnePlusOneCounterOnTargetCreatureEffect) effects.get(0);
        assertThat(counterEffect.count()).isEqualTo(1);

        assertThat(effects.get(1)).isInstanceOf(AddCardTypeToTargetPermanentEffect.class);
        AddCardTypeToTargetPermanentEffect typeEffect = (AddCardTypeToTargetPermanentEffect) effects.get(1);
        assertThat(typeEffect.cardType()).isEqualTo(CardType.ARTIFACT);
        assertThat(typeEffect.persistent()).isTrue();
    }

    @Test
    @DisplayName("Chapter II has destroy all nonartifact creatures effect")
    void chapterIIHasCorrectEffects() {
        PhyrexianScriptures card = new PhyrexianScriptures();

        var effects = card.getEffects(EffectSlot.SAGA_CHAPTER_II);
        assertThat(effects).hasSize(1);
        assertThat(effects.getFirst()).isInstanceOf(DestroyAllPermanentsEffect.class);
    }

    @Test
    @DisplayName("Chapter III has exile all opponents' graveyards effect")
    void chapterIIIHasCorrectEffects() {
        PhyrexianScriptures card = new PhyrexianScriptures();

        var effects = card.getEffects(EffectSlot.SAGA_CHAPTER_III);
        assertThat(effects).hasSize(1);
        assertThat(effects.getFirst()).isInstanceOf(ExileAllOpponentsGraveyardsEffect.class);
    }

    // ===== Chapter I: targeting + resolution =====

    @Test
    @DisplayName("ETB triggers chapter I which awaits creature target selection")
    void etbTriggersChapterITargetSelection() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new PhyrexianScriptures()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers

        GameData gd = harness.getGameData();

        // Saga should be on battlefield with 1 lore counter
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Scriptures"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        assertThat(saga.getLoreCounters()).isEqualTo(1);

        // Chapter I requires targeting — should be awaiting input
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    @DisplayName("Chapter I puts +1/+1 counter on chosen creature and makes it an artifact permanently")
    void chapterIResolvesCounterAndArtifactType() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new PhyrexianScriptures()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers, awaits target

        GameData gd = harness.getGameData();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElse(null);
        assertThat(bears).isNotNull();

        // Choose Grizzly Bears as target
        harness.handlePermanentChosen(player1, bears.getId());

        // Chapter I ability should now be on the stack
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getDescription().contains("chapter I"));

        harness.passBothPriorities(); // resolve chapter I

        gd = harness.getGameData();

        // Bears should have +1/+1 counter
        bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElse(null);
        assertThat(bears).isNotNull();
        assertThat(bears.getPlusOnePlusOneCounters()).isEqualTo(1);

        // Bears should be permanently an artifact
        assertThat(bears.getPersistentGrantedCardTypes()).contains(CardType.ARTIFACT);
        assertThat(gqs.isArtifact(bears)).isTrue();
    }

    @Test
    @DisplayName("Chapter I artifact type persists across turn resets")
    void chapterIArtifactTypeSurvivesTurnReset() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new PhyrexianScriptures()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers

        GameData gd = harness.getGameData();
        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElse(null);
        assertThat(bears).isNotNull();

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities(); // resolve chapter I

        // Simulate turn reset (resetModifiers clears transient but not persistent)
        bears.resetModifiers();

        // Persistent card type should survive
        assertThat(bears.getPersistentGrantedCardTypes()).contains(CardType.ARTIFACT);
        assertThat(gqs.isArtifact(bears)).isTrue();
    }

    @Test
    @DisplayName("Chapter I with no creatures on battlefield pushes ability with no target")
    void chapterINoCreaturesSkipsTargeting() {
        harness.setHand(player1, List.of(new PhyrexianScriptures()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers

        GameData gd = harness.getGameData();

        // No creatures → chapter I should be on the stack with no target (no awaiting input)
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getDescription().contains("chapter I"));
    }

    @Test
    @DisplayName("Chapter I skip option — choosing self as target skips effect")
    void chapterISkipByChoosingPlayer() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PhyrexianScriptures()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        // Choose self (skip)
        harness.handlePermanentChosen(player1, player1.getId());

        // Chapter I ability on stack with no target
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getDescription().contains("chapter I")
                && e.getTargetId() == null);

        harness.passBothPriorities(); // resolve chapter I

        gd = harness.getGameData();

        // Opponent's bears should NOT be an artifact
        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElse(null);
        assertThat(bears).isNotNull();
        assertThat(bears.getPlusOnePlusOneCounters()).isZero();
        assertThat(bears.getPersistentGrantedCardTypes()).doesNotContain(CardType.ARTIFACT);
    }

    // ===== Chapter II: destroy nonartifact creatures =====

    @Test
    @DisplayName("Chapter II destroys all nonartifact creatures")
    void chapterIIDestroysNonartifactCreatures() {
        harness.addToBattlefield(player1, new PhyrexianScriptures());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Scriptures"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to precombat main → chapter II triggers

        GameData gd = harness.getGameData();
        assertThat(saga.getLoreCounters()).isEqualTo(2);

        harness.passBothPriorities(); // resolve chapter II

        gd = harness.getGameData();

        // Both nonartifact creatures should be destroyed
        long p1Creatures = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears")).count();
        long p2Creatures = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears")).count();
        assertThat(p1Creatures).isZero();
        assertThat(p2Creatures).isZero();
    }

    @Test
    @DisplayName("Chapter II does not destroy artifact creatures")
    void chapterIIDoesNotDestroyArtifactCreatures() {
        harness.addToBattlefield(player1, new PhyrexianScriptures());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Scriptures"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(1);

        // Make the Grizzly Bears an artifact permanently (simulating chapter I effect)
        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElse(null);
        assertThat(bears).isNotNull();
        bears.getPersistentGrantedCardTypes().add(CardType.ARTIFACT);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to precombat main → chapter II triggers
        harness.passBothPriorities(); // resolve chapter II

        GameData gd = harness.getGameData();

        // Bears (now an artifact) should survive
        boolean bearsSurvive = gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(bearsSurvive).isTrue();
    }

    // ===== Chapter III: exile opponents' graveyards =====

    @Test
    @DisplayName("Chapter III exiles all opponents' graveyards but not controller's")
    void chapterIIIExilesOpponentGraveyardsOnly() {
        harness.addToBattlefield(player1, new PhyrexianScriptures());

        // Put some cards in both graveyards
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Scriptures"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to precombat main → chapter III triggers
        harness.passBothPriorities(); // resolve chapter III

        GameData gd = harness.getGameData();

        // Opponent's graveyard should be exiled
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        // Opponent's cards should be in exile
        assertThat(gd.playerExiledCards.get(player2.getId())).hasSize(2);

        // Controller's graveyard should contain original card + sacrificed Saga
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Grizzly Bears")).count()).isEqualTo(1);
    }

    // ===== Saga lifecycle =====

    @Test
    @DisplayName("Saga is sacrificed after chapter III resolves")
    void sagaSacrificedAfterChapterIII() {
        harness.addToBattlefield(player1, new PhyrexianScriptures());

        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Scriptures"))
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
        boolean sagaOnBf = gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("Phyrexian Scriptures"));
        assertThat(sagaOnBf).isFalse();

        // Saga should be in graveyard
        boolean sagaInGy = gd.playerGraveyards.get(player1.getId()).stream()
                .anyMatch(c -> c.getName().equals("Phyrexian Scriptures"));
        assertThat(sagaInGy).isTrue();
    }
}
