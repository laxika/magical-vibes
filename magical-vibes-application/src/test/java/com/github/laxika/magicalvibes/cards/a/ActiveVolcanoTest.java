package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BenthicExplorers;
import com.github.laxika.magicalvibes.cards.c.CrimsonKobolds;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ActiveVolcano.class, BenthicExplorers.class, CrimsonKobolds.class, Island.class})
class ActiveVolcanoTest extends BaseCardTest {

    @Nested
    @DisplayName("Mode 0: Destroy target blue permanent")
    class DestroyBluePermanentMode {

        @Test
        @DisplayName("Destroys a blue permanent")
        void destroysBluePermanent() {
            Permanent explorers = harness.addToBattlefieldAndReturn(player2, new BenthicExplorers());
            harness.setHand(player1, List.of(new ActiveVolcano()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castInstant(player1, 0, 0, explorers.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Benthic Explorers");
            harness.assertInGraveyard(player2, "Benthic Explorers");
        }

        @Test
        @DisplayName("Cannot target a nonblue permanent")
        void cannotTargetNonbluePermanent() {
            Permanent kobolds = harness.addToBattlefieldAndReturn(player2, new CrimsonKobolds());
            harness.setHand(player1, List.of(new ActiveVolcano()));
            harness.addMana(player1, ManaColor.RED, 1);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, kobolds.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 1: Return target Island to its owner's hand")
    class ReturnIslandMode {

        @Test
        @DisplayName("Returns an Island to its owner's hand")
        void returnsIsland() {
            Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
            harness.setHand(player1, List.of(new ActiveVolcano()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castInstant(player1, 0, 1, island.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Island");
            harness.assertInHand(player2, "Island");
        }

        @Test
        @DisplayName("Cannot target a non-Island permanent")
        void cannotTargetNonIslandPermanent() {
            Permanent kobolds = harness.addToBattlefieldAndReturn(player2, new CrimsonKobolds());
            harness.setHand(player1, List.of(new ActiveVolcano()));
            harness.addMana(player1, ManaColor.RED, 1);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, kobolds.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
