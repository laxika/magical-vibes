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
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ActiveVolcano.class, Island.class, BenthicExplorers.class, CrimsonKobolds.class})
class ActiveVolcanoTest extends BaseCardTest {

    @Nested
    @DisplayName("Mode 0: Destroy target blue permanent")
    @CardUsed({ActiveVolcano.class, BenthicExplorers.class, CrimsonKobolds.class, Island.class})
    class DestroyBluePermanentMode {

        @Test
        void canDestroyOwnBluePermanent() {
            Permanent explorers = harness.addToBattlefieldAndReturn(player1, new BenthicExplorers());
            harness.setHand(player1, List.of(new ActiveVolcano()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castAndResolveInstant(player1, 0, explorers.getId());

            harness.assertNotOnBattlefield(player1, "Benthic Explorers");
            harness.assertInGraveyard(player1, "Benthic Explorers");
        }

        @Test
        void cannotDestroyColorlessIsland() {
            Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
            harness.setHand(player1, List.of(new ActiveVolcano()));
            harness.addMana(player1, ManaColor.RED, 1);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, island.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void doesNotResolveWhenTargetHasLeftBattlefield() {
            Permanent explorers = harness.addToBattlefieldAndReturn(player2, new BenthicExplorers());
            harness.setHand(player1, List.of(new ActiveVolcano(), new ActiveVolcano()));
            harness.addMana(player1, ManaColor.RED, 2);

            harness.castInstant(player1, 0, 0, explorers.getId());
            harness.castInstant(player1, 0, 0, explorers.getId());
            harness.passBothPriorities();
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Benthic Explorers");
            harness.assertInGraveyard(player2, "Benthic Explorers");
            assertThat(gd.stack).isEmpty();
            assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        }

        @Test
        @DisplayName("Destroys a blue permanent")
        void destroysBluePermanent() {
            Permanent explorers = harness.addToBattlefieldAndReturn(player2, new BenthicExplorers());
            harness.setHand(player1, List.of(new ActiveVolcano()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castAndResolveInstant(player1, 0, explorers.getId());

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
    @CardUsed({ActiveVolcano.class, Island.class, CrimsonKobolds.class})
    class ReturnIslandMode {

        @Test
        void returnsIslandToOwnerRatherThanController() {
            harness.setHand(player2, List.of());
            Island card = new Island();
            card.setOwnerId(player1.getId());
            Permanent island = harness.addToBattlefieldAndReturn(player2, card);
            harness.setHand(player1, List.of(new ActiveVolcano()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castInstant(player1, 0, 1, island.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Island");
            harness.assertInHand(player1, "Island");
            assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        }

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
