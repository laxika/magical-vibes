package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GoblinTestPilotTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to exactly one randomly chosen any-target")
    void dealsTwoDamageToOneRandomAnyTarget() {
        Permanent pilot = addCreatureReady(player1, new GoblinTestPilot());
        Permanent friendly = addCreatureReady(player1, new AvatarOfMight());
        Permanent enemy = addCreatureReady(player2, new AvatarOfMight());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        int lifeLost = (20 - gd.playerLifeTotals.get(player1.getId()))
                + (20 - gd.playerLifeTotals.get(player2.getId()));
        int creatureDamage = friendly.getMarkedDamage() + enemy.getMarkedDamage()
                + (gd.playerBattlefields.get(player1.getId()).contains(pilot) ? pilot.getMarkedDamage() : 2);
        assertThat(lifeLost + creatureDamage).isEqualTo(2);
        assertThat(pilot.isTapped() || !gd.playerBattlefields.get(player1.getId()).contains(pilot)).isTrue();
    }

    @Test
    @DisplayName("Noncreature, nonplaneswalker permanents are never in the random pool")
    void neverHitsNoncreaturePermanents() {
        addCreatureReady(player1, new GoblinTestPilot());
        Permanent artifact = addCreatureReady(player1, new FountainOfYouth());
        Permanent land = addCreatureReady(player2, new Forest());
        addCreatureReady(player2, new AvatarOfMight());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(artifact.getMarkedDamage()).isZero();
        assertThat(land.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Over many activations the recipient varies")
    void recipientVariesAcrossActivations() {
        Permanent pilot = addCreatureReady(player1, new GoblinTestPilot());
        Permanent enemy = addCreatureReady(player2, new AvatarOfMight());
        // The 0/2 pilot is itself in the pool; pad its toughness so a self-hit doesn't end the loop.
        pilot.setToughnessModifier(5);

        Set<UUID> recipients = new HashSet<>();
        for (int i = 0; i < 40; i++) {
            pilot.untap();
            harness.setLife(player1, 20);
            harness.setLife(player2, 20);
            enemy.setMarkedDamage(0);
            pilot.setMarkedDamage(0);

            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();

            if (gd.playerLifeTotals.get(player1.getId()) < 20) recipients.add(player1.getId());
            if (gd.playerLifeTotals.get(player2.getId()) < 20) recipients.add(player2.getId());
            if (enemy.getMarkedDamage() > 0) recipients.add(enemy.getId());
            if (pilot.getMarkedDamage() > 0) recipients.add(pilot.getId());
        }

        assertThat(recipients).hasSizeGreaterThan(1);
    }
}
