package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.l.LeylineOfPunishment;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({PhyrexianHydra.class, GrizzlyBears.class, Shock.class, LeylineOfPunishment.class})
class PhyrexianHydraTest extends BaseCardTest {

    @Test
    @DisplayName("Shock damage is prevented and replaced with -1/-1 counters")
    void shockDamageReplacedWithCounters() {
        harness.addToBattlefield(player2, new PhyrexianHydra());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID hydraId = harness.getPermanentId(player2, "Phyrexian Hydra");
        harness.castAndResolveInstant(player1, 0, hydraId);

        // Hydra survives
        harness.assertOnBattlefield(player2, "Phyrexian Hydra");
        // Hydra has 2 -1/-1 counters from Shock's 2 damage
        Permanent hydra = findPermanent(player2, "Phyrexian Hydra");
        assertThat(hydra.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Combat damage to Phyrexian Hydra is replaced with -1/-1 counters")
    void combatDamageReplacedWithCounters() {
        // Phyrexian Hydra (7/7) blocks Grizzly Bears (2/2)
        PhyrexianHydra hydraCard = new PhyrexianHydra();
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, hydraCard);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        GrizzlyBears bears = new GrizzlyBears();
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, bears);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Hydra survives with 2 -1/-1 counters from Bears' 2 power
        harness.assertOnBattlefield(player2, "Phyrexian Hydra");
        Permanent hydra = findPermanent(player2, "Phyrexian Hydra");
        assertThat(hydra.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);

        // Bears dies from Hydra's 7 power (infect → -1/-1 counters, toughness 0)
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Phyrexian Hydra dies when enough -1/-1 counters reduce toughness to 0")
    void diesWhenCountersReduceToughnessToZero() {
        // 7/7 Hydra blocks a 7/7 creature — gets 7 -1/-1 counters → toughness 0 → dies
        PhyrexianHydra hydraCard = new PhyrexianHydra();
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, hydraCard);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        GrizzlyBears bigCreature = new GrizzlyBears();
        bigCreature.setPower(7);
        bigCreature.setToughness(7);
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, bigCreature);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Hydra dies from 7 -1/-1 counters (toughness = 0)
        harness.assertNotOnBattlefield(player2, "Phyrexian Hydra");
    }

    @Test
    @DisplayName("Phyrexian Hydra deals poison counters to defending player when attacking unblocked")
    void dealsPoisonCountersToPlayer() {
        harness.setLife(player2, 20);

        PhyrexianHydra hydraCard = new PhyrexianHydra();
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, hydraCard);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Infect: player gets 7 poison counters, no life loss
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(7);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Phyrexian Hydra puts -1/-1 counters on creature it deals combat damage to")
    void putsMinusCountersOnBlockingCreature() {
        // Hydra (7/7 infect) attacks, blocked by a 10/10 creature
        PhyrexianHydra hydraCard = new PhyrexianHydra();
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, hydraCard);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        GrizzlyBears bigCreature = new GrizzlyBears();
        bigCreature.setPower(3);
        bigCreature.setToughness(10);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, bigCreature);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Blocker gets 7 -1/-1 counters from Hydra's infect damage
        Permanent survivingBlocker = findPermanent(player2, "Grizzly Bears");
        assertThat(survivingBlocker).isNotNull();
        assertThat(survivingBlocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(7);

        // Hydra gets 3 -1/-1 counters from blocking creature's 3 power
        Permanent hydra = findPermanent(player1, "Phyrexian Hydra");
        assertThat(hydra.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Unpreventable damage is dealt normally without adding counters")
    void unpreventableDamageIsNotConvertedToCounters() {
        Permanent hydra = harness.addToBattlefieldAndReturn(player2, new PhyrexianHydra());
        harness.addToBattlefield(player1, new LeylineOfPunishment());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, hydra.getId());

        harness.assertOnBattlefield(player2, "Phyrexian Hydra");
        assertThat(hydra.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(hydra.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Separate damage events accumulate counters without marking damage")
    void separateDamageEventsAccumulateCounters() {
        Permanent hydra = harness.addToBattlefieldAndReturn(player2, new PhyrexianHydra());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, hydra.getId());
        harness.castAndResolveInstant(player1, 0, hydra.getId());

        harness.assertOnBattlefield(player2, "Phyrexian Hydra");
        assertThat(hydra.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(4);
        assertThat(hydra.getMarkedDamage()).isZero();
    }
}
