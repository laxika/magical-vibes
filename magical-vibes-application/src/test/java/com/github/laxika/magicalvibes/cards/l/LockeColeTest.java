package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LockeCole.class, Forest.class, GrizzlyBears.class})
class LockeColeTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage draws a card, then discards a card")
    void combatDamageDrawsThenDiscards() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        Permanent locke = addCreatureReady(player1, new LockeCole());
        locke.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Combat damage trigger does not happen when Locke deals no damage")
    void noTriggerWhenBlocked() {
        Permanent locke = addCreatureReady(player1, new LockeCole());
        locke.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The newly drawn card can be discarded")
    void canDiscardTheDrawnCard() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new LockeCole()));

        Permanent locke = addCreatureReady(player1, new LockeCole());
        locke.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.assertInHand(player1, "Forest");
        harness.handleCardChosen(player1, 1);

        harness.assertInGraveyard(player1, "Forest");
        harness.assertInHand(player1, "Locke Cole");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The attacking controller draws and discards rather than the damaged player")
    void opponentControllerDrawsAndDiscards() {
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player2, List.of(new LockeCole()));
        harness.setHand(player1, List.of(new Forest()));

        Permanent locke = addCreatureReady(player2, new LockeCole());
        locke.setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.assertInHand(player2, "Forest");
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player2, "Locke Cole");
        harness.assertInHand(player2, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Blocked combat applies deathtouch and lifelink without looting")
    void blockedCombatAppliesKeywordsWithoutLooting() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));

        Permanent attacker = addCreatureReady(player1, new LockeCole());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new LockeCole());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Locke Cole");
        harness.assertInGraveyard(player2, "Locke Cole");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 22);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
