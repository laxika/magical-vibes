package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DoomWeaver.class, GrizzlyBears.class})
class DoomWeaverTest extends BaseCardTest {

    @Test
    @DisplayName("Paired creature draws cards equal to its power when it dies")
    void pairedCreatureDrawsEqualToItsPowerOnDeath() {
        Permanent bears = pairWithBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        kill(bears, 2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Doom Weaver draws cards equal to its power when it dies while paired")
    void doomWeaverDrawsEqualToItsPowerOnDeath() {
        pairWithBears();
        Permanent doomWeaver = findPermanent(player1, "Doom Weaver");
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        kill(doomWeaver, 8);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An unpaired creature does not gain Doom Weaver's death-draw ability")
    void unpairedCreatureDoesNotDrawOnDeath() {
        Permanent doomWeaver = addCreatureReady(player1, new DoomWeaver());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        kill(doomWeaver, 8);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent pairWithBears() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DoomWeaver()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        return bears;
    }

    private void kill(Permanent permanent, int damage) {
        permanent.setMarkedDamage(damage);
        harness.runStateBasedActions();
    }
}
