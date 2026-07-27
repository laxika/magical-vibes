package com.github.laxika.magicalvibes.cards.f;

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

class FieryInterventionTest extends BaseCardTest {

    

    @Nested
    @DisplayName("Mode 1: Deal 5 damage to target creature")
    class DamageMode {

        @Test
        @DisplayName("Deals 5 damage to target creature")
        void deals5DamageToCreature() {
            GrizzlyBears bears = new GrizzlyBears(); // 2/2
            harness.addToBattlefield(player2, bears);

            harness.setHand(player1, List.of(new FieryIntervention()));
            harness.addMana(player1, ManaColor.RED, 5);

            Permanent bearsPermanent = findPermanent(player2, "Grizzly Bears");

            harness.castSorcery(player1, 0, 0, bearsPermanent.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Grizzly Bears");
            harness.assertInGraveyard(player2, "Grizzly Bears");
        }

        @Test
        @DisplayName("Cannot target an artifact with damage mode")
        void cannotTargetArtifact() {
            Millstone millstone = new Millstone();
            harness.addToBattlefield(player2, millstone);

            harness.setHand(player1, List.of(new FieryIntervention()));
            harness.addMana(player1, ManaColor.RED, 5);

            Permanent millstonePermanent = findPermanent(player2, "Millstone");

            assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, millstonePermanent.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 2: Destroy target artifact")
    class DestroyMode {

        @Test
        @DisplayName("Destroys target artifact")
        void destroysArtifact() {
            Millstone millstone = new Millstone();
            harness.addToBattlefield(player2, millstone);

            harness.setHand(player1, List.of(new FieryIntervention()));
            harness.addMana(player1, ManaColor.RED, 5);

            Permanent millstonePermanent = findPermanent(player2, "Millstone");

            harness.castSorcery(player1, 0, 1, millstonePermanent.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Millstone");
            harness.assertInGraveyard(player2, "Millstone");
        }

        @Test
        @DisplayName("Cannot target a non-artifact creature with destroy mode")
        void cannotTargetNonArtifactCreature() {
            GrizzlyBears bears = new GrizzlyBears();
            harness.addToBattlefield(player2, bears);

            harness.setHand(player1, List.of(new FieryIntervention()));
            harness.addMana(player1, ManaColor.RED, 5);

            Permanent bearsPermanent = findPermanent(player2, "Grizzly Bears");

            assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, bearsPermanent.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Test
    @DisplayName("Choosing invalid mode is rejected at cast time")
    void invalidModeIsRejected() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player2, bears);

        harness.setHand(player1, List.of(new FieryIntervention()));
        harness.addMana(player1, ManaColor.RED, 5);

        Permanent bearsPermanent = findPermanent(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 99, bearsPermanent.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid mode index");
    }

    @Test
    @DisplayName("Fiery Intervention goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player2, bears);

        harness.setHand(player1, List.of(new FieryIntervention()));
        harness.addMana(player1, ManaColor.RED, 5);

        Permanent bearsPermanent = findPermanent(player2, "Grizzly Bears");

        harness.castSorcery(player1, 0, 0, bearsPermanent.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Fiery Intervention");
    }
}
