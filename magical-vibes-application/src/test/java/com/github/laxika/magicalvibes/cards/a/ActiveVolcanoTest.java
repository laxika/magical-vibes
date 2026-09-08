package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ActiveVolcanoTest extends BaseCardTest {

    @Nested
    @DisplayName("Mode 0: Destroy target blue permanent")
    class DestroyMode {

        @Test
        @DisplayName("Destroys a blue permanent")
        void destroysBluePermanent() {
            harness.addToBattlefield(player2, new AirElemental());
            harness.setHand(player1, List.of(new ActiveVolcano()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castInstant(player1, 0, 0, harness.getPermanentId(player2, "Air Elemental"));
            harness.passBothPriorities();

            harness.assertInGraveyard(player2, "Air Elemental");
        }

        @Test
        @DisplayName("Cannot target a nonblue permanent")
        void cannotTargetNonbluePermanent() {
            harness.addToBattlefield(player2, new AirElemental());
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new ActiveVolcano()));
            harness.addMana(player1, ManaColor.RED, 1);

            assertThatThrownBy(() -> harness.castInstant(
                    player1, 0, 0, harness.getPermanentId(player2, "Grizzly Bears")))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 1: Return target Island to its owner's hand")
    class ReturnIslandMode {

        @Test
        @DisplayName("Returns an Island to its owner's hand")
        void returnsIsland() {
            harness.addToBattlefield(player2, new Island());
            harness.setHand(player1, List.of(new ActiveVolcano()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castInstant(player1, 0, 1, harness.getPermanentId(player2, "Island"));
            harness.passBothPriorities();

            harness.assertInHand(player2, "Island");
        }

        @Test
        @DisplayName("Cannot target a non-Island land")
        void cannotTargetNonIslandLand() {
            harness.addToBattlefield(player2, new Island());
            harness.addToBattlefield(player2, new Mountain());
            harness.setHand(player1, List.of(new ActiveVolcano()));
            harness.addMana(player1, ManaColor.RED, 1);

            assertThatThrownBy(() -> harness.castInstant(
                    player1, 0, 1, harness.getPermanentId(player2, "Mountain")))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
