package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CloudreaderSphinx;
import com.github.laxika.magicalvibes.cards.c.CabalEvangel;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheFirstEruption.class, CloudreaderSphinx.class, CabalEvangel.class, Mountain.class})
class TheFirstEruptionTest extends BaseCardTest {

    @Test
    @DisplayName("Casting The First Eruption adds a lore counter and triggers chapter I")
    void castingAddsLoreCounterAndTriggersChapterI() {
        harness.setHand(player1, List.of(new TheFirstEruption()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve the enchantment spell

        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("The First Eruption"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getDescription()).contains("chapter I");
    }

    @Test
    @DisplayName("Chapter I deals 1 damage to creature without flying")
    void chapterIDamagesNonFlyingCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new CabalEvangel());
        assertThat(bear).isNotNull();

        harness.setHand(player1, List.of(new TheFirstEruption()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers
        harness.passBothPriorities(); // resolve chapter I ability

        assertThat(bear.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Chapter I does not damage creature with flying")
    void chapterIDoesNotDamageFlyingCreature() {
        Permanent drake = harness.addToBattlefieldAndReturn(player1, new CloudreaderSphinx());
        assertThat(drake).isNotNull();

        harness.setHand(player1, List.of(new TheFirstEruption()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers
        harness.passBothPriorities(); // resolve chapter I ability

        assertThat(drake.getMarkedDamage()).isEqualTo(0);
    }

    @Test
    @DisplayName("Chapter I damages opponent's creature without flying too")
    void chapterIDamagesOpponentNonFlyingCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new CabalEvangel());
        assertThat(bear).isNotNull();

        harness.setHand(player1, List.of(new TheFirstEruption()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment
        harness.passBothPriorities(); // resolve chapter I

        assertThat(bear.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Chapter II adds {R}{R} to controller's mana pool")
    void chapterIIAddsTwoRedMana() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheFirstEruption());
        assertThat(saga).isNotNull();
        saga.setCounterCount(CounterType.LORE, 1);

        int redBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.RED);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to precombat main → chapter II triggers

        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(2);

        // Resolve chapter II
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(redBefore + 2);
    }

    @Test
    @DisplayName("Chapter III prompts to sacrifice a Mountain and deals 3 damage to all creatures")
    void chapterIIISacrificesMountainAndDealsDamage() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheFirstEruption());
        assertThat(saga).isNotNull();
        saga.setCounterCount(CounterType.LORE, 2);

        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        assertThat(mountain).isNotNull();

        // Add creatures to both sides
        harness.addToBattlefield(player1, new CabalEvangel());

        harness.addToBattlefield(player2, new CabalEvangel());

        // Also add a flying creature — chapter III should damage it (unlike chapter I)
        Permanent drake = harness.addToBattlefieldAndReturn(player1, new CloudreaderSphinx());

        // Advance to precombat main → chapter III triggers
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(3);

        // Resolve chapter III — should prompt for Mountain sacrifice
        harness.passBothPriorities();

        // Choose the Mountain to sacrifice
        harness.handlePermanentChosen(player1, mountain.getId());

        assertThat(gd.stack).isEmpty();

        // Mountain should be sacrificed (no longer on battlefield)
        harness.assertNotOnBattlefield(player1, "Mountain");

        // Cabal Evangel are 2/2 — 3 damage is lethal, they should be dead
        harness.assertNotOnBattlefield(player1, "Cabal Evangel");
        harness.assertNotOnBattlefield(player2, "Cabal Evangel");

        // Cloudreader Sphinx is 3/4 — 3 damage should leave it alive with 3 damage
        assertThat(drake.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Chapter III does nothing if controller has no Mountains")
    void chapterIIIDoesNothingWithoutMountain() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheFirstEruption());
        assertThat(saga).isNotNull();
        saga.setCounterCount(CounterType.LORE, 2);

        // No Mountains — just a creature
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new CabalEvangel());

        // Advance to precombat main → chapter III triggers
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Resolve chapter III — no Mountain to sacrifice, nothing happens
        harness.passBothPriorities();

        // Bear should be unharmed
        assertThat(bear.getMarkedDamage()).isEqualTo(0);
    }

    @Test
    @DisplayName("Saga is sacrificed after final chapter ability resolves")
    void sagaSacrificedAfterFinalChapter() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheFirstEruption());
        assertThat(saga).isNotNull();
        saga.setCounterCount(CounterType.LORE, 2);

        // Need a Mountain for chapter III to sacrifice
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter III triggers

        harness.passBothPriorities(); // resolve chapter III → sacrifice Mountain prompt

        harness.handlePermanentChosen(player1, mountain.getId()); // choose Mountain

        assertThat(gd.stack).isEmpty();

        // Saga should be sacrificed
        harness.assertNotOnBattlefield(player1, "The First Eruption");
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .anyMatch(c -> c.getName().equals("The First Eruption"))).isTrue();
    }

    @Test
    @DisplayName("Chapter III cannot sacrifice an opponent's Mountain")
    void chapterIIICannotUseOpponentsMountain() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheFirstEruption());
        saga.setCounterCount(CounterType.LORE, 2);
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CabalEvangel());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(mountain, creature);
        assertThat(creature.getMarkedDamage()).isZero();
        harness.assertNotOnBattlefield(player1, "The First Eruption");
    }

    @Test
    @DisplayName("Chapter III sacrifices exactly one Mountain and damages creatures during resolution")
    void chapterIIISacrificesOnlyOneMountain() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheFirstEruption());
        saga.setCounterCount(CounterType.LORE, 2);
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent retained = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CloudreaderSphinx());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());

        assertThat(creature.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(retained).doesNotContain(chosen);
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "The First Eruption");
    }
}
