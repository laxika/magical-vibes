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

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Destroys enchantments")
    void destroysEnchantments() {
        harness.addToBattlefield(player1, new HonorOfThePure());

        harness.setHand(player1, List.of(new PlanarCleansing()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Honor of the Pure");

        harness.assertInGraveyard(player1, "Honor of the Pure");
    }

    @Test
    @DisplayName("Destroys artifacts")
    void destroysArtifacts() {
        harness.addToBattlefield(player2, new PalladiumMyr());

        harness.setHand(player1, List.of(new PlanarCleansing()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Palladium Myr");

        harness.assertInGraveyard(player2, "Palladium Myr");
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
        harness.assertOnBattlefield(player1, "Plains");

        // Creature should be destroyed
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Planar Cleansing goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new PlanarCleansing()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Planar Cleansing");
    }
}
