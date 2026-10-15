package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BloodMoon;
import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Manabarbs;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.u.UrzasTower;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlashFlood.class, RagingGoblin.class, GrizzlyBears.class, Mountain.class, Manabarbs.class,
        BloodMoon.class, Boomerang.class, UrzasTower.class})
class FlashFloodTest extends BaseCardTest {

    @Nested
    @CardUsed({FlashFlood.class, RagingGoblin.class, GrizzlyBears.class, Mountain.class, Manabarbs.class, Boomerang.class})
    @DisplayName("Mode 0: Destroy target red permanent")
    class DestroyRedPermanentMode {

        @Test
        @DisplayName("Destroys a red permanent")
        void destroysRedPermanent() {
            Permanent goblin = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
            harness.setHand(player1, List.of(new FlashFlood()));
            harness.addMana(player1, ManaColor.BLUE, 1);

            harness.castInstant(player1, 0, 0, goblin.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Raging Goblin");
            harness.assertInGraveyard(player2, "Raging Goblin");
        }

        @Test
        @DisplayName("Destroys a red noncreature permanent")
        void destroysRedNoncreaturePermanent() {
            Permanent manabarbs = harness.addToBattlefieldAndReturn(player2, new Manabarbs());
            harness.setHand(player1, List.of(new FlashFlood()));
            harness.addMana(player1, ManaColor.BLUE, 1);

            harness.castInstant(player1, 0, 0, manabarbs.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Manabarbs");
            harness.assertInGraveyard(player2, "Manabarbs");
        }

        @Test
        @DisplayName("Cannot target a nonred permanent")
        void cannotTargetNonredPermanent() {
            Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new FlashFlood()));
            harness.addMana(player1, ManaColor.BLUE, 1);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, bears.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void cannotDestroyColorlessMountain() {
            Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
            harness.setHand(player1, List.of(new FlashFlood()));
            harness.addMana(player1, ManaColor.BLUE, 1);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, mountain.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void canDestroyOwnRedPermanent() {
            Permanent goblin = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
            harness.setHand(player1, List.of(new FlashFlood()));
            harness.addMana(player1, ManaColor.BLUE, 1);

            harness.castInstant(player1, 0, 0, goblin.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player1, "Raging Goblin");
            harness.assertInGraveyard(player1, "Raging Goblin");
        }

        @Test
        void doesNotDestroyTargetThatLeftBattlefield() {
            Permanent goblin = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
            harness.setHand(player1, List.of(new FlashFlood()));
            harness.setHand(player2, List.of(new Boomerang()));
            harness.addMana(player1, ManaColor.BLUE, 1);
            harness.addMana(player2, ManaColor.BLUE, 2);

            harness.castInstant(player1, 0, 0, goblin.getId());
            harness.castAndResolveInstant(player2, 0, goblin.getId());
            harness.passBothPriorities();

            harness.assertInHand(player2, "Raging Goblin");
            harness.assertNotInGraveyard(player2, "Raging Goblin");
            harness.assertInGraveyard(player1, "Flash Flood");
        }
    }

    @Nested
    @CardUsed({FlashFlood.class, Mountain.class, RagingGoblin.class, BloodMoon.class, Boomerang.class, UrzasTower.class})
    @DisplayName("Mode 1: Return target Mountain to its owner's hand")
    class ReturnMountainMode {

        @Test
        @DisplayName("Returns a Mountain to its owner's hand")
        void returnsMountain() {
            Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
            harness.setHand(player1, List.of(new FlashFlood()));
            harness.addMana(player1, ManaColor.BLUE, 1);

            harness.castInstant(player1, 0, 1, mountain.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Mountain");
            harness.assertInHand(player2, "Mountain");
        }

        @Test
        @DisplayName("Cannot target a non-Mountain permanent")
        void cannotTargetNonMountainPermanent() {
            Permanent goblin = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
            harness.setHand(player1, List.of(new FlashFlood()));
            harness.addMana(player1, ManaColor.BLUE, 1);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, goblin.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void returnsMountainToOwnerRatherThanController() {
            Mountain ownedMountain = new Mountain();
            ownedMountain.setOwnerId(player1.getId());
            Permanent mountain = harness.addToBattlefieldAndReturn(player2, ownedMountain);
            harness.setHand(player1, List.of(new FlashFlood()));
            harness.addMana(player1, ManaColor.BLUE, 1);

            harness.castInstant(player1, 0, 1, mountain.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Mountain");
            harness.assertInHand(player1, "Mountain");
            harness.assertNotInHand(player2, "Mountain");
        }

        @Test
        void returnsNonbasicLandThatIsCurrentlyMountain() {
            Permanent tower = harness.addToBattlefieldAndReturn(player2, new UrzasTower());
            harness.addToBattlefield(player2, new BloodMoon());
            harness.setHand(player1, List.of(new FlashFlood()));
            harness.addMana(player1, ManaColor.BLUE, 1);

            harness.castInstant(player1, 0, 1, tower.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Urza's Tower");
            harness.assertInHand(player2, "Urza's Tower");
            harness.assertOnBattlefield(player2, "Blood Moon");
        }

        @Test
        void doesNotReturnLandThatStopsBeingMountainBeforeResolution() {
            Permanent tower = harness.addToBattlefieldAndReturn(player2, new UrzasTower());
            Permanent moon = harness.addToBattlefieldAndReturn(player2, new BloodMoon());
            harness.setHand(player1, List.of(new FlashFlood()));
            harness.setHand(player2, List.of(new Boomerang()));
            harness.addMana(player1, ManaColor.BLUE, 1);
            harness.addMana(player2, ManaColor.BLUE, 2);

            harness.castInstant(player1, 0, 1, tower.getId());
            harness.castAndResolveInstant(player2, 0, moon.getId());
            harness.passBothPriorities();

            harness.assertOnBattlefield(player2, "Urza's Tower");
            harness.assertNotInHand(player2, "Urza's Tower");
            harness.assertInHand(player2, "Blood Moon");
            harness.assertInGraveyard(player1, "Flash Flood");
        }
    }
}
