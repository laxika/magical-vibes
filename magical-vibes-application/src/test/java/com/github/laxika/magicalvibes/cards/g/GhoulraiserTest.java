package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CemeteryReaper;
import com.github.laxika.magicalvibes.cards.d.DiregrafGhoul;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Ghoulraiser.class, CemeteryReaper.class, GrizzlyBears.class, DiregrafGhoul.class})
class GhoulraiserTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns a Zombie from graveyard to hand")
    void etbReturnsZombieFromGraveyard() {
        harness.setGraveyard(player1, List.of(new CemeteryReaper()));
        harness.setHand(player1, List.of(new Ghoulraiser()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        // Resolve Ghoulraiser entering the battlefield
        harness.passBothPriorities();

        // ETB trigger is on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        // Resolve the ETB trigger
        harness.passBothPriorities();

        harness.assertInHand(player1, "Cemetery Reaper");
        harness.assertNotInGraveyard(player1, "Cemetery Reaper");
    }

    @Test
    @DisplayName("ETB with multiple Zombies returns exactly one at random")
    void etbReturnsOneRandomZombieFromMultiple() {
        harness.setGraveyard(player1, List.of(new CemeteryReaper(), new Ghoulraiser()));
        harness.setHand(player1, List.of(new Ghoulraiser()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB trigger

        // One of the two Zombies should be returned to hand
        long handZombies = gd.playerHands.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Cemetery Reaper") || c.getName().equals("Ghoulraiser"))
                .count();
        assertThat(handZombies).isEqualTo(1);

        // One should remain in graveyard
        long graveyardZombies = gd.playerGraveyards.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Cemetery Reaper") || c.getName().equals("Ghoulraiser"))
                .count();
        assertThat(graveyardZombies).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB ignores non-Zombie creatures in graveyard")
    void etbIgnoresNonZombies() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears, new CemeteryReaper()));
        harness.setHand(player1, List.of(new Ghoulraiser()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB trigger

        // Cemetery Reaper (Zombie) should be returned
        harness.assertInHand(player1, "Cemetery Reaper");
        // Grizzly Bears (not a Zombie) should stay in graveyard
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB does nothing when no Zombies in graveyard")
    void etbDoesNothingWithNoZombies() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new Ghoulraiser()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB trigger

        // Grizzly Bears should stay in graveyard
        harness.assertInGraveyard(player1, "Grizzly Bears");
        // Hand should not contain Grizzly Bears
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB does nothing when graveyard is empty")
    void etbDoesNothingWithEmptyGraveyard() {
        harness.setHand(player1, List.of(new Ghoulraiser()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB trigger

        // Should resolve without error — no card returned to hand
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("ETB does not return Zombies from an opponent's graveyard")
    void etbIgnoresOpponentsGraveyard() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new DiregrafGhoul()));
        harness.setHand(player1, List.of(new Ghoulraiser()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Diregraf Ghoul");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB can return a Zombie that entered the graveyard after triggering")
    void etbUsesGraveyardAtResolution() {
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new Ghoulraiser()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.setGraveyard(player1, List.of(new DiregrafGhoul()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Diregraf Ghoul");
        harness.assertNotInGraveyard(player1, "Diregraf Ghoul");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB resolves without returning a Zombie removed before resolution")
    void etbDoesNothingWhenZombieLeavesBeforeResolution() {
        harness.setGraveyard(player1, List.of(new DiregrafGhoul()));
        harness.setHand(player1, List.of(new Ghoulraiser()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Ghoulraiser");
    }
}
