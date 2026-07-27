package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CrushingVinesTest extends BaseCardTest {

    

    @Nested
    @DisplayName("Mode 1: Destroy target creature with flying")
    class DestroyFlyingCreatureMode {

        @Test
        @DisplayName("Destroys target creature with flying")
        void destroysFlyingCreature() {
            AirElemental airElemental = new AirElemental();
            harness.addToBattlefield(player2, airElemental);

            harness.setHand(player1, List.of(new CrushingVines()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            Permanent airElementalPermanent = findPermanent(player2, "Air Elemental");

            harness.castInstant(player1, 0, 0, airElementalPermanent.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Air Elemental");
            harness.assertInGraveyard(player2, "Air Elemental");
        }

        @Test
        @DisplayName("Cannot target a creature without flying")
        void cannotTargetCreatureWithoutFlying() {
            GrizzlyBears bears = new GrizzlyBears();
            harness.addToBattlefield(player2, bears);
            harness.addToBattlefield(player1, new AirElemental());

            harness.setHand(player1, List.of(new CrushingVines()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            Permanent bearsPermanent = findPermanent(player2, "Grizzly Bears");

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, bearsPermanent.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Cannot target an artifact with flying creature mode")
        void cannotTargetArtifactWithFlyingMode() {
            Millstone millstone = new Millstone();
            harness.addToBattlefield(player2, millstone);
            harness.addToBattlefield(player1, new AirElemental());

            harness.setHand(player1, List.of(new CrushingVines()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            Permanent millstonePermanent = findPermanent(player2, "Millstone");

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, millstonePermanent.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 2: Destroy target artifact")
    class DestroyArtifactMode {

        @Test
        @DisplayName("Destroys target artifact")
        void destroysArtifact() {
            Millstone millstone = new Millstone();
            harness.addToBattlefield(player2, millstone);

            harness.setHand(player1, List.of(new CrushingVines()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            Permanent millstonePermanent = findPermanent(player2, "Millstone");

            harness.castInstant(player1, 0, 1, millstonePermanent.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Millstone");
            harness.assertInGraveyard(player2, "Millstone");
        }

        @Test
        @DisplayName("Cannot target a creature without flying using artifact mode")
        void cannotTargetCreatureWithArtifactMode() {
            GrizzlyBears bears = new GrizzlyBears();
            harness.addToBattlefield(player2, bears);
            harness.addToBattlefield(player1, new Millstone());

            harness.setHand(player1, List.of(new CrushingVines()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            Permanent bearsPermanent = findPermanent(player2, "Grizzly Bears");

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, bearsPermanent.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Test
    @DisplayName("Choosing invalid mode is rejected at cast time")
    void invalidModeIsRejected() {
        AirElemental airElemental = new AirElemental();
        harness.addToBattlefield(player2, airElemental);

        harness.setHand(player1, List.of(new CrushingVines()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        Permanent airElementalPermanent = findPermanent(player2, "Air Elemental");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 99, airElementalPermanent.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid mode index");
    }

    @Test
    @DisplayName("Crushing Vines goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        AirElemental airElemental = new AirElemental();
        harness.addToBattlefield(player2, airElemental);

        harness.setHand(player1, List.of(new CrushingVines()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        Permanent airElementalPermanent = findPermanent(player2, "Air Elemental");

        harness.castInstant(player1, 0, 0, airElementalPermanent.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Crushing Vines");
    }
}
