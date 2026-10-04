package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DireWolfProwler;
import com.github.laxika.magicalvibes.cards.m.MinimusContainment;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrazilaxxIllithidScholar.class, DireWolfProwler.class, MinimusContainment.class})
class GrazilaxxIllithidScholarTest extends BaseCardTest {

    @Test
    @DisplayName("May return a creature that becomes blocked to its owner's hand")
    void returnsBlockedCreatureToOwnersHand() {
        addCreatureReady(player1, new GrazilaxxIllithidScholar());
        Permanent attacker = addCreatureReady(player1, new DireWolfProwler());
        attacker.setAttacking(true);
        addCreatureReady(player2, new DireWolfProwler());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Dire Wolf Prowler");
        harness.assertNotOnBattlefield(player1, "Dire Wolf Prowler");
    }

    @Test
    @DisplayName("Draws only one card when multiple creatures deal combat damage to a player")
    void drawsOnceForMultipleCombatDamageDealers() {
        Permanent grazilaxx = addCreatureReady(player1, new GrazilaxxIllithidScholar());
        grazilaxx.setAttacking(true);
        Permanent attacker = addCreatureReady(player1, new DireWolfProwler());
        attacker.setAttacking(true);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    void canDeclineReturningBlockedCreature() {
        addCreatureReady(player1, new GrazilaxxIllithidScholar());
        Permanent attacker = addCreatureReady(player1, new DireWolfProwler());
        attacker.setAttacking(true);
        addCreatureReady(player2, new DireWolfProwler());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveCombat();
        resolveAllTriggers();

        harness.assertNotInHand(player1, "Dire Wolf Prowler");
        harness.assertInGraveyard(player1, "Dire Wolf Prowler");
        harness.assertInGraveyard(player2, "Dire Wolf Prowler");
    }

    @Test
    void canReturnGrazilaxxItselfBeforeCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new GrazilaxxIllithidScholar());
        attacker.setAttacking(true);
        addCreatureReady(player2, new DireWolfProwler());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveCombat();
        resolveAllTriggers();

        harness.assertInHand(player1, "Grazilaxx, Illithid Scholar");
        harness.assertNotOnBattlefield(player1, "Grazilaxx, Illithid Scholar");
        harness.assertOnBattlefield(player2, "Dire Wolf Prowler");
        assertThat(findPermanent(player2, "Dire Wolf Prowler").getMarkedDamage()).isZero();
    }

    @Test
    void returnsBorrowedCreatureToItsOwner() {
        addCreatureReady(player1, new GrazilaxxIllithidScholar());
        DireWolfProwler borrowed = new DireWolfProwler();
        borrowed.setOwnerId(player2.getId());
        Permanent attacker = addCreatureReady(player1, borrowed);
        attacker.setAttacking(true);
        addCreatureReady(player2, new DireWolfProwler());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player2.getId())).contains(borrowed);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(borrowed);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
    }

    @Test
    void doesNotDrawForOpponentsCombatDamage() {
        addCreatureReady(player1, new GrazilaxxIllithidScholar());
        Permanent attacker = addCreatureReady(player2, new DireWolfProwler());
        attacker.setAttacking(true);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat(player2);
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    void doesNotTriggerWhenGrazilaxxHasLostItsAbilities() {
        Permanent grazilaxx = addCreatureReady(player1, new GrazilaxxIllithidScholar());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new MinimusContainment());
        aura.setAttachedTo(grazilaxx.getId());
        Permanent attacker = addCreatureReady(player1, new DireWolfProwler());
        attacker.setAttacking(true);
        addCreatureReady(player2, new DireWolfProwler());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 1)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.assertNotInHand(player1, "Dire Wolf Prowler");
    }
}
