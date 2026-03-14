package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HonorOfThePure;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PlanarCleansingTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all creatures on both sides")
    void destroysAllCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new PlanarCleansing()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Destroys enchantments")
    void destroysEnchantments() {
        harness.addToBattlefield(player1, new HonorOfThePure());

        harness.setHand(player1, List.of(new PlanarCleansing()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Honor of the Pure"));

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Honor of the Pure"));
    }

    @Test
    @DisplayName("Destroys artifacts")
    void destroysArtifacts() {
        harness.addToBattlefield(player2, new PalladiumMyr());

        harness.setHand(player1, List.of(new PlanarCleansing()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Palladium Myr"));

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Palladium Myr"));
    }

    @Test
    @DisplayName("Does not destroy lands")
    void doesNotDestroyLands() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new PlanarCleansing()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Land should survive
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Plains"));

        // Creature should be destroyed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Planar Cleansing goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new PlanarCleansing()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Planar Cleansing"));
    }
}
