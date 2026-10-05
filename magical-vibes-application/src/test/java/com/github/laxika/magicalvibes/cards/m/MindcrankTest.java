package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GutShot;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Mindcrank.class, GrizzlyBears.class, Shock.class, GutShot.class})
class MindcrankTest extends BaseCardTest {

    @Test
    @DisplayName("Mindcrank mills opponent when they take spell damage")
    void millsOnSpellDamage() {
        harness.addToBattlefield(player1, new Mindcrank());
        harness.setLife(player2, 20);

        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();
        int graveyardSizeBefore = gd.playerGraveyards.get(player2.getId()).size();

        // Shock player2 for 2 damage
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        // Player2 lost 2 life → mills 2 cards
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(graveyardSizeBefore + 2);
    }

    @Test
    @DisplayName("Mindcrank mills opponent when they take combat damage")
    void millsOnCombatDamage() {
        harness.addToBattlefield(player1, new Mindcrank());
        harness.setLife(player2, 20);

        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        // Put an attacking 2/2 creature on player1's battlefield
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Player2 took 2 combat damage → mills 2 cards
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 2);
    }

    @Test
    @DisplayName("Mindcrank does NOT trigger when its controller loses life")
    void doesNotTriggerOnControllerLifeLoss() {
        harness.addToBattlefield(player1, new Mindcrank());
        harness.setLife(player1, 20);

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        // Player1 shocks themselves for 2 damage
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player1.getId());

        // Player1 lost life but Mindcrank shouldn't trigger (it's on player1's side)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
    }

    @Test
    @DisplayName("Two Mindcranks each trigger when opponent loses life, milling twice")
    void twoMindcranksEachTrigger() {
        harness.addToBattlefield(player1, new Mindcrank());
        harness.addToBattlefield(player1, new Mindcrank());
        harness.setLife(player2, 20);

        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        // 2 damage → each Mindcrank mills 2 → total 4 milled
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 4);
    }

    @Test
    @DisplayName("Mindcrank trigger is logged")
    void triggerIsLogged() {
        harness.addToBattlefield(player1, new Mindcrank());
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log ->
                log.contains("Mindcrank") && log.contains("triggers") && log.contains("mills"));
    }

    @Test
    @DisplayName("Mindcrank does not trigger when not on the battlefield")
    void doesNotTriggerWhenNotOnBattlefield() {
        harness.setLife(player2, 20);

        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        // No Mindcrank on battlefield — no milling
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);
    }

    @Test
    @DisplayName("Opponent's Mindcrank triggers when we lose life")
    void opponentsMindcrankTriggersOnOurLifeLoss() {
        harness.addToBattlefield(player2, new Mindcrank());
        harness.setLife(player1, 20);

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        // Player1 shocks themselves — player2's Mindcrank should trigger
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player1.getId());

        // Player1 lost 2 life → player2's Mindcrank mills player1 for 2
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 2);
    }

    @Test
    @DisplayName("Mindcrank mills nothing when opponent has no library")
    void millsNothingWhenLibraryEmpty() {
        harness.addToBattlefield(player1, new Mindcrank());
        harness.setLife(player2, 20);

        // Empty player2's library
        harness.setLibrary(player2, List.of());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        // Damage still happens, milling just doesn't do anything
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Life loss queues milling on the stack before any cards are milled")
    void millingWaitsForTriggerResolution() {
        harness.addToBattlefield(player1, new Mindcrank());
        harness.setLibrary(player2, List.of(new Mindcrank(), new Mindcrank()));
        harness.setGraveyard(player2, List.of());
        harness.setHand(player1, List.of(new GutShot()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 19);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Paying Phyrexian mana with life queues milling above the spell")
    void lifePaymentTriggersBeforeSpellResolves() {
        harness.addToBattlefield(player2, new Mindcrank());
        harness.setLibrary(player1, List.of(new Mindcrank(), new Mindcrank(), new Mindcrank()));
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new GutShot()));

        harness.castInstant(player1, 0, player2.getId());

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Milling more cards than remain mills the entire library")
    void millsOnlyRemainingCards() {
        harness.addToBattlefield(player1, new Mindcrank());
        harness.setLibrary(player2, List.of(new Mindcrank()));
        harness.setGraveyard(player2, List.of());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }
}
