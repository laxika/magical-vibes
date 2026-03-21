package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.PreventTargetUntapWhileSourceOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCreaturesToOwnersHandEffect;
import com.github.laxika.magicalvibes.model.effect.TapTargetPermanentEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TimeOfIceTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Chapter I has tap and prevent-untap-while-on-battlefield effects")
    void chapterIHasCorrectEffects() {
        TimeOfIce card = new TimeOfIce();

        var effects = card.getEffects(EffectSlot.SAGA_CHAPTER_I);
        assertThat(effects).hasSize(2);
        assertThat(effects.get(0)).isInstanceOf(TapTargetPermanentEffect.class);
        assertThat(effects.get(1)).isInstanceOf(PreventTargetUntapWhileSourceOnBattlefieldEffect.class);
    }

    @Test
    @DisplayName("Chapter II has tap and prevent-untap-while-on-battlefield effects")
    void chapterIIHasCorrectEffects() {
        TimeOfIce card = new TimeOfIce();

        var effects = card.getEffects(EffectSlot.SAGA_CHAPTER_II);
        assertThat(effects).hasSize(2);
        assertThat(effects.get(0)).isInstanceOf(TapTargetPermanentEffect.class);
        assertThat(effects.get(1)).isInstanceOf(PreventTargetUntapWhileSourceOnBattlefieldEffect.class);
    }

    @Test
    @DisplayName("Chapter III has return tapped creatures effect")
    void chapterIIIHasCorrectEffects() {
        TimeOfIce card = new TimeOfIce();

        var effects = card.getEffects(EffectSlot.SAGA_CHAPTER_III);
        assertThat(effects).hasSize(1);
        assertThat(effects.getFirst()).isInstanceOf(ReturnCreaturesToOwnersHandEffect.class);
    }

    @Test
    @DisplayName("Chapters I and II have saga chapter target filters")
    void chaptersHaveTargetFilters() {
        TimeOfIce card = new TimeOfIce();

        assertThat(card.getSagaChapterTargetFilters(EffectSlot.SAGA_CHAPTER_I)).isNotEmpty();
        assertThat(card.getSagaChapterTargetFilters(EffectSlot.SAGA_CHAPTER_II)).isNotEmpty();
        assertThat(card.getSagaChapterTargetFilters(EffectSlot.SAGA_CHAPTER_III)).isEmpty();
    }

    // ===== Chapter I: tap target creature an opponent controls =====

    @Test
    @DisplayName("ETB triggers chapter I which awaits creature target selection")
    void etbTriggersChapterITargetSelection() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TimeOfIce()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers

        // Saga should be on battlefield with 1 lore counter
        Permanent saga = findSaga(player1);
        assertThat(saga).isNotNull();
        assertThat(saga.getLoreCounters()).isEqualTo(1);

        // Chapter I requires targeting — should be awaiting input
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    @DisplayName("Chapter I taps target opponent creature and prevents untap")
    void chapterITapsAndPreventsUntap() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TimeOfIce()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers, awaits target

        Permanent bears = findCreature(player2, "Grizzly Bears");
        assertThat(bears).isNotNull();

        // Choose opponent's creature as target
        harness.handlePermanentChosen(player1, bears.getId());

        // Chapter I ability should be on the stack
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getDescription().contains("chapter I"));

        harness.passBothPriorities(); // resolve chapter I

        // Creature should be tapped
        assertThat(bears.isTapped()).isTrue();

        // Creature should have untap prevention from saga
        assertThat(bears.getUntapPreventedWhileSourceOnBattlefieldIds()).isNotEmpty();
    }

    @Test
    @DisplayName("Chapter I only allows targeting opponent's creatures")
    void chapterIOnlyTargetsOpponentCreatures() {
        // Put creatures on both sides
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TimeOfIce()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers

        // Should be awaiting target selection
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        // The valid choices should NOT include controller's own creatures
        Permanent ownBears = findCreature(player1, "Grizzly Bears");
        Permanent oppBears = findCreature(player2, "Grizzly Bears");

        assertThat(gd.interaction.permanentChoice().validIds()).contains(oppBears.getId());
        assertThat(gd.interaction.permanentChoice().validIds()).doesNotContain(ownBears.getId());
    }

    @Test
    @DisplayName("Chapter I with no opponent creatures has no valid targets")
    void chapterINoOpponentCreaturesSkipsTargeting() {
        // Only controller has a creature
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TimeOfIce()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers

        // No valid targets → chapter I should be on the stack with no target
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getDescription().contains("chapter I")
                && e.getTargetId() == null);
    }

    // ===== Untap prevention while saga is on battlefield =====

    @Test
    @DisplayName("Locked creature does not untap during controller's untap step while saga is on battlefield")
    void lockedCreatureDoesNotUntapWhileSagaExists() {
        Permanent saga = addSagaWithLoreCounter(player1, 0);
        Permanent bears = addReadyCreature(player2);

        // Simulate chapter I resolution: tap creature and add untap lock
        bears.tap();
        bears.getUntapPreventedWhileSourceOnBattlefieldIds().add(saga.getId());

        // Advance to player2's turn — their creature should NOT untap
        advanceToNextTurn(player1);

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Locked creature untaps when saga leaves the battlefield")
    void lockedCreatureUntapsWhenSagaRemoved() {
        Permanent saga = addSagaWithLoreCounter(player1, 0);
        Permanent bears = addReadyCreature(player2);

        // Simulate chapter I resolution
        bears.tap();
        bears.getUntapPreventedWhileSourceOnBattlefieldIds().add(saga.getId());

        // Remove saga from the battlefield
        gd.playerBattlefields.get(player1.getId()).remove(saga);

        // Advance to player2's turn — creature should untap (saga gone)
        advanceToNextTurn(player1);

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Locked creature remains locked across multiple turns while saga is on battlefield")
    void lockPersistsAcrossMultipleTurns() {
        Permanent saga = addSagaWithLoreCounter(player1, 0);
        Permanent bears = addReadyCreature(player2);

        // Simulate chapter I resolution
        bears.tap();
        bears.getUntapPreventedWhileSourceOnBattlefieldIds().add(saga.getId());

        // Turn 1: player2's turn — creature stays tapped
        advanceToNextTurn(player1);
        assertThat(bears.isTapped()).isTrue();

        // Turn 2: player1's turn
        advanceToNextTurn(player2);

        // Turn 3: player2's turn — creature STILL stays tapped
        advanceToNextTurn(player1);
        assertThat(bears.isTapped()).isTrue();
    }

    // ===== Chapter II: same as chapter I =====

    @Test
    @DisplayName("Chapter II triggers and taps a second opponent creature")
    void chapterIITapsAnotherCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new TimeOfIce());

        Permanent saga = findSaga(player1);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(1);

        // Advance to precombat main to trigger chapter II
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to precombat main → chapter II triggers

        assertThat(saga.getLoreCounters()).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        // Choose the first untapped opponent creature
        Permanent target = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears") && !p.isTapped())
                .findFirst().orElse(null);
        assertThat(target).isNotNull();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities(); // resolve chapter II

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getUntapPreventedWhileSourceOnBattlefieldIds()).isNotEmpty();
    }

    // ===== Chapter III: return all tapped creatures =====

    @Test
    @DisplayName("Chapter III returns all tapped creatures to owners' hands")
    void chapterIIIReturnsAllTappedCreatures() {
        harness.addToBattlefield(player1, new TimeOfIce());
        Permanent p1Bears = addReadyCreature(player1);
        Permanent p2Bears = addReadyCreature(player2);

        // Tap both creatures
        p1Bears.tap();
        p2Bears.tap();

        Permanent saga = findSaga(player1);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(2);

        // Advance to precombat main to trigger chapter III
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to precombat main → chapter III triggers
        harness.passBothPriorities(); // resolve chapter III

        // Both tapped creatures should be returned to their owners' hands
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"))).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"))).isFalse();

        // Cards should be in their owners' hands
        assertThat(gd.playerHands.get(player1.getId()).stream()
                .anyMatch(c -> c.getName().equals("Grizzly Bears"))).isTrue();
        assertThat(gd.playerHands.get(player2.getId()).stream()
                .anyMatch(c -> c.getName().equals("Grizzly Bears"))).isTrue();
    }

    @Test
    @DisplayName("Chapter III does not return untapped creatures")
    void chapterIIIDoesNotReturnUntappedCreatures() {
        harness.addToBattlefield(player1, new TimeOfIce());
        Permanent tappedBears = addReadyCreature(player2);
        Permanent untappedBears = addReadyCreature(player2);

        // Only tap one creature
        tappedBears.tap();

        Permanent saga = findSaga(player1);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to precombat main → chapter III triggers
        harness.passBothPriorities(); // resolve chapter III

        // Only tapped creature should be gone; untapped creature stays
        long remainingBears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears")).count();
        assertThat(remainingBears).isEqualTo(1);
    }

    // ===== Saga lifecycle =====

    @Test
    @DisplayName("Saga is sacrificed after chapter III resolves")
    void sagaSacrificedAfterChapterIII() {
        harness.addToBattlefield(player1, new TimeOfIce());

        Permanent saga = findSaga(player1);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter III triggers
        harness.passBothPriorities(); // resolve chapter III

        // Saga should be sacrificed
        boolean sagaOnBf = gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("Time of Ice"));
        assertThat(sagaOnBf).isFalse();

        // Saga should be in graveyard
        boolean sagaInGy = gd.playerGraveyards.get(player1.getId()).stream()
                .anyMatch(c -> c.getName().equals("Time of Ice"));
        assertThat(sagaInGy).isTrue();
    }

    // ===== Helpers =====

    private Permanent findSaga(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Time of Ice"))
                .findFirst().orElse(null);
    }

    private Permanent findCreature(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElse(null);
    }

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addSagaWithLoreCounter(Player player, int loreCounters) {
        Permanent saga = new Permanent(new TimeOfIce());
        saga.setLoreCounters(loreCounters);
        gd.playerBattlefields.get(player.getId()).add(saga);
        return saga;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // END_STEP -> CLEANUP
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // CLEANUP -> next turn (advanceTurn)
    }
}
