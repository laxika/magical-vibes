package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DualShotTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to a single target creature")
    void singleTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DualShot()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, List.of(bearId));
        harness.passBothPriorities();

        Permanent bear = harness.getGameData().playerBattlefields.get(player2.getId()).getFirst();
        assertThat(bear.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals 1 damage to each of two target creatures")
    void twoTargets() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DualShot()));
        harness.addMana(player1, ManaColor.RED, 1);

        List<Permanent> bf = harness.getGameData().playerBattlefields.get(player2.getId());
        UUID id1 = bf.get(0).getId();
        UUID id2 = bf.get(1).getId();

        harness.castInstant(player1, 0, List.of(id1, id2));
        harness.passBothPriorities();

        bf = harness.getGameData().playerBattlefields.get(player2.getId());
        assertThat(bf.get(0).getMarkedDamage()).isEqualTo(1);
        assertThat(bf.get(1).getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Kills 1/1 creatures")
    void killsOneOneCreatures() {
        // Use a 1/1 token-like creature — Grizzly Bears is 2/2, so let's use a different approach
        // We'll deal 1 damage to a creature that already has 1 damage on it (total = toughness)
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DualShot()));
        harness.addMana(player1, ManaColor.RED, 1);

        // Pre-damage the bear so 1 more will be lethal (toughness 2)
        Permanent bear = harness.getGameData().playerBattlefields.get(player2.getId()).getFirst();
        bear.setMarkedDamage(1);
        UUID bearId = bear.getId();

        harness.castInstant(player1, 0, List.of(bearId));
        harness.passBothPriorities();

        // Bear should be dead
        assertThat(harness.getGameData().playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(harness.getGameData().playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Partially resolves when one of two targets is removed")
    void partiallyResolvesWhenOneTargetRemoved() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DualShot()));
        harness.addMana(player1, ManaColor.RED, 1);

        List<Permanent> bf = harness.getGameData().playerBattlefields.get(player2.getId());
        UUID id1 = bf.get(0).getId();
        UUID id2 = bf.get(1).getId();

        harness.castInstant(player1, 0, List.of(id1, id2));

        // Remove the first target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).removeFirst();

        harness.passBothPriorities();

        // Second creature should still take damage
        bf = harness.getGameData().playerBattlefields.get(player2.getId());
        assertThat(bf).hasSize(1);
        assertThat(bf.getFirst().getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Spell fizzles when all targets are removed before resolution")
    void fizzlesWhenAllTargetsRemoved() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DualShot()));
        harness.addMana(player1, ManaColor.RED, 1);

        List<Permanent> bf = harness.getGameData().playerBattlefields.get(player2.getId());
        UUID id1 = bf.get(0).getId();
        UUID id2 = bf.get(1).getId();

        harness.castInstant(player1, 0, List.of(id1, id2));

        // Remove both targets before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        // Spell should fizzle
        assertThat(harness.getGameData().stack).isEmpty();
        assertThat(harness.getGameData().gameLog).anyMatch(log -> log.contains("fizzles"));
        // Card should go to graveyard even when fizzled
        assertThat(harness.getGameData().playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Dual Shot"));
    }
}
