package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VigorTest extends BaseCardTest {

    private Permanent bearsOf(java.util.UUID playerId) {
        return gd.playerBattlefields.get(playerId).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("Noncombat damage to another creature you control is prevented and replaced with +1/+1 counters")
    void preventsNoncombatDamageToOtherCreature() {
        harness.addToBattlefield(player2, new Vigor());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities();

        // Shock's 2 damage prevented; Bears survives with two +1/+1 counters.
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(bearsOf(player2.getId()).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Vigor does not protect itself — its own damage is not prevented and adds no counters")
    void vigorDoesNotProtectItself() {
        harness.addToBattlefield(player2, new Vigor());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID vigorId = harness.getPermanentId(player2, "Vigor");
        harness.castInstant(player1, 0, vigorId);
        harness.passBothPriorities();

        Permanent vigor = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Vigor"))
                .findFirst().orElseThrow();
        // "Another creature" — Vigor's own damage lands as marked damage, no +1/+1 counters.
        assertThat(vigor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(vigor.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Combat damage to another creature you control is prevented and replaced with +1/+1 counters")
    void preventsCombatDamageToOtherCreature() {
        harness.addToBattlefield(player2, new Vigor());

        GrizzlyBears blockerCard = new GrizzlyBears();
        Permanent blocker = new Permanent(blockerCard);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        GrizzlyBears attackerCard = new GrizzlyBears();
        Permanent attacker = new Permanent(attackerCard);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Blocker takes no combat damage — it survives with two +1/+1 counters.
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(bearsOf(player2.getId()).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("When Vigor is put into the graveyard, a triggered ability shuffles it into its owner's library")
    void diesThenTriggerShufflesIntoLibrary() {
        harness.setLibrary(player2, new java.util.ArrayList<>());
        Permanent vigor = harness.addToBattlefieldAndReturn(player2, new Vigor());
        vigor.setMarkedDamage(6);

        harness.runStateBasedActions();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Vigor"));
        assertThat(gd.stack).isNotEmpty();

        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Vigor");
        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Vigor"));
    }
}
