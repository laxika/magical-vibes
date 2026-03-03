package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.PreventDamageAndAddMinusCountersEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PhyrexianHydraTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Phyrexian Hydra has PreventDamageAndAddMinusCountersEffect as static effect")
    void hasCorrectStaticEffect() {
        PhyrexianHydra card = new PhyrexianHydra();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(PreventDamageAndAddMinusCountersEffect.class);
    }

    // ===== Spell damage is replaced with -1/-1 counters =====

    @Test
    @DisplayName("Shock damage is prevented and replaced with -1/-1 counters")
    void shockDamageReplacedWithCounters() {
        harness.addToBattlefield(player2, new PhyrexianHydra());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID hydraId = harness.getPermanentId(player2, "Phyrexian Hydra");
        harness.castInstant(player1, 0, hydraId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Hydra survives
        harness.assertOnBattlefield(player2, "Phyrexian Hydra");
        // Hydra has 2 -1/-1 counters from Shock's 2 damage
        Permanent hydra = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Hydra"))
                .findFirst().orElseThrow();
        assertThat(hydra.getMinusOneMinusOneCounters()).isEqualTo(2);
    }

    // ===== Combat damage is replaced with -1/-1 counters =====

    @Test
    @DisplayName("Combat damage to Phyrexian Hydra is replaced with -1/-1 counters")
    void combatDamageReplacedWithCounters() {
        // Phyrexian Hydra (7/7) blocks Grizzly Bears (2/2)
        PhyrexianHydra hydraCard = new PhyrexianHydra();
        Permanent blocker = new Permanent(hydraCard);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(blocker);

        GrizzlyBears bears = new GrizzlyBears();
        Permanent attacker = new Permanent(bears);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Hydra survives with 2 -1/-1 counters from Bears' 2 power
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Phyrexian Hydra"));
        Permanent hydra = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Hydra"))
                .findFirst().orElseThrow();
        assertThat(hydra.getMinusOneMinusOneCounters()).isEqualTo(2);

        // Bears dies from Hydra's 7 power (infect → -1/-1 counters, toughness 0)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    // ===== Lethal damage via counters kills the Hydra =====

    @Test
    @DisplayName("Phyrexian Hydra dies when enough -1/-1 counters reduce toughness to 0")
    void diesWhenCountersReduceToughnessToZero() {
        // 7/7 Hydra blocks a 7/7 creature — gets 7 -1/-1 counters → toughness 0 → dies
        PhyrexianHydra hydraCard = new PhyrexianHydra();
        Permanent blocker = new Permanent(hydraCard);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(blocker);

        GrizzlyBears bigCreature = new GrizzlyBears();
        bigCreature.setPower(7);
        bigCreature.setToughness(7);
        Permanent attacker = new Permanent(bigCreature);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Hydra dies from 7 -1/-1 counters (toughness = 0)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Phyrexian Hydra"));
    }

    // ===== Infect deals poison counters to players =====

    @Test
    @DisplayName("Phyrexian Hydra deals poison counters to defending player when attacking unblocked")
    void dealsPoisonCountersToPlayer() {
        harness.setLife(player2, 20);

        PhyrexianHydra hydraCard = new PhyrexianHydra();
        Permanent attacker = new Permanent(hydraCard);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Infect: player gets 7 poison counters, no life loss
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(7);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    // ===== Infect puts -1/-1 counters on blocking creature =====

    @Test
    @DisplayName("Phyrexian Hydra puts -1/-1 counters on creature it deals combat damage to")
    void putsMinusCountersOnBlockingCreature() {
        // Hydra (7/7 infect) attacks, blocked by a 10/10 creature
        PhyrexianHydra hydraCard = new PhyrexianHydra();
        Permanent attacker = new Permanent(hydraCard);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(attacker);

        GrizzlyBears bigCreature = new GrizzlyBears();
        bigCreature.setPower(3);
        bigCreature.setToughness(10);
        Permanent blocker = new Permanent(bigCreature);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Blocker gets 7 -1/-1 counters from Hydra's infect damage
        Permanent survivingBlocker = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElse(null);
        assertThat(survivingBlocker).isNotNull();
        assertThat(survivingBlocker.getMinusOneMinusOneCounters()).isEqualTo(7);

        // Hydra gets 3 -1/-1 counters from blocking creature's 3 power
        Permanent hydra = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Hydra"))
                .findFirst().orElseThrow();
        assertThat(hydra.getMinusOneMinusOneCounters()).isEqualTo(3);
    }
}
