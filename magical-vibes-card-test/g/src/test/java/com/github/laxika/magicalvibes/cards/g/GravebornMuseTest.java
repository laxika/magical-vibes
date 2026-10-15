package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AmoeboidChangeling;
import com.github.laxika.magicalvibes.cards.e.EnormousBaloth;
import com.github.laxika.magicalvibes.cards.v.VilisBrokerOfBlood;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GravebornMuse.class, EnormousBaloth.class, WoodlandChangeling.class, AmoeboidChangeling.class, VilisBrokerOfBlood.class})
class GravebornMuseTest extends BaseCardTest {

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
        harness.addToBattlefield(player1, new GravebornMuse());
        harness.setHand(player1, List.of());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        // 2 Zombies: two Graveborn Muse permanents
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
        harness.addToBattlefield(player2, new GravebornMuse()); // opponent's Zombie
        harness.setHand(player1, List.of());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        // Only 1 Zombie (Graveborn Muse) — opponent's Graveborn Muse doesn't count
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
        harness.assertLife(player1, lifeBefore - 1);
    }

    @Test
    @DisplayName("Counts Changeling creatures as Zombies")
    void changelingsCountAsZombies() {
        harness.addToBattlefield(player1, new GravebornMuse());
        harness.addToBattlefield(player1, new WoodlandChangeling());
        harness.setHand(player1, List.of());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 2);
        harness.assertLife(player1, lifeBefore - 2);
    }

    @Test
    @DisplayName("Each Muse independently draws and loses life for all controlled Zombies")
    void multipleMusesEachResolveTheirTrigger() {
        harness.addToBattlefield(player1, new GravebornMuse());
        harness.addToBattlefield(player1, new GravebornMuse());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(
                new EnormousBaloth(), new EnormousBaloth(),
                new EnormousBaloth(), new EnormousBaloth()));
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        harness.assertLife(player1, 16);
    }

    @Test
    @DisplayName("Counts a creature that gains Zombie before the upkeep trigger resolves")
    void countsZombieTypeGainedInResponse() {
        harness.addToBattlefield(player1, new GravebornMuse());
        var baloth = harness.addToBattlefieldAndReturn(player1, new EnormousBaloth());
        addCreatureReady(player2, new AmoeboidChangeling());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new EnormousBaloth(), new EnormousBaloth()));
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.activateAbility(player2, 0, 0, null, baloth.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Does not count a Muse that loses Zombie before its trigger resolves")
    void excludesZombieTypeLostInResponse() {
        var muse = harness.addToBattlefieldAndReturn(player1, new GravebornMuse());
        addCreatureReady(player2, new AmoeboidChangeling());
        harness.setHand(player1, List.of());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.activateAbility(player2, 0, 1, null, muse.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Life lost to the Muse triggers Vilis to draw that many additional cards")
    void lifeLossTriggersVilis() {
        harness.addToBattlefield(player1, new GravebornMuse());
        harness.addToBattlefield(player1, new VilisBrokerOfBlood());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new EnormousBaloth(), new EnormousBaloth()));
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertLife(player1, 19);
    }
}
