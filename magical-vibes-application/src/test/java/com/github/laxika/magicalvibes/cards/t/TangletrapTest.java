package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TangletrapTest extends BaseCardTest {

    @Nested
    @DisplayName("Mode 0: Deal 5 damage to target creature with flying")
    class DamageMode {

        @Test
        @DisplayName("Deals 5 damage to a flying creature")
        void dealsDamageToFlyingCreature() {
            harness.addToBattlefield(player2, new AirElemental());
            harness.setHand(player1, List.of(new Tangletrap()));
            harness.addMana(player1, ManaColor.GREEN, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 1);

            harness.castInstant(player1, 0, 0,
                    harness.getPermanentId(player2, "Air Elemental"));
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Air Elemental");
            harness.assertInGraveyard(player2, "Air Elemental");
        }

        @Test
        @DisplayName("Cannot target a creature without flying")
        void cannotTargetNonFlyingCreature() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new Tangletrap()));
            harness.addMana(player1, ManaColor.GREEN, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 1);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0,
                    harness.getPermanentId(player2, "Grizzly Bears")))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 1: Destroy target artifact")
    class DestroyMode {

        @Test
        @DisplayName("Destroys target artifact")
        void destroysArtifact() {
            harness.addToBattlefield(player2, new Millstone());
            harness.setHand(player1, List.of(new Tangletrap()));
            harness.addMana(player1, ManaColor.GREEN, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 1);

            harness.castInstant(player1, 0, 1,
                    harness.getPermanentId(player2, "Millstone"));
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Millstone");
            harness.assertInGraveyard(player2, "Millstone");
        }

        @Test
        @DisplayName("Cannot target a non-artifact creature")
        void cannotTargetNonArtifact() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new Tangletrap()));
            harness.addMana(player1, ManaColor.GREEN, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 1);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1,
                    harness.getPermanentId(player2, "Grizzly Bears")))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
