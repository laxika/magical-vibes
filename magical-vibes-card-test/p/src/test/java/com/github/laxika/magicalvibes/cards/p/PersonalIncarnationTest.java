package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
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

@CardUsed({PersonalIncarnation.class, ProdigalSorcerer.class, CrawWurm.class, Sacrifice.class, PlatinumAngel.class})
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
        resolveAllTriggers();
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
        harness.setLife(player1, 15);
        harness.setLife(player2, 20);

        harness.setHand(player2, List.of(new Sacrifice()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstantWithSacrifice(player2, 0, null, incarnation.getId());
        resolveAllTriggers();
        harness.assertLife(player1, 7);
        harness.assertLife(player2, 20);
    }

    @Test
    void aSingleActivationRedirectsOnlyOneDamage() {
        Permanent incarnation = addCreatureReady(player1, new PersonalIncarnation());
        Permanent firstSorcerer = addCreatureReady(player1, new ProdigalSorcerer());
        Permanent secondSorcerer = addCreatureReady(player1, new ProdigalSorcerer());

        harness.activateAbility(player1, indexOf(player1, incarnation), null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, indexOf(player1, firstSorcerer), null, incarnation.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, indexOf(player1, secondSorcerer), null, incarnation.getId());
        harness.passBothPriorities();

        assertThat(incarnation.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player1, 19);
    }

    @Test
    void ownerMayActivateWhileOpponentControlsIncarnation() {
        PersonalIncarnation card = new PersonalIncarnation();
        card.setOwnerId(player1.getId());
        Permanent incarnation = addCreatureReady(player2, card);
        gd.stolenCreatures.put(incarnation.getId(), player1.getId());

        harness.activateAbility(player1, indexOf(player2, incarnation), null, null);
        harness.passBothPriorities();
        Permanent sorcerer = addCreatureReady(player1, new ProdigalSorcerer());
        harness.activateAbility(player1, indexOf(player1, sorcerer), null, incarnation.getId());
        harness.passBothPriorities();

        assertThat(incarnation.getMarkedDamage()).isZero();
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    void repeatedActivationsAccumulateForCombatDamage() {
        Permanent incarnation = addCreatureReady(player1, new PersonalIncarnation());
        for (int i = 0; i < 2; i++) {
            harness.activateAbility(player1, indexOf(player1, incarnation), null, null);
            harness.passBothPriorities();
        }

        setupCombatWhereIncarnationDies();
        resolveCombat();

        assertThat(incarnation.getMarkedDamage()).isEqualTo(4);
        harness.assertOnBattlefield(player1, "Personal Incarnation");
        harness.assertLife(player1, 18);
    }

    @Test
    void unusedRedirectionExpiresAtEndOfTurn() {
        Permanent incarnation = addCreatureReady(player1, new PersonalIncarnation());
        Permanent sorcerer = addCreatureReady(player1, new ProdigalSorcerer());
        harness.activateAbility(player1, indexOf(player1, incarnation), null, null);
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        advanceToUpkeep(player2);
        harness.activateAbility(player1, indexOf(player1, sorcerer), null, incarnation.getId());
        harness.passBothPriorities();

        assertThat(incarnation.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player1, 20);
    }

    @Test
    void deathTriggerUsesLifeTotalAtResolution() {
        Permanent incarnation = addCreatureReady(player1, new PersonalIncarnation());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Sacrifice()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstantWithSacrifice(player1, 0, null, incarnation.getId());
        harness.setLife(player1, 9);
        resolveAllTriggers();

        harness.assertLife(player1, 4);
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
        resolveCombat(); // combat damage — Incarnation dies
        resolveAllTriggers();

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
        resolveCombat(); // combat damage — Incarnation dies
        resolveAllTriggers();

        // 20 / 2 = 10; 20 - 10 = 10
        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
    }

    /** Personal Incarnation attacks and is blocked by Craw Wurm; both die without redirection. */
    private void setupCombatWhereIncarnationDies() {
        Permanent incarnation = findPermanent(player1, "Personal Incarnation");
        incarnation.setSummoningSick(false);
        incarnation.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new CrawWurm());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
