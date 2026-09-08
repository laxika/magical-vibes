package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ActiveVolcanoTest extends BaseCardTest {

    @Nested
    @DisplayName("Mode 0: Destroy target blue permanent")
    class DestroyBluePermanentMode {

        @Test
        @DisplayName("Destroys a blue permanent")
        void destroysBluePermanent() {
            harness.addToBattlefield(player2, new AzureDrake());
            harness.setHand(player1, List.of(new ActiveVolcano()));
            harness.addMana(player1, ManaColor.RED, 1);

            Permanent drake = findPermanent(player2, "Azure Drake");
            harness.castInstant(player1, 0, 0, drake.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Azure Drake");
            harness.assertInGraveyard(player2, "Azure Drake");
        }

        @Test
        @DisplayName("Cannot target a nonblue permanent")
        void cannotTargetNonbluePermanent() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new ActiveVolcano()));
            harness.addMana(player1, ManaColor.RED, 1);

            Permanent bears = findPermanent(player2, "Grizzly Bears");
            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, bears.getId()))
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

            Permanent island = findPermanent(player2, "Island");
            harness.castInstant(player1, 0, 1, island.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Island");
            harness.assertInHand(player2, "Island");
        }

        @Test
        @DisplayName("Cannot target a non-Island permanent")
        void cannotTargetNonIslandPermanent() {
            harness.addToBattlefield(player2, new LlanowarElves());
            harness.setHand(player1, List.of(new ActiveVolcano()));
            harness.addMana(player1, ManaColor.RED, 1);

            Permanent elves = findPermanent(player2, "Llanowar Elves");
            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, elves.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
