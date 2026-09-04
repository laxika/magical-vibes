package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Sacrifice;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PersonalIncarnation.class, ProdigalSorcerer.class, GrizzlyBears.class, Sacrifice.class, PlatinumAngel.class})
class PersonalIncarnationTest extends BaseCardTest {

    @Test
    void negativeLifeTotalRemainsUnchanged() {
        PersonalIncarnation card = new PersonalIncarnation();
        card.setOwnerId(player1.getId());
        Permanent incarnation = addCreatureReady(player1, card);
        addCreatureReady(player1, new PlatinumAngel());
        gd.playerLifeTotals.put(player1.getId(), -10);

        harness.setHand(player1, List.of(new Sacrifice()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstantWithSacrifice(player1, 0, null, incarnation.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(-10);
    }

    @Test
    void onlyOwnerMayActivateAbility() {
        PersonalIncarnation card = new PersonalIncarnation();
        card.setOwnerId(player1.getId());
        Permanent incarnation = addCreatureReady(player2, card);
        gd.stolenCreatures.put(incarnation.getId(), player1.getId());

        assertThatThrownBy(() -> harness.activateAbility(player2, indexOf(player2, incarnation), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void deathTriggerAffectsOwnerNotController() {
        PersonalIncarnation card = new PersonalIncarnation();
        card.setOwnerId(player1.getId());
        Permanent incarnation = addCreatureReady(player2, card);
        gd.stolenCreatures.put(incarnation.getId(), player1.getId());
        gd.playerLifeTotals.put(player1.getId(), 20);
        gd.playerLifeTotals.put(player2.getId(), 20);

        harness.setHand(player2, List.of(new Sacrifice()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstantWithSacrifice(player2, 0, null, incarnation.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Activating {0} registers a next-1-damage redirect shield destined for the owner")
    void activationCreatesShieldToOwner() {
        Permanent incarnation = addCreatureReady(player1, new PersonalIncarnation());

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
        Permanent incarnation = addCreatureReady(player1, new PersonalIncarnation());
        Permanent sorcerer = addCreatureReady(player1, new ProdigalSorcerer());
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, indexOf(player1, incarnation), null, null);
        harness.passBothPriorities();

        // Sorcerer pings the Incarnation for 1 — that 1 damage is dealt to its owner instead
        harness.activateAbility(player1, indexOf(player1, sorcerer), null, incarnation.getId());
        harness.passBothPriorities();

        assertThat(incarnation.getMarkedDamage()).isEqualTo(0);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("When Personal Incarnation dies, its owner loses half their life, rounded up (odd life)")
    void deathTriggerLosesHalfLifeRoundedUp() {
        addCreatureReady(player1, new PersonalIncarnation());
        gd.playerLifeTotals.put(player1.getId(), 15);

        setupCombatWhereIncarnationDies();
        harness.passBothPriorities(); // combat damage — Incarnation dies
        harness.passBothPriorities(); // resolve the death trigger

        harness.assertInGraveyard(player1, "Personal Incarnation");
        // 15 / 2 rounded up = 8; 15 - 8 = 7
        assertThat(gd.getLife(player1.getId())).isEqualTo(7);
    }

    @Test
    @DisplayName("Half-life loss rounds up from an even life total")
    void deathTriggerRoundsFromEvenLife() {
        addCreatureReady(player1, new PersonalIncarnation());
        gd.playerLifeTotals.put(player1.getId(), 20);

        setupCombatWhereIncarnationDies();
        harness.passBothPriorities(); // combat damage — Incarnation dies
        harness.passBothPriorities(); // resolve the death trigger

        // 20 / 2 = 10; 20 - 10 = 10
        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
    }

    /** Personal Incarnation (player1) attacks and is blocked by a 6/6 (player2); the 6/6 kills it. */
    private void setupCombatWhereIncarnationDies() {
        Permanent incarnation = findPermanent(player1, "Personal Incarnation");
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

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
