package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Larceny.class, GrizzlyBears.class, FreshVolunteers.class})
class LarcenyTest extends BaseCardTest {

    @Test
    @DisplayName("A creature you control deals combat damage — that player discards a card")
    void combatDamageForcesDiscard() {
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new Larceny());

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("No trigger without Larceny on the battlefield")
    void noTriggerWithoutLarceny() {
        harness.setHand(player2, List.of(new GrizzlyBears()));

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("No discard when the damaged player has an empty hand")
    void noDiscardWhenEmptyHand() {
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player1, new Larceny());

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("no cards to discard"));
    }

    @Test
    @DisplayName("A creature controlled by an opponent does not trigger Larceny")
    void opponentCreatureDoesNotTrigger() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new Larceny());

        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A blocked creature does not trigger Larceny when it deals no damage to a player")
    void blockedCreatureDoesNotTrigger() {
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new Larceny());

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(1);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Each creature that deals combat damage creates a discard trigger")
    void eachDamagingCreatureTriggersSeparately() {
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addToBattlefield(player1, new Larceny());

        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        firstAttacker.setAttacking(true);
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());
        secondAttacker.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player2, 0);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
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
}
