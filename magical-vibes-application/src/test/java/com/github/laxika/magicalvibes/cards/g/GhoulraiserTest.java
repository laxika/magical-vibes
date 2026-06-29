package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CemeteryReaper;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GhoulraiserTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has ETB effect that returns a random Zombie from graveyard to hand")
    void hasCorrectEffect() {
        Ghoulraiser card = new Ghoulraiser();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(ReturnCardFromGraveyardEffect.class);

        ReturnCardFromGraveyardEffect effect =
                (ReturnCardFromGraveyardEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.returnAtRandom()).isTrue();
        assertThat(effect.filter()).isInstanceOf(CardSubtypePredicate.class);
        assertThat(((CardSubtypePredicate) effect.filter()).subtype()).isEqualTo(CardSubtype.ZOMBIE);
    }

    // ===== ETB trigger: return random Zombie =====

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

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Cemetery Reaper"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Cemetery Reaper"));
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
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Cemetery Reaper"));
        // Grizzly Bears (not a Zombie) should stay in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
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
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        // Hand should not contain Grizzly Bears
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
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
}
