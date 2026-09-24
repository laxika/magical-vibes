package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GoblinBrawler;
import com.github.laxika.magicalvibes.cards.w.WayfarersBauble;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MagmaGiant.class, GoblinBrawler.class, WayfarersBauble.class})
class MagmaGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack as a creature spell")
    void castingPutsOnStack() {
        castGiant();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("ETB deals 2 damage to each player")
    void etbDamagesEachPlayer() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castGiant();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("ETB deals 2 damage to each creature on both sides, killing 2/2s")
    void etbKillsSmallCreaturesBothSides() {
        harness.addToBattlefield(player1, new GoblinBrawler()); // 2/2 own
        harness.addToBattlefield(player2, new GoblinBrawler()); // 2/2 opponent

        castGiant();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Goblin Brawler");
        harness.assertNotOnBattlefield(player2, "Goblin Brawler");
    }

    @Test
    @DisplayName("ETB does not damage noncreature permanents")
    void etbLeavesNoncreaturePermanentsUntouched() {
        Permanent bauble = harness.addToBattlefieldAndReturn(player1, new WayfarersBauble());

        castGiant();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Wayfarer's Bauble");
        assertThat(bauble.getMarkedDamage()).isEqualTo(0);
    }

    @Test
    @DisplayName("ETB damages the Giant itself, which survives as a 5/5")
    void etbDamagesItselfButSurvives() {
        castGiant();
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Magma Giant").getMarkedDamage()).isEqualTo(2);
    }

    private void castGiant() {
        harness.castFromHand(player1, new MagmaGiant(), "{5}{R}{R}");
    }
}
