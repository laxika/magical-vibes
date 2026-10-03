package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.n.NephaliaSeakite;
import com.github.laxika.magicalvibes.cards.e.ErdwalRipper;
import com.github.laxika.magicalvibes.cards.w.WolfhuntersQuiver;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrushingVines.class, NephaliaSeakite.class, ErdwalRipper.class, WolfhuntersQuiver.class})
class CrushingVinesTest extends BaseCardTest {

    @Nested
    @DisplayName("Mode 1: Destroy target creature with flying")
    @CardUsed({CrushingVines.class, NephaliaSeakite.class, ErdwalRipper.class, WolfhuntersQuiver.class})
    class DestroyFlyingCreatureMode {

        @Test
        @DisplayName("Destroys target creature with flying")
        void destroysFlyingCreature() {
            Permanent seakitePermanent = harness.addToBattlefieldAndReturn(player2, new NephaliaSeakite());

            harness.setHand(player1, List.of(new CrushingVines()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            harness.castInstant(player1, 0, 0, seakitePermanent.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Nephalia Seakite");
            harness.assertInGraveyard(player2, "Nephalia Seakite");
        }

        @Test
        @DisplayName("Cannot target a creature without flying")
        void cannotTargetCreatureWithoutFlying() {
            Permanent ripperPermanent = harness.addToBattlefieldAndReturn(player2, new ErdwalRipper());
            harness.addToBattlefield(player1, new NephaliaSeakite());

            harness.setHand(player1, List.of(new CrushingVines()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, ripperPermanent.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Cannot target an artifact with flying creature mode")
        void cannotTargetArtifactWithFlyingMode() {
            Permanent quiverPermanent = harness.addToBattlefieldAndReturn(player2, new WolfhuntersQuiver());
            harness.addToBattlefield(player1, new NephaliaSeakite());

            harness.setHand(player1, List.of(new CrushingVines()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, quiverPermanent.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 2: Destroy target artifact")
    @CardUsed({CrushingVines.class, ErdwalRipper.class, WolfhuntersQuiver.class})
    class DestroyArtifactMode {

        @Test
        @DisplayName("Destroys target artifact")
        void destroysArtifact() {
            Permanent quiverPermanent = harness.addToBattlefieldAndReturn(player2, new WolfhuntersQuiver());

            harness.setHand(player1, List.of(new CrushingVines()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            harness.castInstant(player1, 0, 1, quiverPermanent.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Wolfhunter's Quiver");
            harness.assertInGraveyard(player2, "Wolfhunter's Quiver");
        }

        @Test
        @DisplayName("Cannot target a creature without flying using artifact mode")
        void cannotTargetCreatureWithArtifactMode() {
            Permanent ripperPermanent = harness.addToBattlefieldAndReturn(player2, new ErdwalRipper());
            harness.addToBattlefield(player1, new WolfhuntersQuiver());

            harness.setHand(player1, List.of(new CrushingVines()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, ripperPermanent.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Test
    @DisplayName("Choosing invalid mode is rejected at cast time")
    void invalidModeIsRejected() {
        Permanent seakitePermanent = harness.addToBattlefieldAndReturn(player2, new NephaliaSeakite());

        harness.setHand(player1, List.of(new CrushingVines()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 99, seakitePermanent.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid mode index");
    }

    @Test
    @DisplayName("Crushing Vines goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        Permanent seakitePermanent = harness.addToBattlefieldAndReturn(player2, new NephaliaSeakite());

        harness.setHand(player1, List.of(new CrushingVines()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, 0, seakitePermanent.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Crushing Vines");
    }

    @Test
    void flyingTargetSurvivesIfItLosesFlyingBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NephaliaSeakite());
        harness.setHand(player1, List.of(new CrushingVines()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, 0, target.getId());
        target.getRemovedKeywords().add(Keyword.FLYING);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Nephalia Seakite");
        harness.assertInGraveyard(player1, "Crushing Vines");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canDestroyOwnFlyingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new NephaliaSeakite());
        harness.setHand(player1, List.of(new CrushingVines()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Nephalia Seakite");
        harness.assertInGraveyard(player1, "Nephalia Seakite");
    }

    @Test
    void artifactModeCannotTargetNonartifactEvenWithFlying() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NephaliaSeakite());
        harness.addToBattlefield(player1, new WolfhuntersQuiver());
        harness.setHand(player1, List.of(new CrushingVines()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
