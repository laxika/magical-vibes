package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwarmSaboteur.class})
class SwarmSaboteurTest extends BaseCardTest {

    @Test
    void combatDamageConjuresVirusBeetleIntoHand() {
        Permanent saboteur = addCreatureReady(player1, new SwarmSaboteur());
        saboteur.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .anySatisfy(card -> {
                    assertThat(card.getName()).isEqualTo("Virus Beetle");
                    assertThat(card.isToken()).isFalse();
                });
    }

    @Test
    void blockedSaboteurDoesNotConjureVirusBeetle() {
        harness.setHand(player1, List.of());
        Permanent saboteur = addCreatureReady(player1, new SwarmSaboteur());
        saboteur.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new SwarmSaboteur());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void ninjutsuReturnsAttackerAndEntersTappedAndAttackingForOneBlackMana() {
        Permanent attacker = addCreatureReady(player1, new SwarmSaboteur());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        SwarmSaboteur ninja = new SwarmSaboteur();
        harness.setHand(player1, List.of(ninja));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.activateHandAbility(player1, 0, attacker.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ninja, attacker.getCard());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, harness::passBothPriorities);

        Permanent entered = findPermanent(player1, "Swarm Saboteur");
        assertThat(entered.getCard()).isSameAs(ninja);
        assertThat(entered.isTapped()).isTrue();
        assertThat(entered.isAttacking()).isTrue();
        assertThat(entered.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(attacker.getCard());

        resolveCombat();
        resolveAllTriggers();

        harness.assertInHand(player1, "Virus Beetle");
        harness.assertLife(player2, 18);
    }

    @Test
    void deathtouchKillsBlockerWithMoreToughnessThanDamageDealt() {
        Permanent saboteur = addCreatureReady(player1, new SwarmSaboteur());
        saboteur.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new SwarmSaboteur());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Swarm Saboteur");
        harness.assertNotOnBattlefield(player2, "Swarm Saboteur");
        harness.assertNotInHand(player1, "Virus Beetle");
        harness.assertLife(player2, 20);
    }

    @Test
    void playerTwoConjuresIntoTheirOwnHandWithCorrectOwnership() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        Permanent saboteur = addCreatureReady(player2, new SwarmSaboteur());
        saboteur.setAttacking(true);
        saboteur.setAttackTarget(player1.getId());

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).singleElement().satisfies(card -> {
            assertThat(card.getName()).isEqualTo("Virus Beetle");
            assertThat(card.getOwnerId()).isEqualTo(player2.getId());
            assertThat(card.isToken()).isFalse();
        });
    }
}
