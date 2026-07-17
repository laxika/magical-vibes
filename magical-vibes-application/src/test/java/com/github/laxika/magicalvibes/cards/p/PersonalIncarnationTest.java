package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PersonalIncarnationTest extends BaseCardTest {

    @Test
    @DisplayName("Activating {0} registers a next-1-damage redirect shield destined for the owner")
    void activationCreatesShieldToOwner() {
        Permanent incarnation = addReadyPermanent(player1, new PersonalIncarnation());

        harness.activateAbility(player1, indexOf(player1, incarnation), null, null);
        harness.passBothPriorities();

        assertThat(gd.creatureDamageRedirectShields).hasSize(1);
        var shield = gd.creatureDamageRedirectShields.getFirst();
        assertThat(shield.protectedPermanentId()).isEqualTo(incarnation.getId());
        assertThat(shield.damageSourceId()).isNull();
        assertThat(shield.remainingAmount()).isEqualTo(1);
        assertThat(shield.redirectTargetId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("The next 1 damage to Personal Incarnation is dealt to its owner instead")
    void redirectsDamageToOwner() {
        Permanent incarnation = addReadyPermanent(player1, new PersonalIncarnation());
        Permanent pyromancer = addReadyPermanent(player1, new ProdigalPyromancer());
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, indexOf(player1, incarnation), null, null);
        harness.passBothPriorities();

        // Pyromancer pings the Incarnation for 1 — that 1 damage is dealt to its owner instead
        harness.activateAbility(player1, indexOf(player1, pyromancer), null, incarnation.getId());
        harness.passBothPriorities();

        assertThat(incarnation.getMarkedDamage()).isEqualTo(0);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("When Personal Incarnation dies, its owner loses half their life, rounded up (odd life)")
    void deathTriggerLosesHalfLifeRoundedUp() {
        harness.addToBattlefield(player1, new PersonalIncarnation());
        gd.playerLifeTotals.put(player1.getId(), 15);

        setupCombatWhereIncarnationDies();
        harness.passBothPriorities(); // combat damage — Incarnation dies
        harness.passBothPriorities(); // resolve the death trigger

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Personal Incarnation"));
        // 15 / 2 rounded up = 8; 15 - 8 = 7
        assertThat(gd.getLife(player1.getId())).isEqualTo(7);
    }

    @Test
    @DisplayName("Half-life loss rounds up from an even life total")
    void deathTriggerRoundsFromEvenLife() {
        harness.addToBattlefield(player1, new PersonalIncarnation());
        gd.playerLifeTotals.put(player1.getId(), 20);

        setupCombatWhereIncarnationDies();
        harness.passBothPriorities(); // combat damage — Incarnation dies
        harness.passBothPriorities(); // resolve the death trigger

        // 20 / 2 = 10; 20 - 10 = 10
        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
    }

    // ===== Helpers =====

    /** Personal Incarnation (player1) attacks and is blocked by a 6/6 (player2); the 6/6 kills it. */
    private void setupCombatWhereIncarnationDies() {
        Permanent incarnation = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Personal Incarnation"))
                .findFirst().orElseThrow();
        incarnation.setSummoningSick(false);
        incarnation.setAttacking(true);

        GrizzlyBears bigBear = new GrizzlyBears();
        bigBear.setPower(6);
        bigBear.setToughness(6);
        Permanent blocker = new Permanent(bigBear);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
