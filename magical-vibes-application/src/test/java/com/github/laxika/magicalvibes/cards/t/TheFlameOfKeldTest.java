package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BalothGorger;
import com.github.laxika.magicalvibes.cards.g.GhituChronicler;
import com.github.laxika.magicalvibes.cards.a.AncientAnimus;
import com.github.laxika.magicalvibes.cards.p.PaintersServant;
import com.github.laxika.magicalvibes.cards.w.WizardsLightning;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.CardColor;
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

@CardUsed({TheFlameOfKeld.class, BalothGorger.class, WizardsLightning.class, GhituChronicler.class, PaintersServant.class, AncientAnimus.class})
class TheFlameOfKeldTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I discards controller's entire hand")
    void chapterIDiscardsHand() {
        harness.setHand(player2, List.of(new BalothGorger()));
        harness.setHand(player1, List.of(new TheFlameOfKeld(), new BalothGorger(), new WizardsLightning()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers

        // Chapter I ability on stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getDescription()).contains("chapter I");

        // Hand should still have the 2 remaining cards (BalothGorger and WizardsLightning)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        harness.passBothPriorities(); // resolve chapter I (discard hand)

        // Hand should be empty
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        // Cards should be in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        harness.assertInHand(player2, "Baloth Gorger");
    }

    @Test
    @DisplayName("Chapter I with empty hand does nothing")
    void chapterIWithEmptyHand() {
        harness.castFromHand(player1, new TheFlameOfKeld(), "{1}{R}");
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers

        // Hand is now empty (spell was cast from hand)
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities(); // resolve chapter I

        // Hand is still empty, no errors
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Chapter II draws two cards")
    void chapterIIDrawsTwoCards() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheFlameOfKeld());
        saga.setCounterCount(CounterType.LORE, 1);

        // Clear the hand
        harness.setHand(player1, List.of());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to precombat main → chapter II triggers

        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(2);
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getDescription().contains("chapter II"));

        harness.passBothPriorities(); // resolve chapter II

        // forceStep does not perform the draw-step action.
        assertThat(gd.playerDecks.get(player1.getId()).size()).isEqualTo(deckSizeBefore - 2);
    }

    @Test
    @DisplayName("Chapter III also boosts damage to its controller")
    void chapterIIISetsRedDamageBonus() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheFlameOfKeld());
        saga.setCounterCount(CounterType.LORE, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to precombat main → chapter III triggers
        harness.passBothPriorities(); // resolve chapter III

        harness.setHand(player1, List.of(new WizardsLightning()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, player1.getId());
        harness.assertLife(player1, 15);
    }

    @Test
    @DisplayName("Chapter III boosts red spell damage by 2")
    void chapterIIIBoostsRedSpellDamage() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheFlameOfKeld());
        saga.setCounterCount(CounterType.LORE, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to precombat main → chapter III triggers
        harness.passBothPriorities(); // resolve chapter III

        // Now cast a Wizard's Lightning targeting player2 — should deal 3 + 2 = 5 damage
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new WizardsLightning()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15); // 20 - 5 = 15
    }

    @Test
    @DisplayName("Chapter III does not boost non-red source damage")
    void chapterIIIDoesNotBoostNonRedDamage() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheFlameOfKeld());
        saga.setCounterCount(CounterType.LORE, 2);

        // Add a green creature
        harness.addToBattlefield(player1, new BalothGorger());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to precombat main → chapter III triggers
        harness.passBothPriorities(); // resolve chapter III

        // Green creature combat damage should NOT be boosted
        Permanent bears = findPermanent(player1, "Baloth Gorger");
        bears.setAttacking(true);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Chapter III boost applies to red combat damage")
    void chapterIIIBoostsRedCombatDamage() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheFlameOfKeld());
        saga.setCounterCount(CounterType.LORE, 2);

        Permanent redPerm = harness.addToBattlefieldAndReturn(player1, new GhituChronicler());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to precombat main → chapter III triggers
        harness.passBothPriorities(); // resolve chapter III

        redPerm.setAttacking(true);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Chapter III boost does not apply to green combat damage")
    void chapterIIIDoesNotBoostGreenCombatDamage() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheFlameOfKeld());
        saga.setCounterCount(CounterType.LORE, 2);

        // Add a green creature (Baloth Gorger)
        harness.addToBattlefield(player1, new BalothGorger());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to precombat main → chapter III triggers
        harness.passBothPriorities(); // resolve chapter III

        Permanent bears = findPermanent(player1, "Baloth Gorger");

        bears.setAttacking(true);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Saga is sacrificed after chapter III resolves")
    void sagaSacrificedAfterChapterIII() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheFlameOfKeld());
        saga.setCounterCount(CounterType.LORE, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter III triggers
        harness.assertOnBattlefield(player1, "The Flame of Keld");
        harness.passBothPriorities(); // resolve chapter III

        harness.assertNotOnBattlefield(player1, "The Flame of Keld");
        harness.assertInGraveyard(player1, "The Flame of Keld");
    }

    private void resolveChapterIII() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheFlameOfKeld());
        saga.setCounterCount(CounterType.LORE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Chapter III boosts damage to creatures")
    void boostsDamageToPermanents() {
        resolveChapterIII();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GhituChronicler());
        harness.setHand(player1, List.of(new WizardsLightning()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(target.getMarkedDamage()).isEqualTo(5);
        harness.assertInGraveyard(player2, "Ghitu Chronicler");
    }

    @Test
    @DisplayName("Chapter III does not boost opposing red sources")
    void doesNotBoostOpposingSources() {
        resolveChapterIII();
        harness.setHand(player2, List.of(new WizardsLightning()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Chapter III expires at the end of the turn")
    void bonusExpiresAtEndOfTurn() {
        resolveChapterIII();
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new WizardsLightning()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.ensurePriority(player1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Chapter III boosts combat damage from a creature made red")
    void boostsCombatDamageFromSourceMadeRed() {
        resolveChapterIII();
        Permanent painter = harness.addToBattlefieldAndReturn(player1, new PaintersServant());
        painter.setChosenColor(CardColor.RED);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BalothGorger());
        bears.setAttacking(true);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Chapter III boosts fight damage from a creature made red")
    void boostsFightDamageFromSourceMadeRed() {
        resolveChapterIII();
        Permanent painter = harness.addToBattlefieldAndReturn(player1, new PaintersServant());
        painter.setChosenColor(CardColor.RED);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BalothGorger());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GhituChronicler());
        harness.setHand(player1, List.of(new AncientAnimus()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, List.of(bears.getId(), target.getId()));
        harness.assertInGraveyard(player2, "Ghitu Chronicler");
        assertThat(target.getMarkedDamage()).isEqualTo(6);
        assertThat(bears.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Two chapter III effects each add two damage")
    void multipleChapterIIIEffectsStack() {
        resolveChapterIII();
        resolveChapterIII();
        harness.setHand(player1, List.of(new WizardsLightning()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player2, 13);
    }
}
