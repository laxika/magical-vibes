package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.Dodecapod;
import com.github.laxika.magicalvibes.cards.e.EmblazonedGolem;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PowerstoneMinefield.class, Dodecapod.class, EmblazonedGolem.class})
class PowerstoneMinefieldTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to each attacking creature")
    void damagesAttackingCreature() {
        harness.addToBattlefield(player1, new PowerstoneMinefield());
        Permanent attacker = addCreatureReady(player2, new Dodecapod());

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deals 2 damage separately to each attacking creature")
    void damagesEachAttackingCreature() {
        harness.addToBattlefield(player1, new PowerstoneMinefield());
        Permanent firstAttacker = addCreatureReady(player2, new Dodecapod());
        Permanent secondAttacker = addCreatureReady(player2, new Dodecapod());

        declareAttackers(player2, List.of(0, 1));
        resolveAllTriggers();

        assertThat(firstAttacker.getMarkedDamage()).isEqualTo(2);
        assertThat(secondAttacker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deals 2 damage to each blocking creature")
    void damagesBlockingCreature() {
        harness.addToBattlefield(player1, new PowerstoneMinefield());
        Permanent attacker = addCreatureReady(player1, new Dodecapod());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new Dodecapod());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Damages its controller's attacking creature but not a creature that stays back")
    void damagesFriendlyAttackerOnly() {
        harness.addToBattlefield(player1, new PowerstoneMinefield());
        Permanent attacker = addCreatureReady(player1, new Dodecapod());
        Permanent idleCreature = addCreatureReady(player1, new Dodecapod());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(1));
            resolveAllTriggers();
        });

        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
        assertThat(idleCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Kills a two-toughness attacker before it can deal combat damage")
    void killsAttackerBeforeCombatDamage() {
        harness.addToBattlefield(player1, new PowerstoneMinefield());
        addCreatureReady(player2, new EmblazonedGolem());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player2, List.of(0));
            resolveAllTriggers();
        });

        harness.assertNotOnBattlefield(player2, "Emblazoned Golem");
        harness.assertInGraveyard(player2, "Emblazoned Golem");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Damages every friendly blocker once without damaging the attacker")
    void damagesEachFriendlyBlocker() {
        harness.addToBattlefield(player2, new PowerstoneMinefield());
        Permanent attacker = addCreatureReady(player1, new Dodecapod());
        attacker.setAttacking(true);
        Permanent firstBlocker = addCreatureReady(player2, new Dodecapod());
        Permanent secondBlocker = addCreatureReady(player2, new Dodecapod());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            prepareDeclareBlockers();
            gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0), new BlockerAssignment(2, 0)));
            resolveAllTriggers();
        });

        assertThat(firstBlocker.getMarkedDamage()).isEqualTo(2);
        assertThat(secondBlocker.getMarkedDamage()).isEqualTo(2);
        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Minefields controlled by both players each damage the attacker")
    void multipleMinefieldsTriggerIndependently() {
        harness.addToBattlefield(player1, new PowerstoneMinefield());
        harness.addToBattlefield(player2, new PowerstoneMinefield());
        addCreatureReady(player2, new Dodecapod());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player2, List.of(1));
            resolveAllTriggers();
        });

        harness.assertNotOnBattlefield(player2, "Dodecapod");
        harness.assertInGraveyard(player2, "Dodecapod");
    }
}
