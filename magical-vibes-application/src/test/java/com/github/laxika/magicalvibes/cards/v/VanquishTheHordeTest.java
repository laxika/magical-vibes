package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AvacynAngelOfHope;
import com.github.laxika.magicalvibes.cards.s.SanitariumSkeleton;
import com.github.laxika.magicalvibes.cards.t.TravelersAmulet;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VanquishTheHorde.class})
class VanquishTheHordeTest extends BaseCardTest {

    @Nested
    @DisplayName("Cost reduction")
    @CardUsed({VanquishTheHorde.class, SanitariumSkeleton.class})
    class CostReduction {

        @Test
        @DisplayName("Cannot cast with insufficient mana and no creatures")
        void cannotCastWithInsufficientMana() {
            harness.setHand(player1, List.of(new VanquishTheHorde()));
            harness.addMana(player1, ManaColor.WHITE, 2);

            assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        @DisplayName("Can cast for full cost {6}{W}{W} with no creatures")
        void canCastForFullCost() {
            harness.setHand(player1, List.of(new VanquishTheHorde()));
            harness.addMana(player1, ManaColor.WHITE, 8);

            harness.castSorcery(player1, 0, 0);

            GameData gd = harness.getGameData();
            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.getFirst();
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Cost is reduced by 1 for each creature on the battlefield")
        void costReducedByCreatureCount() {
            harness.addToBattlefield(player1, new SanitariumSkeleton());
            harness.addToBattlefield(player2, new SanitariumSkeleton());
            harness.setHand(player1, List.of(new VanquishTheHorde()));
            // 2 creatures: {4}{W}{W}
            harness.addMana(player1, ManaColor.WHITE, 6);

            harness.castSorcery(player1, 0, 0);

            GameData gd = harness.getGameData();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Cost cannot be reduced below {W}{W}")
        void costCannotGoBelowColoredMana() {
            for (int i = 0; i < 4; i++) {
                harness.addToBattlefield(player1, new SanitariumSkeleton());
            }
            for (int i = 0; i < 3; i++) {
                harness.addToBattlefield(player2, new SanitariumSkeleton());
            }
            harness.setHand(player1, List.of(new VanquishTheHorde()));
            // 7 creatures: generic floors at 0; still need {W}{W}
            harness.addMana(player1, ManaColor.WHITE, 2);

            harness.castSorcery(player1, 0, 0);

            GameData gd = harness.getGameData();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Insufficient mana with partial cost reduction still fails")
        void insufficientManaWithPartialReduction() {
            harness.addToBattlefield(player1, new SanitariumSkeleton()); // cost {5}{W}{W} = 7
            harness.setHand(player1, List.of(new VanquishTheHorde()));
            harness.addMana(player1, ManaColor.WHITE, 6);

            assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }
    }

    @Nested
    @DisplayName("Destroy all creatures")
    @CardUsed({VanquishTheHorde.class, SanitariumSkeleton.class})
    class DestroyEffect {

        @Test
        @DisplayName("Destroys all creatures on both battlefields")
        void destroysAllCreatures() {
            harness.addToBattlefield(player1, new SanitariumSkeleton());
            harness.addToBattlefield(player2, new SanitariumSkeleton());
            harness.setHand(player1, List.of(new VanquishTheHorde()));
            harness.addMana(player1, ManaColor.WHITE, 6); // 2 creatures: {4}{W}{W}

            harness.castAndResolveSorcery(player1, 0, 0);

            harness.assertNotOnBattlefield(player1, "Sanitarium Skeleton");
            harness.assertNotOnBattlefield(player2, "Sanitarium Skeleton");
            harness.assertInGraveyard(player1, "Vanquish the Horde");
        }
    }

    @Test
    @CardUsed({SanitariumSkeleton.class})
    @DisplayName("Creature reduction still requires two white mana")
    void reductionDoesNotPayColoredCost() {
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player2, new SanitariumSkeleton());
        }
        harness.setHand(player1, List.of(new VanquishTheHorde()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @CardUsed({SanitariumSkeleton.class, TravelersAmulet.class})
    @DisplayName("Noncreature permanents do not reduce the casting cost")
    void noncreaturesDoNotReduceCost() {
        harness.addToBattlefield(player1, new SanitariumSkeleton());
        harness.addToBattlefield(player2, new TravelersAmulet());
        harness.setHand(player1, List.of(new VanquishTheHorde()));
        harness.addMana(player1, ManaColor.WHITE, 8);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @CardUsed({SanitariumSkeleton.class})
    @DisplayName("Creatures outside the battlefield do not reduce the casting cost")
    void creaturesInOtherZonesDoNotReduceCost() {
        harness.setHand(player1, List.of(new VanquishTheHorde(), new SanitariumSkeleton()));
        harness.setGraveyard(player2, List.of(new SanitariumSkeleton()));
        harness.setExile(player2, List.of(new SanitariumSkeleton()));
        harness.addMana(player1, ManaColor.WHITE, 8);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @CardUsed({AvacynAngelOfHope.class, SanitariumSkeleton.class, TravelersAmulet.class})
    @DisplayName("Indestructible creatures count toward reduction and survive the wipe")
    void indestructibleCreaturesSurviveAndNoncreaturesAreUnaffected() {
        harness.addToBattlefield(player1, new AvacynAngelOfHope());
        harness.addToBattlefield(player1, new SanitariumSkeleton());
        harness.addToBattlefield(player2, new SanitariumSkeleton());
        harness.addToBattlefield(player2, new TravelersAmulet());
        harness.setHand(player1, List.of(new VanquishTheHorde()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player1, "Avacyn, Angel of Hope");
        harness.assertOnBattlefield(player1, "Sanitarium Skeleton");
        harness.assertNotInGraveyard(player1, "Sanitarium Skeleton");
        harness.assertNotOnBattlefield(player2, "Sanitarium Skeleton");
        harness.assertInGraveyard(player2, "Sanitarium Skeleton");
        harness.assertOnBattlefield(player2, "Traveler's Amulet");
        harness.assertInGraveyard(player1, "Vanquish the Horde");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Resolves with no creatures and no targets")
    void resolvesOnEmptyBattlefield() {
        harness.setHand(player1, List.of(new VanquishTheHorde()));
        harness.addMana(player1, ManaColor.WHITE, 8);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Vanquish the Horde");
    }
}
