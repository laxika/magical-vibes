package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CryptRats;
import com.github.laxika.magicalvibes.cards.l.LongbowArcher;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GossamerChains.class, CryptRats.class, LongbowArcher.class})
class GossamerChainsTest extends BaseCardTest {

    @Test
    @DisplayName("Returns to hand as a cost and prevents combat damage from the unblocked attacker")
    void bouncesAndPreventsUnblockedCombatDamage() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new GossamerChains());
        Permanent attacker = addUnblockedAttacker(player1, player2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        int chainsIndex = battlefieldIndex(player2, "Gossamer Chains");
        harness.activateAbility(player2, chainsIndex, null, attacker.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Gossamer Chains");
        harness.assertNotOnBattlefield(player2, "Gossamer Chains");
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());

        resolveCombat(player1);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Prevents combat damage only from the targeted attacker")
    void preventsOnlyTargetedAttacker() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new GossamerChains());
        Permanent preventedAttacker = addUnblockedAttacker(player1, player2);
        addUnblockedAttacker(player1, player2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        int chainsIndex = battlefieldIndex(player2, "Gossamer Chains");
        harness.activateAbility(player2, chainsIndex, null, preventedAttacker.getId());
        harness.passBothPriorities();

        resolveCombat(player1);

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Prevents combat damage but not noncombat damage from the target")
    void preventsCombatDamageButNotNoncombatDamage() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new GossamerChains());
        Permanent attacker = addUnblockedAttacker(player1, player2, new CryptRats());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        int chainsIndex = battlefieldIndex(player2, "Gossamer Chains");
        harness.activateAbility(player2, chainsIndex, null, attacker.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, battlefieldIndex(player1, "Crypt Rats"), 1, null);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Cannot target a blocked attacker")
    void cannotTargetBlockedAttacker() {
        harness.addToBattlefield(player2, new GossamerChains());
        Permanent blocked = addUnblockedAttacker(player1, player2);
        Permanent blocker = addCreatureReady(player2, new LongbowArcher());
        blocker.setBlocking(true);
        blocker.getBlockingTargetIds().add(blocked.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        int chainsIndex = battlefieldIndex(player2, "Gossamer Chains");
        UUID targetId = blocked.getId();
        assertThatThrownBy(() -> harness.activateAbility(player2, chainsIndex, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an attacker before blockers are declared")
    void cannotTargetBeforeBlockersDeclared() {
        harness.addToBattlefield(player2, new GossamerChains());
        Permanent attacker = addUnblockedAttacker(player1, player2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        int chainsIndex = battlefieldIndex(player2, "Gossamer Chains");
        UUID targetId = attacker.getId();
        assertThatThrownBy(() -> harness.activateAbility(player2, chainsIndex, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target your own unblocked attacker and returns to hand before resolution")
    void returnsAsCostWhenTargetingOwnAttacker() {
        harness.addToBattlefield(player1, new GossamerChains());
        Permanent attacker = addUnblockedAttacker(player1, player2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, battlefieldIndex(player1, "Gossamer Chains"), null, attacker.getId());

        harness.assertInHand(player1, "Gossamer Chains");
        harness.assertNotOnBattlefield(player1, "Gossamer Chains");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(attacker.getId());

        harness.passBothPriorities();
        resolveCombat(player1);

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot target a nonattacking creature or pay the return cost for an illegal target")
    void cannotTargetNonattackingCreature() {
        harness.addToBattlefield(player2, new GossamerChains());
        Permanent creature = addCreatureReady(player1, new LongbowArcher());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        int chainsIndex = battlefieldIndex(player2, "Gossamer Chains");

        assertThatThrownBy(() -> harness.activateAbility(player2, chainsIndex, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Gossamer Chains");
        harness.assertNotInHand(player2, "Gossamer Chains");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The return cost stays paid when the target leaves before resolution")
    void returnCostStaysPaidWhenTargetLeaves() {
        harness.addToBattlefield(player2, new GossamerChains());
        Permanent attacker = addUnblockedAttacker(player1, player2, new CryptRats());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, battlefieldIndex(player2, "Gossamer Chains"), null, attacker.getId());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, battlefieldIndex(player1, "Crypt Rats"), 1, null);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Crypt Rats");
        harness.assertInHand(player2, "Gossamer Chains");
        harness.assertNotOnBattlefield(player2, "Gossamer Chains");
        harness.assertLife(player2, 19);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(attacker.getId());
    }

    @Test
    @DisplayName("Combat damage prevention expires at the end of the turn")
    void preventionExpiresAfterTurn() {
        harness.addToBattlefield(player2, new GossamerChains());
        Permanent attacker = addUnblockedAttacker(player1, player2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, battlefieldIndex(player2, "Gossamer Chains"), null, attacker.getId());
        harness.passBothPriorities();
        resolveCombat(player1);
        harness.assertLife(player2, 20);

        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveCombat(player1);

        harness.assertLife(player2, 18);
    }
    private Permanent addUnblockedAttacker(Player owner, Player defender) {
        return addUnblockedAttacker(owner, defender, new LongbowArcher());
    }

    private Permanent addUnblockedAttacker(Player owner, Player defender, Card card) {
        Permanent attacker = addCreatureReady(owner, card);
        attacker.setAttacking(true);
        attacker.setAttackTarget(defender.getId());
        return attacker;
    }

    private int battlefieldIndex(Player owner, String name) {
        return gd.playerBattlefields.get(owner.getId()).indexOf(findPermanent(owner, name));
    }
}
