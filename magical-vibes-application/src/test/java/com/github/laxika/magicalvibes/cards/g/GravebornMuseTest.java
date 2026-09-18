package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.e.EnormousBaloth;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GravebornMuse.class, GempalmPolluter.class, EnormousBaloth.class})
class GravebornMuseTest extends BaseCardTest {

    // ===== Triggering =====

    @Test
    @DisplayName("Draws 1 and loses 1 life when only Graveborn Muse is the only Zombie")
    void drawsAndLosesLifeForSelfAsZombie() {
        harness.addToBattlefield(player1, new GravebornMuse());
        harness.setHand(player1, List.of());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
        harness.assertLife(player1, lifeBefore - 1);
    }

    @Test
    @DisplayName("Draws and loses life equal to total Zombie count")
    void drawsAndLosesLifeEqualToZombieCount() {
        harness.addToBattlefield(player1, new GravebornMuse());
        harness.addToBattlefield(player1, new GempalmPolluter());
        harness.setHand(player1, List.of());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        // 2 Zombies: Graveborn Muse + Gempalm Polluter
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 2);
        harness.assertLife(player1, lifeBefore - 2);
    }

    @Test
    @DisplayName("Uses the Zombie count when the ability resolves")
    void usesZombieCountAtResolution() {
        var muse = harness.addToBattlefieldAndReturn(player1, new GravebornMuse());
        harness.setHand(player1, List.of());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        gd.playerBattlefields.get(player1.getId()).remove(muse);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore);
        harness.assertLife(player1, lifeBefore);
    }

    @Test
    @DisplayName("Non-Zombie creatures do not increase the count")
    void nonZombiesDoNotCount() {
        harness.addToBattlefield(player1, new GravebornMuse());
        harness.addToBattlefield(player1, new EnormousBaloth());
        harness.setHand(player1, List.of());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        // Only Graveborn Muse is a Zombie, Enormous Baloth is a Beast
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
        harness.assertLife(player1, lifeBefore - 1);
    }

    @Test
    @DisplayName("Does not trigger during opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new GravebornMuse());
        harness.setHand(player1, List.of());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player2); // opponent's upkeep
        harness.passBothPriorities();

        // No trigger — player1's hand and life unchanged
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore);
        harness.assertLife(player1, lifeBefore);
    }

    @Test
    @DisplayName("Only counts Zombies controller controls, not opponent's")
    void onlyCountsOwnZombies() {
        harness.addToBattlefield(player1, new GravebornMuse());
        harness.addToBattlefield(player2, new GempalmPolluter()); // opponent's Zombie
        harness.setHand(player1, List.of());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        // Only 1 Zombie (Graveborn Muse) — opponent's Gempalm Polluter doesn't count
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
        harness.assertLife(player1, lifeBefore - 1);
    }
}

