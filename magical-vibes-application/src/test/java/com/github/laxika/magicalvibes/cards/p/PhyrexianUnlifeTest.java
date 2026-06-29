package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CantLoseGameFromLifeEffect;
import com.github.laxika.magicalvibes.model.effect.DamageDealtAsInfectBelowZeroLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PhyrexianUnlifeTest extends BaseCardTest {

    @Test
    @DisplayName("Phyrexian Unlife has both static effects")
    void hasBothStaticEffects() {
        PhyrexianUnlife card = new PhyrexianUnlife();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.STATIC))
                .anyMatch(e -> e instanceof CantLoseGameFromLifeEffect)
                .anyMatch(e -> e instanceof DamageDealtAsInfectBelowZeroLifeEffect);
    }

    @Test
    @DisplayName("Controller doesn't lose at 0 life with Phyrexian Unlife")
    void controllerDoesNotLoseAtZeroLife() {
        harness.addToBattlefield(player1, new PhyrexianUnlife());
        harness.setLife(player1, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(0);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Controller doesn't lose at negative life with Phyrexian Unlife")
    void controllerDoesNotLoseAtNegativeLife() {
        harness.addToBattlefield(player1, new PhyrexianUnlife());
        harness.setLife(player1, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(-1);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Damage is dealt as poison counters when at 0 or less life")
    void damageDealtAsPoisonWhenAtZeroOrLessLife() {
        harness.addToBattlefield(player1, new PhyrexianUnlife());
        harness.setLife(player1, 0);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        // Life should stay at 0 — damage dealt as poison instead
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(0);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(2);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Damage that brings life to 0 or below is normal damage, not poison")
    void damageToZeroIsNormalNotPoison() {
        harness.addToBattlefield(player1, new PhyrexianUnlife());
        harness.setLife(player1, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        // Damage that brings you TO 0 is normal life loss, not poison
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(0);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("Controller still loses from 10 poison counters with Phyrexian Unlife")
    void controllerLosesFromPoisonCounters() {
        harness.addToBattlefield(player1, new PhyrexianUnlife());
        harness.setLife(player1, 0);
        gd.playerPoisonCounters.put(player1.getId(), 8);

        // Deal 2 more poison (Shock while at 0 life = 2 poison)
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(10);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Player loses normally without Phyrexian Unlife at 0 life")
    void playerLosesNormallyWithoutPhyrexianUnlife() {
        harness.setLife(player1, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(0);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Normal damage is dealt when life is above 0 even with Phyrexian Unlife")
    void normalDamageAboveZeroLife() {
        harness.addToBattlefield(player1, new PhyrexianUnlife());
        harness.setLife(player1, 10);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        // Normal damage reduces life, no poison
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(8);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("Player loses after Phyrexian Unlife is removed while at 0 life")
    void playerLosesAfterPhyrexianUnlifeRemoved() {
        harness.addToBattlefield(player1, new PhyrexianUnlife());
        harness.setLife(player1, 0);

        // Remove Phyrexian Unlife
        gd.playerBattlefields.get(player1.getId()).clear();

        // Trigger a win check via damage
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Opponent's Phyrexian Unlife does not protect you")
    void opponentsPhyrexianUnlifeDoesNotProtect() {
        harness.addToBattlefield(player2, new PhyrexianUnlife());
        harness.setLife(player1, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(0);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Combat damage is dealt as poison even when defender's lifelink creature heals during same combat")
    void combatDamageDealtAsPoisonEvenWhenLifelinkHealsDefender() {
        // Defender (player1) has Phyrexian Unlife and is at -1 life
        harness.addToBattlefield(player1, new PhyrexianUnlife());
        harness.setLife(player1, -1);

        // Defender has a 2/2 lifelink creature that blocks
        GrizzlyBears defenderBear = new GrizzlyBears();
        defenderBear.setKeywords(EnumSet.of(Keyword.LIFELINK));
        Permanent blocker = addCreatureReady(player1, defenderBear);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        // Attacker (player2) has a 3/3 that attacks unblocked
        // and a 2/2 that the lifelink creature blocks
        GrizzlyBears attackerBlocked = new GrizzlyBears();
        Permanent blockedAttacker = addCreatureReady(player2, attackerBlocked);
        blockedAttacker.setAttacking(true);

        GrizzlyBears attackerUnblocked = new GrizzlyBears();
        Permanent unblockedAttacker = addCreatureReady(player2, attackerUnblocked);
        unblockedAttacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Pass priority through declare blockers → combat damage
        harness.passBothPriorities();

        // The unblocked 2/2 deals 2 damage to defender.
        // Since defender was at -1 life BEFORE combat, this should be 2 poison (not life loss).
        // Lifelink gaining life during the same combat should NOT prevent the infect conversion.
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(2);

        // Lifelink should still gain 2 life (2/2 blocker deals 2 damage to blocked attacker)
        // Life was -1, lifelink adds 2, so life should be 1
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(1);
    }
}
