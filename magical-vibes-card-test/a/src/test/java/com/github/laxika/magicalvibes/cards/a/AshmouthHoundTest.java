package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DarkthicketWolf;
import com.github.laxika.magicalvibes.cards.l.Lumberknot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AshmouthHound.class, DarkthicketWolf.class, AvacynsPilgrim.class, Lumberknot.class})
class AshmouthHoundTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking creates a trigger that deals 1 damage to the attacker")
    void blockingDeals1DamageToAttacker() {
        addCreatureReady(player2, new AshmouthHound());
        Permanent attacker = addCreatureReady(player1, new DarkthicketWolf());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Ashmouth Hound");
        assertThat(entry.getTargetId()).isEqualTo(attacker.getId());

        harness.passBothPriorities();

        // Attacker (2/2) takes 1 damage but survives
        harness.assertOnBattlefield(player1, "Darkthicket Wolf");
        Permanent damagedAttacker = findPermanent(player1, "Darkthicket Wolf");
        assertThat(damagedAttacker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Becoming blocked creates a trigger that deals 1 damage to the blocker")
    void becomingBlockedDeals1DamageToBlocker() {
        Permanent hound = addCreatureReady(player1, new AshmouthHound());
        hound.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new DarkthicketWolf());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getTargetId()).isEqualTo(blocker.getId());
        assertThat(entry.getSourcePermanentId()).isEqualTo(hound.getId());

        harness.passBothPriorities();

        // Blocker (2/2) takes 1 damage but survives
        harness.assertOnBattlefield(player2, "Darkthicket Wolf");
        Permanent damagedBlocker = findPermanent(player2, "Darkthicket Wolf");
        assertThat(damagedBlocker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Becoming blocked by multiple creatures creates one trigger per blocker")
    void becomingBlockedByMultipleCreaturesCreatesMultipleTriggers() {
        Permanent hound = addCreatureReady(player1, new AshmouthHound());
        hound.setAttacking(true);
        addCreatureReady(player2, new DarkthicketWolf());
        addCreatureReady(player2, new DarkthicketWolf());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        long triggerCount = gd.stack.stream()
                .filter(e -> e.getCard().getName().equals("Ashmouth Hound"))
                .count();
        assertThat(triggerCount).isEqualTo(2);

        resolveAllTriggers();

        // Both blockers (2/2) take 1 damage but survive
        List<Permanent> bears = findPermanents(player2, "Darkthicket Wolf");
        assertThat(bears).hasSize(2);
        assertThat(bears).allMatch(p -> p.getMarkedDamage() == 1);
    }

    @Test
    @DisplayName("Becomes-blocked trigger references its blocker without targeting")
    void blockTriggerIsNonTargeting() {
        Permanent hound = addCreatureReady(player1, new AshmouthHound());
        hound.setAttacking(true);
        addCreatureReady(player2, new DarkthicketWolf());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.isNonTargeting()).isTrue();
    }

    @Test
    @DisplayName("The trigger kills a one-toughness blocker before combat damage")
    void killsBlockerBeforeCombatDamage() {
        Permanent hound = addCreatureReady(player1, new AshmouthHound());
        hound.setAttacking(true);
        addCreatureReady(player2, new AvacynsPilgrim());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Avacyn's Pilgrim");
        harness.assertNotOnBattlefield(player2, "Avacyn's Pilgrim");
        assertThat(hound.getMarkedDamage()).isZero();

        harness.resolveCombatDamage();

        harness.assertOnBattlefield(player1, "Ashmouth Hound");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The block trigger kills a one-toughness attacker before combat damage")
    void killsAttackerBeforeCombatDamage() {
        Permanent hound = addCreatureReady(player2, new AshmouthHound());
        Permanent attacker = addCreatureReady(player1, new AvacynsPilgrim());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Avacyn's Pilgrim");
        harness.assertNotOnBattlefield(player1, "Avacyn's Pilgrim");
        assertThat(hound.getMarkedDamage()).isZero();
        harness.resolveCombatDamage();
        harness.assertOnBattlefield(player2, "Ashmouth Hound");
    }

    @Test
    @DisplayName("A hexproof blocker still takes damage from the non-targeting trigger")
    void damagesHexproofBlocker() {
        Permanent hound = addCreatureReady(player1, new AshmouthHound());
        hound.setAttacking(true);
        addCreatureReady(player2, new Lumberknot());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Lumberknot");
        harness.assertInGraveyard(player2, "Lumberknot");
        harness.assertOnBattlefield(player1, "Ashmouth Hound");
    }

    @Test
    @DisplayName("A becomes-blocked trigger still deals damage after the Hound dies")
    void triggerResolvesAfterSourceDies() {
        Permanent hound = addCreatureReady(player1, new AshmouthHound());
        hound.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new DarkthicketWolf());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        hound.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Ashmouth Hound");

        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Darkthicket Wolf");
        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("A trigger does not damage a different creature when its blocker dies")
    void departedBlockerIsNotReplacedByAnotherCreature() {
        Permanent hound = addCreatureReady(player1, new AshmouthHound());
        hound.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new DarkthicketWolf());
        Permanent otherCreature = addCreatureReady(player2, new DarkthicketWolf());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        blocker.setMarkedDamage(2);
        harness.runStateBasedActions();

        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Darkthicket Wolf");
        assertThat(findPermanents(player2, "Darkthicket Wolf")).containsExactly(otherCreature);
        assertThat(otherCreature.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

}
