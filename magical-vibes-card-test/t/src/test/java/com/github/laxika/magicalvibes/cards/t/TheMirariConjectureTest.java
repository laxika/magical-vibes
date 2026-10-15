package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.r.Redirect;
import com.github.laxika.magicalvibes.cards.s.SoulSalvage;
import com.github.laxika.magicalvibes.cards.b.BalothGorger;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.Card;
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
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({TheMirariConjecture.class, Divination.class, LightningBolt.class, Shock.class,
        com.github.laxika.magicalvibes.cards.g.GrizzlyBears.class, Redirect.class, SoulSalvage.class,
        BalothGorger.class, Opt.class})
class TheMirariConjectureTest extends BaseCardTest {

    @Test
    @DisplayName("Casting The Mirari Conjecture prompts for instant target in graveyard")
    void castingPromptsForInstantGraveyardTarget() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(new TheMirariConjecture()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers

        // Should prompt for graveyard target selection (instant in graveyard)
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
    }

    @Test
    @DisplayName("Chapter I returns selected instant from graveyard to hand")
    void chapterIReturnsInstantToHand() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(new TheMirariConjecture()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers

        // Select the instant from graveyard
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));

        // Triggered ability should be on the stack
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getDescription().contains("chapter I"));

        // Resolve the chapter ability
        harness.passBothPriorities();

        // Shock should be in hand, not in graveyard
        harness.assertInHand(player1, "Shock");
        harness.assertNotInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("Chapter I skips when no instants in graveyard")
    void chapterISkipsWithNoInstants() {
        // Only a sorcery in graveyard - chapter I should skip
        Divination divination = new Divination();
        harness.setGraveyard(player1, List.of(divination));
        harness.setHand(player1, List.of(new TheMirariConjecture()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers but no valid targets

        // Should NOT prompt for graveyard choice (no instants in graveyard)
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    @DisplayName("Chapter I does not show sorceries as valid targets")
    void chapterIDoesNotTargetSorceries() {
        // Sorcery + instant in graveyard
        Divination divination = new Divination();
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(divination, shock));
        harness.setHand(player1, List.of(new TheMirariConjecture()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers

        // Should prompt for graveyard target selection
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        // Select the instant
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();

        // Only Shock returned, Divination still in graveyard
        harness.assertInHand(player1, "Shock");
        harness.assertInGraveyard(player1, "Divination");
    }

    @Test
    @DisplayName("Chapter II prompts for sorcery target in graveyard")
    void chapterIIPromptsForSorceryTarget() {
        Divination divination = new Divination();
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheMirariConjecture());
        saga.setCounterCount(CounterType.LORE, 1);
        harness.setGraveyard(player1, List.of(divination));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to precombat main → chapter II triggers

        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
    }

    @Test
    @DisplayName("Chapter II returns selected sorcery from graveyard to hand")
    void chapterIIReturnsSorceryToHand() {
        Divination divination = new Divination();
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheMirariConjecture());
        saga.setCounterCount(CounterType.LORE, 1);
        harness.setGraveyard(player1, List.of(divination));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter II triggers

        // Select the sorcery from graveyard
        harness.handleMultipleCardsChosen(player1, List.of(divination.getId()));
        harness.passBothPriorities(); // resolve chapter II

        // Divination should be in hand
        harness.assertInHand(player1, "Divination");
        harness.assertNotInGraveyard(player1, "Divination");
    }

    @Test
    @DisplayName("Chapter II skips when no sorceries in graveyard")
    void chapterIISkipsWithNoSorceries() {
        // Only an instant in graveyard - chapter II should skip
        Shock shock = new Shock();
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheMirariConjecture());
        saga.setCounterCount(CounterType.LORE, 1);
        harness.setGraveyard(player1, List.of(shock));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter II triggers but no valid targets

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    @DisplayName("Chapter III adds controller to spell copy set")
    void chapterIIIGrantsSpellCopy() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheMirariConjecture());
        saga.setCounterCount(CounterType.LORE, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter III triggers

        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(3);

        harness.passBothPriorities(); // resolve chapter III

        assertThat(gd.playersWithSpellCopyUntilEndOfTurn).contains(player1.getId());
    }

    @Test
    @DisplayName("After chapter III, casting an instant creates a copy on the stack")
    void chapterIIICopiesInstantSpell() {
        // Set up the copy effect directly
        gd.playersWithSpellCopyUntilEndOfTurn.add(player1.getId());

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        // Stack should have: original Lightning Bolt + copy triggered ability
        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getDescription().contains("Copy"));
    }

    @Test
    @DisplayName("After chapter III, casting a sorcery creates a copy on the stack")
    void chapterIIICopiesSorcerySpell() {
        gd.playersWithSpellCopyUntilEndOfTurn.add(player1.getId());

        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);

        // Stack should have: original Divination + copy triggered ability
        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getDescription().contains("Copy"));
    }

    @Test
    @DisplayName("Copy triggered ability resolves and creates a spell copy")
    void copyTriggerResolvesCreatingCopy() {
        gd.playersWithSpellCopyUntilEndOfTurn.add(player1.getId());

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        // Resolve the copy trigger (top of stack)
        harness.passBothPriorities();

        // Now the stack should have the copy + original
        long spellCount = gd.stack.stream()
                .filter(e -> e.getCard().getName().equals("Lightning Bolt"))
                .count();
        assertThat(spellCount).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Spell copy effect does not apply to creature spells")
    void spellCopyDoesNotApplyToCreatures() {
        gd.playersWithSpellCopyUntilEndOfTurn.add(player1.getId());

        Card creature = new com.github.laxika.magicalvibes.cards.g.GrizzlyBears();
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        // Stack should only have the creature spell, no copy trigger
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getDescription().contains("Copy"));
    }

    @Test
    @DisplayName("Spell copy effect is cleared at end of turn")
    void spellCopyIsClearedAtEndOfTurn() {
        gd.playersWithSpellCopyUntilEndOfTurn.add(player1.getId());
        assertThat(gd.playersWithSpellCopyUntilEndOfTurn).isNotEmpty();

        // Simulate end-of-turn by advancing through the end step → cleanup
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance through end step → cleanup clears the set

        assertThat(gd.playersWithSpellCopyUntilEndOfTurn).isEmpty();
    }

    @Test
    @DisplayName("Saga is sacrificed after chapter III resolves")
    void sagaSacrificedAfterChapterIII() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheMirariConjecture());
        saga.setCounterCount(CounterType.LORE, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter III triggers

        // Saga should still be on battlefield while chapter is on stack
        harness.assertOnBattlefield(player1, "The Mirari Conjecture");

        harness.passBothPriorities(); // resolve chapter III

        // Saga should be sacrificed
        harness.assertNotOnBattlefield(player1, "The Mirari Conjecture");
        harness.assertInGraveyard(player1, "The Mirari Conjecture");
    }

    @Test
    @DisplayName("Saga with lore counter 1 enters and chapter I triggers on ETB")
    void sagaEntersWithLoreCounterOne() {
        harness.setHand(player1, List.of(new TheMirariConjecture()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment

        Permanent saga = findPermanent(player1, "The Mirari Conjecture");
        assertThat(saga).isNotNull();
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each final chapter resolution creates an independent copy ability")
    void multipleFinalChaptersEachCopyTheSpell() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new TheMirariConjecture());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new TheMirariConjecture());
        first.setCounterCount(CounterType.LORE, 2);
        second.setCounterCount(CounterType.LORE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.setHand(player1, List.of(new Divination()));
        harness.setLibrary(player1, List.of(new Divination(), new Divination(), new Divination(),
                new Divination(), new Divination(), new Divination(), new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        assertThat(gd.stack.stream()
                .filter(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY))
                .hasSize(2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Divination"))).hasSize(1);
    }

    @Test
    @DisplayName("The final chapter copies every subsequent sorcery, including spells without targets")
    void finalChapterCopiesMultipleTargetlessSpells() {
        resolveFinalChapter();
        harness.setHand(player1, List.of(new Divination(), new Divination()));
        harness.setLibrary(player1, List.of(new Divination(), new Divination(), new Divination(),
                new Divination(), new Divination(), new Divination(), new Divination(),
                new Divination(), new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);

        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(8);
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Divination"))).hasSize(2);
    }

    @Test
    @DisplayName("A copied instant can choose a different target without changing the original")
    void finalChapterCopyCanChooseNewTarget() {
        resolveFinalChapter();
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player2,
                new com.github.laxika.magicalvibes.cards.g.GrizzlyBears());
        Permanent copyTarget = harness.addToBattlefieldAndReturn(player2,
                new com.github.laxika.magicalvibes.cards.g.GrizzlyBears());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, originalTarget.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, copyTarget.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(originalTarget).doesNotContain(copyTarget);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(originalTarget, copyTarget);
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Lightning Bolt"))).hasSize(1);
    }

    @Test
    @DisplayName("Keeping the original target resolves both instant spells")
    void finalChapterCopyCanKeepOriginalTarget() {
        resolveFinalChapter();
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertLife(player2, 14);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The final chapter does not copy an opponent's spells")
    void finalChapterDoesNotCopyOpponentSpells() {
        resolveFinalChapter();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Divination()));
        harness.setLibrary(player2, List.of(new Divination(), new Divination(), new Divination()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castSorcery(player2, 0, 0);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Spells are no longer copied after the final chapter's turn ends")
    void finalChapterCopyAbilityExpires() {
        resolveFinalChapter();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Divination()));
        harness.setLibrary(player1, List.of(new Divination(), new Divination(), new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Chapter I offers only instants in its controller's graveyard")
    void chapterITargetChoicesExcludeOpponentsGraveyard() {
        Opt ownInstant = new Opt();
        Opt opposingInstant = new Opt();
        harness.setGraveyard(player1, List.of(new Divination(), ownInstant));
        harness.setGraveyard(player2, List.of(opposingInstant));
        harness.setHand(player1, List.of(new TheMirariConjecture()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).cards())
                .containsExactly(ownInstant);
        harness.handleMultipleCardsChosen(player1, List.of(ownInstant.getId()));
        resolveAllTriggers();
        harness.assertInHand(player1, "Opt");
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingInstant);
    }

    @Test
    @DisplayName("Chapter II offers only sorceries in its controller's graveyard")
    void chapterIITargetChoicesExcludeInstantsAndOpponentsGraveyard() {
        Divination ownSorcery = new Divination();
        Divination opposingSorcery = new Divination();
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheMirariConjecture());
        saga.setCounterCount(CounterType.LORE, 1);
        harness.setGraveyard(player1, List.of(new Opt(), ownSorcery));
        harness.setGraveyard(player2, List.of(opposingSorcery));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).cards())
                .containsExactly(ownSorcery);
        harness.handleMultipleCardsChosen(player1, List.of(ownSorcery.getId()));
        resolveAllTriggers();
        harness.assertInHand(player1, "Divination");
        harness.assertInGraveyard(player1, "Opt");
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingSorcery);
    }

    @Test
    @DisplayName("Chapter I does not return a target that has left the graveyard")
    void chapterIDoesNotReturnAbsentTarget() {
        Opt target = new Opt();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new TheMirariConjecture()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));

        harness.setGraveyard(player1, List.of());
        resolveAllTriggers();

        harness.assertNotInHand(player1, "Opt");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A copy inherits target changes made before its trigger resolves")
    void finalChapterCopyUsesOriginalSpellsCurrentTarget() {
        resolveFinalChapter();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        LightningBolt bolt = new LightningBolt();
        harness.setHand(player1, List.of(bolt));
        harness.setHand(player2, List.of(new Redirect()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bolt.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, player1.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertLife(player1, 14);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A copy with one graveyard-card target offers the choice to change it")
    void finalChapterCopyCanRetargetSingleGraveyardCard() {
        resolveFinalChapter();
        BalothGorger originalTarget = new BalothGorger();
        BalothGorger alternateTarget = new BalothGorger();
        harness.setGraveyard(player1, List.of(originalTarget, alternateTarget));
        harness.setHand(player1, List.of(new SoulSalvage()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(originalTarget.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
    private void resolveFinalChapter() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheMirariConjecture());
        saga.setCounterCount(CounterType.LORE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();
        harness.assertNotOnBattlefield(player1, "The Mirari Conjecture");
    }
}
