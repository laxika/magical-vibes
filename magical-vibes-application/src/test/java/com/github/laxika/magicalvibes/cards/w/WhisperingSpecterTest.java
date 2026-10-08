package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.s.SpireMonitor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WhisperingSpecter.class, SpireMonitor.class})
class WhisperingSpecterTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifice choice is made when the combat damage trigger resolves")
    void sacrificeChoiceWaitsForResolution() {
        Permanent specter = addCreatureReady(player1, new WhisperingSpecter());
        specter.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting may sacrifices Whispering Specter and forces discard equal to poison counters")
    void sacrificeSelfAndTargetDiscards() {
        // Give opponent 3 poison counters before combat
        gd.playerPoisonCounters.put(player2.getId(), 3);

        Permanent specter = addCreatureReady(player1, new WhisperingSpecter());
        specter.setAttacking(true);

        // Give player2 enough cards to discard
        harness.setHand(player2, List.of(
                new SpireMonitor(), new SpireMonitor(), new SpireMonitor(),
                new SpireMonitor(), new SpireMonitor()));

        resolveCombat();

        // Accept the may ability
        harness.handleMayAbilityChosen(player1, true);
        // Resolve the triggered ability from the stack
        harness.passBothPriorities();

        // Whispering Specter should be sacrificed
        harness.assertNotOnBattlefield(player1, "Whispering Specter");
        harness.assertInGraveyard(player1, "Whispering Specter");

        // Player2 should be prompted to discard cards equal to their poison counter count
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
    }

    @Test
    @DisplayName("Declining the may ability keeps Whispering Specter alive and no discard")
    void declineSacrifice() {
        gd.playerPoisonCounters.put(player2.getId(), 2);

        Permanent specter = addCreatureReady(player1, new WhisperingSpecter());
        specter.setAttacking(true);

        int handSizeBefore = gd.playerHands.get(player2.getId()).size();

        resolveCombat();

        harness.handleMayAbilityChosen(player1, false);

        // Whispering Specter should still be on the battlefield
        harness.assertOnBattlefield(player1, "Whispering Specter");

        // No cards discarded
        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(handSizeBefore);

        assertThat(gameLogContains("declines")).isTrue();
    }

    @Test
    @DisplayName("No trigger when Whispering Specter is blocked and deals no damage to player")
    void noTriggerWhenBlocked() {
        Permanent specter = addCreatureReady(player1, new WhisperingSpecter());
        specter.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new SpireMonitor());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Infect combat damage gives poison counter then discard based on total")
    void infectCombatDamageGivesPoisonThenDiscard() {
        // No prior poison counters — infect combat damage will give 1
        Permanent specter = addCreatureReady(player1, new WhisperingSpecter());
        specter.setAttacking(true);
        harness.setHand(player2, List.of(new SpireMonitor(), new SpireMonitor()));

        resolveCombat();

        // Accept the may ability
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        // Specter is sacrificed
        harness.assertNotOnBattlefield(player1, "Whispering Specter");

        // Player2 has 1 poison counter from infect combat damage, so they should discard 1 card
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
    }

    @Test
    @DisplayName("Sacrifice with no cards in opponent's hand does not error")
    void sacrificeWithEmptyOpponentHand() {
        gd.playerPoisonCounters.put(player2.getId(), 3);
        harness.setHand(player2, List.of());

        Permanent specter = addCreatureReady(player1, new WhisperingSpecter());
        specter.setAttacking(true);

        resolveCombat();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        // Specter should be sacrificed even though opponent has no cards
        harness.assertNotOnBattlefield(player1, "Whispering Specter");
        harness.assertInGraveyard(player1, "Whispering Specter");
    }

    @Test
    @DisplayName("Discard count uses poison counters at resolution")
    void discardCountUsesCurrentPoisonCounters() {
        Permanent specter = addCreatureReady(player1, new WhisperingSpecter());
        specter.setAttacking(true);
        harness.setHand(player2, List.of(new SpireMonitor(), new SpireMonitor(),
                new SpireMonitor(), new SpireMonitor()));

        resolveCombat();
        gd.playerPoisonCounters.put(player2.getId(), 3);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        for (int i = 0; i < 3; i++) {
            assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                    .isEqualTo(player2.getId());
            harness.handleCardChosen(player2, 0);
        }
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Whispering Specter");
    }

    @Test
    @DisplayName("Cannot sacrifice a Specter another player now controls")
    void cannotSacrificeAfterLosingControl() {
        Permanent specter = addCreatureReady(player1, new WhisperingSpecter());
        specter.setAttacking(true);
        harness.setHand(player2, List.of(new SpireMonitor(), new SpireMonitor()));

        resolveCombat();
        gd.playerBattlefields.get(player1.getId()).remove(specter);
        gd.playerBattlefields.get(player2.getId()).add(specter);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Whispering Specter");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
    }

    @Test
    @DisplayName("No discard if the Specter left the battlefield before resolution")
    void noDiscardWhenSourceHasLeftBattlefield() {
        Permanent specter = addCreatureReady(player1, new WhisperingSpecter());
        specter.setAttacking(true);
        harness.setHand(player2, List.of(new SpireMonitor(), new SpireMonitor()));

        resolveCombat();
        gd.playerBattlefields.get(player1.getId()).remove(specter);
        gd.playerGraveyards.get(player1.getId()).add(specter.getCard());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
    }
    @Test
    @DisplayName("Discard as many cards as possible when poison exceeds hand size")
    void discardStopsWhenHandIsEmpty() {
        gd.playerPoisonCounters.put(player2.getId(), 3);
        Permanent specter = addCreatureReady(player1, new WhisperingSpecter());
        specter.setAttacking(true);
        harness.setHand(player2, List.of(new SpireMonitor(), new SpireMonitor()));

        resolveCombat();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Whispering Specter");
    }

    @Test
    @DisplayName("Sacrifice still happens when no poison counters remain at resolution")
    void sacrificeWithZeroPoisonCounters() {
        Permanent specter = addCreatureReady(player1, new WhisperingSpecter());
        specter.setAttacking(true);
        harness.setHand(player2, List.of(new SpireMonitor()));

        resolveCombat();
        gd.playerPoisonCounters.put(player2.getId(), 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Whispering Specter");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
    }
}
