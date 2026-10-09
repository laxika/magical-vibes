package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CurseOfStalkedPrey.class, WalkingCorpse.class})
class CurseOfStalkedPreyTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast targeting opponent, enters battlefield attached to that player")
    void castTargetingOpponent() {
        harness.setHand(player1, List.of(new CurseOfStalkedPrey()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve

        harness.assertOnBattlefield(player1, "Curse of Stalked Prey");

        Permanent curse = findPermanent(player1, "Curse of Stalked Prey");
        assertThat(curse.getAttachedTo()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Can be cast targeting self")
    void castTargetingSelf() {
        harness.setHand(player1, List.of(new CurseOfStalkedPrey()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, player1.getId());
        harness.passBothPriorities(); // resolve

        Permanent curse = findPermanent(player1, "Curse of Stalked Prey");
        assertThat(curse.getAttachedTo()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Creature gets +1/+1 counter when dealing combat damage to enchanted player")
    void creatureGetsCounterOnCombatDamage() {
        // Put curse on player2
        addCurseOnPlayer2();

        // Add an attacking creature for player1
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage

        // Player2 takes 2 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        // Resolve the triggered ability
        harness.passBothPriorities();

        // Attacker should have a +1/+1 counter
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple creatures each get a +1/+1 counter when dealing combat damage to enchanted player")
    void multipleCreaturesGetCounters() {
        addCurseOnPlayer2();

        Permanent attacker1 = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        attacker1.setSummoningSick(false);
        attacker1.setAttacking(true);

        Permanent attacker2 = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        attacker2.setSummoningSick(false);
        attacker2.setAttacking(true);

        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage

        // Player2 takes 4 total damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);

        // Resolve both triggered abilities
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(attacker1.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(attacker2.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Blocked creature that deals no damage to player does not get a counter")
    void blockedCreatureDoesNotGetCounter() {
        addCurseOnPlayer2();

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        // Block with a creature (attacker is at battlefield index 1, curse is at index 0)
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(1);

        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage

        // Player2 takes no damage (creature was blocked)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);

        // No counter on attacker (it dealt damage to blocker, not player)
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Curse is not removed as orphaned aura when attached to player")
    void curseNotRemovedAsOrphanedAura() {
        addCurseOnPlayer2();

        // Advance through several steps to trigger SBA / orphan aura checks
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Curse should still be on the battlefield
        harness.assertOnBattlefield(player1, "Curse of Stalked Prey");
    }

    @Test
    @DisplayName("Counter stacks — creature with existing counter gets another")
    void counterStacks() {
        addCurseOnPlayer2();

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        attacker.setSummoningSick(false);
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1); // already has a counter
        attacker.setAttacking(true);

        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage

        // 2 base + 1 counter = 3 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);

        // Resolve triggered ability
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("A creature controlled by the Curse controller's opponent also gets a counter")
    void opponentCreatureGetsCounterWhenControllerIsEnchanted() {
        Permanent curse = harness.addToBattlefieldAndReturn(player1, new CurseOfStalkedPrey());
        curse.setAttachedTo(player1.getId());
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, harness::passBothPriorities);

        harness.assertLife(player1, 18);
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());

        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(curse.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Damage to a player who is not enchanted does not trigger the Curse")
    void damageToUnenchantedPlayerDoesNotTrigger() {
        addCurseOnPlayer2();
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        assertThat(gd.stack).isEmpty();
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Removing the Curse after combat damage does not stop its pending trigger")
    void pendingTriggerSurvivesCurseLeavingBattlefield() {
        Permanent curse = addCurseOnPlayer2();
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, harness::passBothPriorities);

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(curse);
        gd.playerGraveyards.get(player1.getId()).add(curse.getCard());
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addCurseOnPlayer2() {
        Permanent curse = harness.addToBattlefieldAndReturn(player1, new CurseOfStalkedPrey());
        curse.setAttachedTo(player2.getId());
        return curse;
    }
}
