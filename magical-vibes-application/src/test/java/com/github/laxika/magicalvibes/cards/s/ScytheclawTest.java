package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Scytheclaw.class, GrizzlyBears.class})
class ScytheclawTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+1")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent scytheclaw = harness.addToBattlefieldAndReturn(player1, new Scytheclaw());
        scytheclaw.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Living weapon creates and attaches a Phyrexian Germ")
    void livingWeaponCreatesAndAttachesGerm() {
        harness.castFromHand(player1, new Scytheclaw(), "{5}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent scytheclaw = findPermanent(player1, "Scytheclaw");
        Permanent germ = findPermanent(player1, "Phyrexian Germ");

        assertThat(scytheclaw.getAttachedTo()).isEqualTo(germ.getId());
        assertThat(gqs.getEffectivePower(gd, germ)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, germ)).isEqualTo(1);
    }

    @Test
    @DisplayName("Equipped creature makes a player lose half their life, rounded up")
    void combatDamageMakesPlayerLoseHalfLifeRoundedUp() {
        harness.setLife(player2, 24);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent scytheclaw = harness.addToBattlefieldAndReturn(player1, new Scytheclaw());
        scytheclaw.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        // 3 combat damage: 24 -> 21. Half of 21 rounded up is 11: 21 -> 10.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Blocked equipped creature does not trigger the life-loss ability")
    void blockedCreatureDoesNotTriggerLifeLoss() {
        harness.setLife(player2, 23);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent scytheclaw = harness.addToBattlefieldAndReturn(player1, new Scytheclaw());
        scytheclaw.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Equip costs three mana and moves Scytheclaw from its Germ")
    void equipMovesFromGermToCreature() {
        harness.castFromHand(player1, new Scytheclaw(), "{5}");
        harness.passBothPriorities();
        resolveAllTriggers();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent scytheclaw = findPermanent(player1, "Scytheclaw");
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(scytheclaw.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(countPermanents(player1, "Phyrexian Germ")).isZero();
    }

    @Test
    @DisplayName("An even life total after combat damage is halved exactly")
    void evenLifeTotalIsHalved() {
        harness.setLife(player2, 23);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent scytheclaw = harness.addToBattlefieldAndReturn(player1, new Scytheclaw());
        scytheclaw.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        harness.resolveCombatDamage();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Life loss uses the player's life total when the trigger resolves")
    void lifeTotalIsEvaluatedAtResolution() {
        harness.setLife(player2, 23);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent scytheclaw = harness.addToBattlefieldAndReturn(player1, new Scytheclaw());
        scytheclaw.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        harness.resolveCombatDamage();
        harness.setLife(player2, 15);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(7);
    }

    @Test
    @DisplayName("Detaching Scytheclaw after combat damage does not stop its trigger")
    void triggerSurvivesDetachment() {
        harness.setLife(player2, 24);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent scytheclaw = harness.addToBattlefieldAndReturn(player1, new Scytheclaw());
        scytheclaw.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        harness.resolveCombatDamage();
        scytheclaw.setAttachedTo(null);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("An unattached Scytheclaw does not trigger for another creature")
    void unattachedEquipmentDoesNotTrigger() {
        harness.setLife(player2, 23);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Scytheclaw());
        creature.setAttacking(true);

        harness.resolveCombatDamage();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(21);
    }
}
