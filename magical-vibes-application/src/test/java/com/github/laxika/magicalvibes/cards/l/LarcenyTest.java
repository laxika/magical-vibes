package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Larceny.class, FreshVolunteers.class})
class LarcenyTest extends BaseCardTest {

    @Test
    @DisplayName("A creature you control deals combat damage — that player discards a card")
    void combatDamageForcesDiscard() {
        harness.setHand(player2, List.of(new FreshVolunteers()));
        harness.addToBattlefield(player1, new Larceny());

        Permanent attacker = addCreatureReady(player1, new FreshVolunteers());
        attacker.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities(); // resolve the discard trigger

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Fresh Volunteers");
    }

    @Test
    @DisplayName("No trigger without Larceny on the battlefield")
    void noTriggerWithoutLarceny() {
        harness.setHand(player2, List.of(new FreshVolunteers()));

        Permanent attacker = addCreatureReady(player1, new FreshVolunteers());
        attacker.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player2, "Fresh Volunteers");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("No discard when the damaged player has an empty hand")
    void noDiscardWhenEmptyHand() {
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player1, new Larceny());

        Permanent attacker = addCreatureReady(player1, new FreshVolunteers());
        attacker.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("no cards to discard"));
    }

    @Test
    @DisplayName("Triggers once for each creature that deals combat damage")
    void triggersForEachCreatureThatDealsCombatDamage() {
        harness.setHand(player2, List.of(new FreshVolunteers(), new FreshVolunteers()));
        harness.addToBattlefield(player1, new Larceny());

        Permanent firstAttacker = addCreatureReady(player1, new FreshVolunteers());
        firstAttacker.setAttacking(true);
        Permanent secondAttacker = addCreatureReady(player1, new FreshVolunteers());
        secondAttacker.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Does not trigger for a creature controlled by another player")
    void opponentCreatureDoesNotTrigger() {
        harness.setHand(player1, List.of(new FreshVolunteers()));
        harness.addToBattlefield(player1, new Larceny());

        Permanent attacker = addCreatureReady(player2, new FreshVolunteers());
        attacker.setAttacking(true);

        resolveCombat(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Fresh Volunteers");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when the attacking creature is blocked")
    void blockedCreatureDoesNotTrigger() {
        harness.setHand(player2, List.of(new FreshVolunteers()));
        harness.addToBattlefield(player1, new Larceny());

        Permanent attacker = addCreatureReady(player1, new FreshVolunteers());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new FreshVolunteers());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(1);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player2, "Fresh Volunteers");
    }
}
