package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasGreatestPowerAmongControlledCreaturesPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TriumphOfGerrardTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Chapter I has +1/+1 counter effect")
    void chapterIHasCorrectEffects() {
        TriumphOfGerrard card = new TriumphOfGerrard();

        var effects = card.getEffects(EffectSlot.SAGA_CHAPTER_I);
        assertThat(effects).hasSize(1);
        assertThat(effects.getFirst()).isInstanceOf(PutPlusOnePlusOneCounterOnTargetCreatureEffect.class);
        PutPlusOnePlusOneCounterOnTargetCreatureEffect counterEffect =
                (PutPlusOnePlusOneCounterOnTargetCreatureEffect) effects.getFirst();
        assertThat(counterEffect.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Chapter II has +1/+1 counter effect")
    void chapterIIHasCorrectEffects() {
        TriumphOfGerrard card = new TriumphOfGerrard();

        var effects = card.getEffects(EffectSlot.SAGA_CHAPTER_II);
        assertThat(effects).hasSize(1);
        assertThat(effects.getFirst()).isInstanceOf(PutPlusOnePlusOneCounterOnTargetCreatureEffect.class);
    }

    @Test
    @DisplayName("Chapter III grants flying, first strike, and lifelink")
    void chapterIIIHasCorrectEffects() {
        TriumphOfGerrard card = new TriumphOfGerrard();

        var effects = card.getEffects(EffectSlot.SAGA_CHAPTER_III);
        assertThat(effects).hasSize(1);
        assertThat(effects.getFirst()).isInstanceOf(GrantKeywordEffect.class);
        GrantKeywordEffect grantEffect = (GrantKeywordEffect) effects.getFirst();
        assertThat(grantEffect.keywords()).containsExactlyInAnyOrder(
                Keyword.FLYING, Keyword.FIRST_STRIKE, Keyword.LIFELINK);
        assertThat(grantEffect.scope()).isEqualTo(GrantScope.TARGET);
    }

    @Test
    @DisplayName("Chapter effects carry greatest-power target predicate")
    void effectsHaveGreatestPowerTargetPredicate() {
        TriumphOfGerrard card = new TriumphOfGerrard();

        assertThat(card.getEffects(EffectSlot.SAGA_CHAPTER_I).getFirst().targetPredicate())
                .isInstanceOf(PermanentHasGreatestPowerAmongControlledCreaturesPredicate.class);
        assertThat(card.getEffects(EffectSlot.SAGA_CHAPTER_II).getFirst().targetPredicate())
                .isInstanceOf(PermanentHasGreatestPowerAmongControlledCreaturesPredicate.class);
        assertThat(card.getEffects(EffectSlot.SAGA_CHAPTER_III).getFirst().targetPredicate())
                .isInstanceOf(PermanentHasGreatestPowerAmongControlledCreaturesPredicate.class);
    }

    // ===== Chapter I: targeting + resolution =====

    @Test
    @DisplayName("ETB triggers chapter I which awaits target selection for creature with greatest power")
    void etbTriggersChapterITargetSelection() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TriumphOfGerrard()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers

        GameData gd = harness.getGameData();

        // Saga should be on battlefield with 1 lore counter
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Triumph of Gerrard"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        assertThat(saga.getLoreCounters()).isEqualTo(1);

        // Chapter I requires targeting — should be awaiting input
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    @DisplayName("Chapter I only allows targeting the creature with greatest power")
    void chapterIOnlyTargetsGreatestPowerCreature() {
        // Hill Giant (3/3) and Grizzly Bears (2/2) — only Hill Giant should be targetable
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TriumphOfGerrard()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        // The valid permanent choices should only include Hill Giant (greatest power)
        Permanent hillGiant = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Hill Giant"))
                .findFirst().orElse(null);
        assertThat(hillGiant).isNotNull();

        // Valid choices should contain Hill Giant but not Grizzly Bears
        assertThat(gd.interaction.permanentChoice().validIds()).contains(hillGiant.getId());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElse(null);
        assertThat(bears).isNotNull();
        assertThat(gd.interaction.permanentChoice().validIds()).doesNotContain(bears.getId());
    }

    @Test
    @DisplayName("Chapter I puts +1/+1 counter on chosen creature with greatest power")
    void chapterIPutsCounterOnGreatestPowerCreature() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TriumphOfGerrard()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers

        GameData gd = harness.getGameData();
        Permanent hillGiant = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Hill Giant"))
                .findFirst().orElse(null);
        assertThat(hillGiant).isNotNull();

        harness.handlePermanentChosen(player1, hillGiant.getId());
        harness.passBothPriorities(); // resolve chapter I

        gd = harness.getGameData();
        hillGiant = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Hill Giant"))
                .findFirst().orElse(null);
        assertThat(hillGiant).isNotNull();
        assertThat(hillGiant.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Chapter I allows choice among tied greatest-power creatures")
    void chapterIAllowsTiedCreatures() {
        // Two Grizzly Bears (both 2/2) — both should be targetable
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TriumphOfGerrard()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        // Both creatures should be valid choices (tied for greatest power)
        List<Permanent> bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .toList();
        assertThat(bears).hasSize(2);
        assertThat(gd.interaction.permanentChoice().validIds())
                .contains(bears.get(0).getId(), bears.get(1).getId());
    }

    @Test
    @DisplayName("Chapter I does not target opponent's creatures even if they have greater power")
    void chapterIDoesNotTargetOpponentCreatures() {
        // Player1 has Grizzly Bears (2/2), Player2 has Hill Giant (3/3)
        // Only player1's Grizzly Bears should be targetable
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new TriumphOfGerrard()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        Permanent opponentGiant = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Hill Giant"))
                .findFirst().orElse(null);
        assertThat(opponentGiant).isNotNull();
        assertThat(gd.interaction.permanentChoice().validIds()).doesNotContain(opponentGiant.getId());

        Permanent ownBears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElse(null);
        assertThat(ownBears).isNotNull();
        assertThat(gd.interaction.permanentChoice().validIds()).contains(ownBears.getId());
    }

    @Test
    @DisplayName("Chapter I with no creatures pushes ability with no target")
    void chapterINoCreaturesSkipsTargeting() {
        harness.setHand(player1, List.of(new TriumphOfGerrard()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers

        GameData gd = harness.getGameData();

        // No creatures → chapter I should be on the stack with no target
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getDescription().contains("chapter I"));
    }

    // ===== Chapter II =====

    @Test
    @DisplayName("Chapter II puts a +1/+1 counter on creature with greatest power")
    void chapterIIPutsCounter() {
        harness.addToBattlefield(player1, new TriumphOfGerrard());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Triumph of Gerrard"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to precombat main → chapter II triggers

        GameData gd = harness.getGameData();
        assertThat(saga.getLoreCounters()).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        Permanent hillGiant = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Hill Giant"))
                .findFirst().orElse(null);
        assertThat(hillGiant).isNotNull();

        // Only Hill Giant (3/3) should be targetable, not Grizzly Bears (2/2)
        assertThat(gd.interaction.permanentChoice().validIds()).contains(hillGiant.getId());

        harness.handlePermanentChosen(player1, hillGiant.getId());
        harness.passBothPriorities(); // resolve chapter II

        gd = harness.getGameData();
        hillGiant = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Hill Giant"))
                .findFirst().orElse(null);
        assertThat(hillGiant).isNotNull();
        assertThat(hillGiant.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    // ===== Chapter III: grant keywords =====

    @Test
    @DisplayName("Chapter III grants flying, first strike, and lifelink to greatest power creature")
    void chapterIIIGrantsKeywords() {
        harness.addToBattlefield(player1, new TriumphOfGerrard());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Triumph of Gerrard"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to precombat main → chapter III triggers

        GameData gd = harness.getGameData();
        assertThat(saga.getLoreCounters()).isEqualTo(3);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        Permanent hillGiant = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Hill Giant"))
                .findFirst().orElse(null);
        assertThat(hillGiant).isNotNull();

        harness.handlePermanentChosen(player1, hillGiant.getId());
        harness.passBothPriorities(); // resolve chapter III

        gd = harness.getGameData();
        hillGiant = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Hill Giant"))
                .findFirst().orElse(null);
        assertThat(hillGiant).isNotNull();
        assertThat(hillGiant.getGrantedKeywords()).contains(
                Keyword.FLYING, Keyword.FIRST_STRIKE, Keyword.LIFELINK);
    }

    // ===== Saga lifecycle =====

    @Test
    @DisplayName("Saga is sacrificed after chapter III resolves")
    void sagaSacrificedAfterChapterIII() {
        harness.addToBattlefield(player1, new TriumphOfGerrard());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Triumph of Gerrard"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter III triggers

        GameData gd = harness.getGameData();
        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElse(null);
        assertThat(bears).isNotNull();

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities(); // resolve chapter III

        gd = harness.getGameData();

        // Saga should be sacrificed
        boolean sagaOnBf = gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("Triumph of Gerrard"));
        assertThat(sagaOnBf).isFalse();

        // Saga should be in graveyard
        boolean sagaInGy = gd.playerGraveyards.get(player1.getId()).stream()
                .anyMatch(c -> c.getName().equals("Triumph of Gerrard"));
        assertThat(sagaInGy).isTrue();
    }
}
