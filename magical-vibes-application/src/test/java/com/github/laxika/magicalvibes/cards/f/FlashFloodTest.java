package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FlashFloodTest extends BaseCardTest {

    @Nested
    @DisplayName("Mode 0: Destroy target red permanent")
    class DestroyRedPermanentMode {

        @Test
        @DisplayName("Destroys a red permanent")
        void destroysRedPermanent() {
            harness.addToBattlefield(player2, new RagingGoblin());
            harness.setHand(player1, List.of(new FlashFlood()));
            harness.addMana(player1, ManaColor.BLUE, 1);

            Permanent goblin = findPermanent(player2, "Raging Goblin");
            harness.castInstant(player1, 0, 0, goblin.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Raging Goblin");
            harness.assertInGraveyard(player2, "Raging Goblin");
        }

        @Test
        @DisplayName("Cannot target a nonred permanent")
        void cannotTargetNonredPermanent() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new FlashFlood()));
            harness.addMana(player1, ManaColor.BLUE, 1);

            Permanent bears = findPermanent(player2, "Grizzly Bears");
            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, bears.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 1: Return target Mountain to its owner's hand")
    class ReturnMountainMode {

        @Test
        @DisplayName("Returns a Mountain to its owner's hand")
        void returnsMountain() {
            harness.addToBattlefield(player2, new Mountain());
            harness.setHand(player1, List.of(new FlashFlood()));
            harness.addMana(player1, ManaColor.BLUE, 1);

            Permanent mountain = findPermanent(player2, "Mountain");
            harness.castInstant(player1, 0, 1, mountain.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Mountain");
            harness.assertInHand(player2, "Mountain");
        }

        @Test
        @DisplayName("Cannot target a non-Mountain permanent")
        void cannotTargetNonMountainPermanent() {
            harness.addToBattlefield(player2, new RagingGoblin());
            harness.setHand(player1, List.of(new FlashFlood()));
            harness.addMana(player1, ManaColor.BLUE, 1);

            Permanent goblin = findPermanent(player2, "Raging Goblin");
            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, goblin.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
