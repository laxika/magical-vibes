package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AzureDrake;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.CounterType;

class TheFirstEruptionTest extends BaseCardTest {

    // ===== ETB: first lore counter and chapter I triggers =====

    @Test
    @DisplayName("Casting The First Eruption adds a lore counter and triggers chapter I")
    void castingAddsLoreCounterAndTriggersChapterI() {
        harness.setHand(player1, List.of(new TheFirstEruption()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve the enchantment spell

        GameData gd = harness.getGameData();

        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("The First Eruption"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getDescription()).contains("chapter I");
    }

    // ===== Chapter I: damage creatures without flying =====

    @Test
    @DisplayName("Chapter I deals 1 damage to creature without flying")
    void chapterIDamagesNonFlyingCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // 2/2, no flying
        Permanent bear = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElse(null);
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
        harness.addToBattlefield(player1, new AzureDrake()); // 2/4, has flying
        Permanent drake = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Azure Drake"))
                .findFirst().orElse(null);
        assertThat(drake).isNotNull();
        assertThat(gqs.hasKeyword(gd, drake, Keyword.FLYING)).isTrue();

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
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bear = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElse(null);
        assertThat(bear).isNotNull();

        harness.setHand(player1, List.of(new TheFirstEruption()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment
        harness.passBothPriorities(); // resolve chapter I

        assertThat(bear.getMarkedDamage()).isEqualTo(1);
    }

    // ===== Chapter II: add mana =====

    @Test
    @DisplayName("Chapter II adds {R}{R} to controller's mana pool")
    void chapterIIAddsTwoRedMana() {
        harness.addToBattlefield(player1, new TheFirstEruption());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("The First Eruption"))
                .findFirst().orElse(null);
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

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(redBefore + 2);
    }

    // ===== Chapter III: sacrifice Mountain then deal 3 damage =====

    @Test
    @DisplayName("Chapter III prompts to sacrifice a Mountain and deals 3 damage to all creatures")
    void chapterIIISacrificesMountainAndDealsDamage() {
        harness.addToBattlefield(player1, new TheFirstEruption());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("The First Eruption"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setCounterCount(CounterType.LORE, 2);

        harness.addToBattlefield(player1, new Mountain());
        Permanent mountain = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Mountain"))
                .findFirst().orElse(null);
        assertThat(mountain).isNotNull();

        // Add creatures to both sides
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent ownBear = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElse(null);

        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent oppBear = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElse(null);

        // Also add a flying creature — chapter III should damage it (unlike chapter I)
        harness.addToBattlefield(player1, new AzureDrake()); // 2/4 flying
        Permanent drake = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Azure Drake"))
                .findFirst().orElse(null);

        // Advance to precombat main → chapter III triggers
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(3);

        // Resolve chapter III — should prompt for Mountain sacrifice
        harness.passBothPriorities();

        // Choose the Mountain to sacrifice
        harness.handlePermanentChosen(player1, mountain.getId());

        // The "if you do" effect is now on the stack — resolve it
        harness.passBothPriorities();

        gd = harness.getGameData();

        // Mountain should be sacrificed (no longer on battlefield)
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("Mountain"))).isFalse();

        // Grizzly Bears are 2/2 — 3 damage is lethal, they should be dead
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"))).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"))).isFalse();

        // Azure Drake is 2/4 — 3 damage should leave it alive with 3 damage
        assertThat(drake.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Chapter III does nothing if controller has no Mountains")
    void chapterIIIDoesNothingWithoutMountain() {
        harness.addToBattlefield(player1, new TheFirstEruption());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("The First Eruption"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setCounterCount(CounterType.LORE, 2);

        // No Mountains — just a creature
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bear = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElse(null);

        // Advance to precombat main → chapter III triggers
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Resolve chapter III — no Mountain to sacrifice, nothing happens
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Bear should be unharmed
        assertThat(bear.getMarkedDamage()).isEqualTo(0);
    }

    // ===== Saga sacrifice SBA =====

    @Test
    @DisplayName("Saga is sacrificed after final chapter ability resolves")
    void sagaSacrificedAfterFinalChapter() {
        harness.addToBattlefield(player1, new TheFirstEruption());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("The First Eruption"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setCounterCount(CounterType.LORE, 2);

        // Need a Mountain for chapter III to sacrifice
        harness.addToBattlefield(player1, new Mountain());
        Permanent mountain = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Mountain"))
                .findFirst().orElse(null);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter III triggers

        harness.passBothPriorities(); // resolve chapter III → sacrifice Mountain prompt

        harness.handlePermanentChosen(player1, mountain.getId()); // choose Mountain

        harness.passBothPriorities(); // resolve the "if you do" damage effect

        GameData gd = harness.getGameData();

        // Saga should be sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("The First Eruption"))).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .anyMatch(c -> c.getName().equals("The First Eruption"))).isTrue();
    }
}
