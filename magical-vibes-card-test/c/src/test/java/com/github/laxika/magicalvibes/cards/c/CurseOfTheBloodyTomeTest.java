package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CurseOfTheBloodyTome.class})
class CurseOfTheBloodyTomeTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Curse of the Bloody Tome targeting a player puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new CurseOfTheBloodyTome()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Resolving Curse of the Bloody Tome attaches it to target player")
    void resolvingAttachesToPlayer() {
        harness.setHand(player1, List.of(new CurseOfTheBloodyTome()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Curse of the Bloody Tome")
                        && p.isAttached()
                        && p.getAttachedTo().equals(player2.getId()));
    }

    @Test
    @DisplayName("Enchanted player mills 2 cards at their upkeep")
    void enchantedPlayerMillsAtUpkeep() {
        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new CurseOfTheBloodyTome());
        auraPerm.setAttachedTo(player2.getId());

        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();
        int graveyardSizeBefore = gd.playerGraveyards.get(player2.getId()).size();

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(graveyardSizeBefore + 2);
    }

    @Test
    @DisplayName("Mill trigger does NOT fire during aura controller's upkeep")
    void millDoesNotFireDuringAuraControllerUpkeep() {
        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new CurseOfTheBloodyTome());
        auraPerm.setAttachedTo(player2.getId());

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        // Player1's deck should be unchanged
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
    }

    @Test
    @DisplayName("Mill accumulates over multiple upkeeps")
    void millAccumulatesOverUpkeeps() {
        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new CurseOfTheBloodyTome());
        auraPerm.setAttachedTo(player2.getId());

        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 4);
    }

    @Test
    @DisplayName("No mill after Curse is removed")
    void noMillAfterRemoval() {
        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new CurseOfTheBloodyTome());
        auraPerm.setAttachedTo(player2.getId());

        // Remove the curse
        gd.playerBattlefields.get(player1.getId()).remove(auraPerm);

        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);
    }

    @Test
    @DisplayName("Curse attached to player is not removed as orphaned aura")
    void curseAttachedToPlayerNotOrphaned() {
        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new CurseOfTheBloodyTome());
        auraPerm.setAttachedTo(player2.getId());

        // Advance through several steps to trigger SBA / orphan aura checks
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Curse should still be on the battlefield
        harness.assertOnBattlefield(player1, "Curse of the Bloody Tome");
    }

    @Test
    @DisplayName("Can cast Curse targeting yourself")
    void canCurseSelf() {
        harness.setHand(player1, List.of(new CurseOfTheBloodyTome()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Curse of the Bloody Tome")
                        && p.getAttachedTo().equals(player1.getId()));
    }

    @Test
    @DisplayName("Self-cursed player mills at their own upkeep")
    void selfCursedPlayerMillsAtUpkeep() {
        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new CurseOfTheBloodyTome());
        auraPerm.setAttachedTo(player1.getId());

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 2);
    }

    @Test
    @DisplayName("The upkeep trigger mills the top two cards and leaves the controller's library alone")
    void millsTopTwoCardsOnly() {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new CurseOfTheBloodyTome());
        aura.setAttachedTo(player2.getId());
        CurseOfTheBloodyTome top = new CurseOfTheBloodyTome();
        CurseOfTheBloodyTome second = new CurseOfTheBloodyTome();
        CurseOfTheBloodyTome third = new CurseOfTheBloodyTome();
        harness.setLibrary(player2, List.of(top, second, third));
        int controllerLibrarySize = gd.playerDecks.get(player1.getId()).size();

        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(top, second, third);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(third);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(top, second).doesNotContain(third);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(controllerLibrarySize);
    }

    @Test
    @DisplayName("A library with one card mills just that card")
    void millsOnlyAvailableCard() {
        gd.playerAutoStopSteps.put(player1.getId(), java.util.Set.of(TurnStep.UPKEEP));
        gd.playerAutoStopSteps.put(player2.getId(), java.util.Set.of(TurnStep.UPKEEP));
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new CurseOfTheBloodyTome());
        aura.setAttachedTo(player2.getId());
        CurseOfTheBloodyTome remaining = new CurseOfTheBloodyTome();
        harness.setLibrary(player2, List.of(remaining));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(remaining);
        assertThat(gd.currentStep).isEqualTo(TurnStep.UPKEEP);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Milling an empty library does nothing")
    void emptyLibraryDoesNotPreventResolution() {
        gd.playerAutoStopSteps.put(player1.getId(), java.util.Set.of(TurnStep.UPKEEP));
        gd.playerAutoStopSteps.put(player2.getId(), java.util.Set.of(TurnStep.UPKEEP));
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new CurseOfTheBloodyTome());
        aura.setAttachedTo(player2.getId());
        harness.setLibrary(player2, List.of());
        int graveyardSize = gd.playerGraveyards.get(player2.getId()).size();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(graveyardSize);
        assertThat(gd.currentStep).isEqualTo(TurnStep.UPKEEP);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("An already triggered ability still mills after the Curse leaves the battlefield")
    void triggerResolvesAfterCurseLeaves() {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new CurseOfTheBloodyTome());
        aura.setAttachedTo(player2.getId());
        int librarySize = gd.playerDecks.get(player2.getId()).size();

        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySize - 2);
    }

    @Test
    @DisplayName("Each Curse triggers independently for the enchanted player's upkeep")
    void multipleCursesMillFourCards() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new CurseOfTheBloodyTome());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new CurseOfTheBloodyTome());
        first.setAttachedTo(player2.getId());
        second.setAttachedTo(player2.getId());
        int librarySize = gd.playerDecks.get(player2.getId()).size();

        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySize - 4);
    }
}
