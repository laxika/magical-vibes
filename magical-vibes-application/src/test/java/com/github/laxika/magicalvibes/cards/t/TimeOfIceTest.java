package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.a.AcademyDrake;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.i.InBolassClutches;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({TimeOfIce.class, AcademyDrake.class, IcyManipulator.class, InBolassClutches.class})
class TimeOfIceTest extends BaseCardTest {

    @Test
    @DisplayName("ETB triggers chapter I which awaits creature target selection")
    void etbTriggersChapterITargetSelection() {
        harness.addToBattlefield(player2, new AcademyDrake());
        harness.setHand(player1, List.of(new TimeOfIce()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers

        // Saga should be on battlefield with 1 lore counter
        Permanent saga = findSaga(player1);
        assertThat(saga).isNotNull();
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);

        // Chapter I requires targeting — should be awaiting input
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    @DisplayName("Chapter I taps target opponent creature and prevents untap")
    void chapterITapsAndPreventsUntap() {
        harness.addToBattlefield(player2, new AcademyDrake());
        harness.setHand(player1, List.of(new TimeOfIce()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers, awaits target

        Permanent bears = findPermanent(player2, "Academy Drake");
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
        harness.addToBattlefield(player1, new AcademyDrake());
        harness.addToBattlefield(player2, new AcademyDrake());
        harness.setHand(player1, List.of(new TimeOfIce()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers

        // Should be awaiting target selection
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        // The valid choices should NOT include controller's own creatures
        Permanent ownBears = findPermanent(player1, "Academy Drake");
        Permanent oppBears = findPermanent(player2, "Academy Drake");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds()).contains(oppBears.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds()).doesNotContain(ownBears.getId());
    }

    @Test
    @DisplayName("Chapter I with no opponent creatures has no valid targets")
    void chapterINoOpponentCreaturesSkipsTargeting() {
        // Only controller has a creature
        harness.addToBattlefield(player1, new AcademyDrake());
        harness.setHand(player1, List.of(new TimeOfIce()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers

        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getDescription().contains("chapter I"));
    }

    @Test
    @DisplayName("Locked creature does not untap during controller's untap step while saga is on battlefield")
    void lockedCreatureDoesNotUntapWhileSagaExists() {
        Permanent saga = addSagaWithLoreCounter(player1, 0);
        Permanent bears = addCreatureReady(player2, new AcademyDrake());

        // Simulate chapter I resolution: tap creature and add untap lock
        bears.tap();
        bears.getUntapPreventedWhileSourceOnBattlefieldIds().add(saga.getId());

        // Advance to player2's turn — their creature should NOT untap
        harness.performUntapStep(player2);

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Locked creature untaps when saga leaves the battlefield")
    void lockedCreatureUntapsWhenSagaRemoved() {
        Permanent saga = addSagaWithLoreCounter(player1, 0);
        Permanent bears = addCreatureReady(player2, new AcademyDrake());

        // Simulate chapter I resolution
        bears.tap();
        bears.getUntapPreventedWhileSourceOnBattlefieldIds().add(saga.getId());

        // Remove saga from the battlefield
        gd.playerBattlefields.get(player1.getId()).remove(saga);

        // Advance to player2's turn — creature should untap (saga gone)
        harness.performUntapStep(player2);

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Locked creature remains locked across multiple turns while saga is on battlefield")
    void lockPersistsAcrossMultipleTurns() {
        Permanent saga = addSagaWithLoreCounter(player1, 0);
        Permanent bears = addCreatureReady(player2, new AcademyDrake());

        // Simulate chapter I resolution
        bears.tap();
        bears.getUntapPreventedWhileSourceOnBattlefieldIds().add(saga.getId());

        // Player2's first untap step — creature stays tapped
        harness.performUntapStep(player2);
        assertThat(bears.isTapped()).isTrue();

        // Player1's untap step
        harness.performUntapStep(player1);

        // Player2's next untap step — creature STILL stays tapped
        harness.performUntapStep(player2);
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Chapter II triggers and taps a second opponent creature")
    void chapterIITapsAnotherCreature() {
        harness.addToBattlefield(player2, new AcademyDrake());
        harness.addToBattlefield(player2, new AcademyDrake());
        harness.addToBattlefield(player1, new TimeOfIce());

        Permanent saga = findSaga(player1);
        assertThat(saga).isNotNull();
        saga.setCounterCount(CounterType.LORE, 1);

        // Advance to precombat main to trigger chapter II
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to precombat main → chapter II triggers

        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        // Choose the first untapped opponent creature
        Permanent target = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Academy Drake") && !p.isTapped())
                .findFirst().orElse(null);
        assertThat(target).isNotNull();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities(); // resolve chapter II

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getUntapPreventedWhileSourceOnBattlefieldIds()).isNotEmpty();
    }

    @Test
    @DisplayName("Chapter III returns all tapped creatures to owners' hands")
    void chapterIIIReturnsAllTappedCreatures() {
        harness.addToBattlefield(player1, new TimeOfIce());
        Permanent p1Bears = addCreatureReady(player1, new AcademyDrake());
        Permanent p2Bears = addCreatureReady(player2, new AcademyDrake());

        // Tap both creatures
        p1Bears.tap();
        p2Bears.tap();

        Permanent saga = findSaga(player1);
        assertThat(saga).isNotNull();
        saga.setCounterCount(CounterType.LORE, 2);

        // Advance to precombat main to trigger chapter III
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to precombat main → chapter III triggers
        harness.passBothPriorities(); // resolve chapter III

        // Both tapped creatures should be returned to their owners' hands
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("Academy Drake"))).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("Academy Drake"))).isFalse();

        // Cards should be in their owners' hands
        assertThat(gd.playerHands.get(player1.getId()).stream()
                .anyMatch(c -> c.getName().equals("Academy Drake"))).isTrue();
        assertThat(gd.playerHands.get(player2.getId()).stream()
                .anyMatch(c -> c.getName().equals("Academy Drake"))).isTrue();
    }

    @Test
    @DisplayName("Chapter III does not return untapped creatures")
    void chapterIIIDoesNotReturnUntappedCreatures() {
        harness.addToBattlefield(player1, new TimeOfIce());
        Permanent tappedBears = addCreatureReady(player2, new AcademyDrake());
        addCreatureReady(player2, new AcademyDrake());

        // Only tap one creature
        tappedBears.tap();

        Permanent saga = findSaga(player1);
        assertThat(saga).isNotNull();
        saga.setCounterCount(CounterType.LORE, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to precombat main → chapter III triggers
        harness.passBothPriorities(); // resolve chapter III

        // Only tapped creature should be gone; untapped creature stays
        long remainingBears = countPermanents(player2, "Academy Drake");
        assertThat(remainingBears).isEqualTo(1);
    }

    @Test
    @DisplayName("Saga is sacrificed after chapter III resolves")
    void sagaSacrificedAfterChapterIII() {
        harness.addToBattlefield(player1, new TimeOfIce());

        Permanent saga = findSaga(player1);
        assertThat(saga).isNotNull();
        saga.setCounterCount(CounterType.LORE, 2);

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

    @Test
    void chapterIExcludesNoncreaturePermanentsAndCannotBeSkipped() {
        Permanent creature = addCreatureReady(player2, new AcademyDrake());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new IcyManipulator());
        harness.setHand(player1, List.of(new TimeOfIce()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(creature.getId())
                .doesNotContain(artifact.getId(), player1.getId());
    }

    @Test
    void chapterIIExcludesNoncreaturePermanentsAndCannotBeSkipped() {
        Permanent creature = addCreatureReady(player2, new AcademyDrake());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new IcyManipulator());
        addSagaWithLoreCounter(player1, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(creature.getId())
                .doesNotContain(artifact.getId(), player1.getId());
    }

    @Test
    void creatureUntapsAfterOpponentGainsControlOfSaga() {
        Permanent creature = addCreatureReady(player2, new AcademyDrake());
        harness.setHand(player1, List.of(new TimeOfIce()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        assertThat(creature.isTapped()).isTrue();

        Permanent saga = findSaga(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new InBolassClutches()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.castEnchantment(player2, 0, saga.getId());
        harness.passBothPriorities();
        assertThat(findSaga(player2)).isSameAs(saga);

        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void chapterIIIDoesNotReturnTappedNoncreatures() {
        addSagaWithLoreCounter(player1, 2);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new IcyManipulator());
        artifact.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Icy Manipulator")).isSameAs(artifact);
        assertThat(artifact.isTapped()).isTrue();
    }

    private Permanent findSaga(Player player) {
        return findPermanent(player, "Time of Ice");
    }

    private Permanent addSagaWithLoreCounter(Player player, int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player, new TimeOfIce());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

}
