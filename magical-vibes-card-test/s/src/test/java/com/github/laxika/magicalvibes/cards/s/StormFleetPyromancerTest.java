package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.q.QueensBaySoldier;
import com.github.laxika.magicalvibes.cards.j.JaceCunningCastaway;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StormFleetPyromancer.class, QueensBaySoldier.class, JaceCunningCastaway.class})
class StormFleetPyromancerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB triggers and deals 2 damage to target creature when raid is met")
    void etbDeals2DamageToCreatureWithRaid() {
        harness.addToBattlefield(player2, new QueensBaySoldier());
        markAttackedThisTurn();
        castStormFleetPyromancer();

        harness.passBothPriorities(); // resolve creature spell — trigger-time target prompt
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Queen's Bay Soldier"));

        // ETB trigger should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Storm Fleet Pyromancer");

        harness.passBothPriorities(); // resolve ETB trigger

        // 2 damage to a 2/2 kills it
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Queen's Bay Soldier");
        harness.assertInGraveyard(player2, "Queen's Bay Soldier");
    }

    @Test
    @DisplayName("ETB triggers and deals 2 damage to target player when raid is met")
    void etbDeals2DamageToPlayerWithRaid() {
        harness.setLife(player2, 20);
        markAttackedThisTurn();
        castStormFleetPyromancer();

        harness.passBothPriorities(); // resolve creature spell — trigger-time target prompt
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("ETB does NOT trigger without raid (did not attack this turn)")
    void etbDoesNotTriggerWithoutRaid() {
        harness.setLife(player2, 20);
        castStormFleetPyromancer();

        harness.passBothPriorities(); // resolve creature spell

        // No ETB trigger on the stack and no target prompt (intervening-if failed, CR 603.4)
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();

        // Creature is on the battlefield
        harness.assertOnBattlefield(player1, "Storm Fleet Pyromancer");

        // Opponent life unchanged
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Creature enters battlefield even without raid")
    void creatureEntersBattlefieldWithoutRaid() {
        castStormFleetPyromancer();
        harness.passBothPriorities(); // resolve creature spell

        harness.assertOnBattlefield(player1, "Storm Fleet Pyromancer");
    }

    @Test
    @DisplayName("Raid remains satisfied after the attacker leaves the battlefield")
    void raidRemainsSatisfiedAfterAttackerLeaves() {
        harness.addToBattlefield(player1, new QueensBaySoldier());
        harness.setLife(player2, 20);
        markAttackedThisTurn();
        castStormFleetPyromancer();

        harness.passBothPriorities(); // resolve creature spell — trigger-time target prompt
        harness.handlePermanentChosen(player1, player2.getId()); // ETB trigger on stack

        gd.playerBattlefields.get(player1.getId()).removeIf(
                permanent -> permanent.getCard() instanceof QueensBaySoldier);

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("ETB fizzles if target creature is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new QueensBaySoldier());
        markAttackedThisTurn();
        UUID targetId = harness.getPermanentId(player2, "Queen's Bay Soldier");
        castStormFleetPyromancer();

        harness.passBothPriorities(); // resolve creature spell — trigger-time target prompt
        harness.handlePermanentChosen(player1, targetId); // ETB trigger on stack

        // Remove target before ETB resolves
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities(); // resolve ETB — fizzles

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Trigger-time prompt offers both creatures and players (any target)")
    void triggerTimePromptOffersAnyTarget() {
        harness.addToBattlefield(player2, new QueensBaySoldier());
        markAttackedThisTurn();
        castStormFleetPyromancer();

        harness.passBothPriorities(); // resolve creature spell — trigger-time target prompt

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds())
                .contains(harness.getPermanentId(player2, "Queen's Bay Soldier"), player1.getId(), player2.getId());
    }

    @Test
    @DisplayName("An opponent's attack does not satisfy raid")
    void opponentsAttackDoesNotSatisfyRaid() {
        gd.playersDeclaredAttackersThisTurn.add(player2.getId());
        castStormFleetPyromancer();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Storm Fleet Pyromancer");
    }

    @Test
    @DisplayName("Raid damage can target its controller")
    void raidCanDamageController() {
        harness.setLife(player1, 20);
        markAttackedThisTurn();
        castStormFleetPyromancer();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Raid damage removes two loyalty counters from a planeswalker")
    void raidDamagesPlaneswalker() {
        var jace = harness.addToBattlefieldAndReturn(player2, new JaceCunningCastaway());
        jace.setCounterCount(CounterType.LOYALTY, 3);
        markAttackedThisTurn();
        castStormFleetPyromancer();
        harness.passBothPriorities();
        UUID targetId = harness.getPermanentId(player2, "Jace, Cunning Castaway");
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Jace, Cunning Castaway");
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("Raid damage resolves even if its source leaves the battlefield")
    void raidResolvesAfterSourceLeaves() {
        harness.setLife(player2, 20);
        markAttackedThisTurn();
        castStormFleetPyromancer();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.stack).isEmpty();
    }

    private void markAttackedThisTurn() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
    }

    private void castStormFleetPyromancer() {
        harness.setHand(player1, List.of(new StormFleetPyromancer()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castCreature(player1, 0);
    }
}
