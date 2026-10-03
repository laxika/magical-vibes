package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FortressCrab;
import com.github.laxika.magicalvibes.cards.d.DarkthicketWolf;
import com.github.laxika.magicalvibes.cards.m.ManorGargoyle;
import com.github.laxika.magicalvibes.cards.s.SilverInlaidDagger;
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

@CardUsed({BlasphemousAct.class, DarkthicketWolf.class, FortressCrab.class,
        ManorGargoyle.class, SilverInlaidDagger.class})
class BlasphemousActTest extends BaseCardTest {

    @Nested
    @CardUsed({BlasphemousAct.class, DarkthicketWolf.class, ManorGargoyle.class, SilverInlaidDagger.class})
    @DisplayName("Cost reduction")
    class CostReduction {

        @Test
        @DisplayName("Cannot cast with insufficient mana and no creatures")
        void cannotCastWithInsufficientMana() {
            harness.setHand(player1, List.of(new BlasphemousAct()));
            harness.addMana(player1, ManaColor.RED, 1);

            assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        @DisplayName("Can cast for full cost {8}{R} with no creatures")
        void canCastForFullCost() {
            harness.setHand(player1, List.of(new BlasphemousAct()));
            harness.addMana(player1, ManaColor.RED, 9);

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
            harness.addToBattlefield(player1, new DarkthicketWolf());
            harness.addToBattlefield(player2, new DarkthicketWolf());
            harness.setHand(player1, List.of(new BlasphemousAct()));
            // 2 creatures = cost reduced by 2, so {6}{R} = 7 mana
            harness.addMana(player1, ManaColor.RED, 7);

            harness.castSorcery(player1, 0, 0);

            GameData gd = harness.getGameData();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Cost cannot be reduced below {R}")
        void costCannotGoBelowColoredMana() {
            // Add 9 creatures â€” would reduce generic cost to -1, but it floors at 0
            for (int i = 0; i < 5; i++) {
                harness.addToBattlefield(player1, new DarkthicketWolf());
            }
            for (int i = 0; i < 4; i++) {
                harness.addToBattlefield(player2, new DarkthicketWolf());
            }
            harness.setHand(player1, List.of(new BlasphemousAct()));
            // With 9 creatures, generic cost should be 0, total = just {R}
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castSorcery(player1, 0, 0);

            GameData gd = harness.getGameData();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Insufficient mana with partial cost reduction still fails")
        void insufficientManaWithPartialReduction() {
            harness.addToBattlefield(player1, new DarkthicketWolf()); // 1 creature = cost {7}{R} = 8
            harness.setHand(player1, List.of(new BlasphemousAct()));
            harness.addMana(player1, ManaColor.RED, 7); // Need 8, only have 7

            assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        void noncreatureArtifactsDoNotReduceCost() {
            harness.addToBattlefield(player1, new SilverInlaidDagger());
            harness.addToBattlefield(player2, new SilverInlaidDagger());
            harness.setHand(player1, List.of(new BlasphemousAct()));
            harness.addMana(player1, ManaColor.RED, 8);

            assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        void artifactCreaturesReduceCost() {
            harness.addToBattlefield(player2, new ManorGargoyle());
            harness.setHand(player1, List.of(new BlasphemousAct()));
            harness.addMana(player1, ManaColor.RED, 8);

            harness.castSorcery(player1, 0, 0);

            assertThat(harness.getGameData().stack).hasSize(1);
            assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotal()).isZero();
        }
    }

    @Nested
    @CardUsed({BlasphemousAct.class, DarkthicketWolf.class, FortressCrab.class,
            ManorGargoyle.class, SilverInlaidDagger.class})
    @DisplayName("Damage effect")
    class DamageEffect {

        @Test
        @DisplayName("Deals 13 damage to each creature")
        void deals13DamageToEachCreature() {
            harness.addToBattlefield(player1, new DarkthicketWolf()); // 2/2
            harness.addToBattlefield(player2, new FortressCrab());  // 1/6
            harness.setHand(player1, List.of(new BlasphemousAct()));
            harness.addMana(player1, ManaColor.RED, 9);
            harness.setLife(player1, 20);
            harness.setLife(player2, 20);

            harness.castAndResolveSorcery(player1, 0, 0);

            // Both creatures have lethal damage.
            harness.assertNotOnBattlefield(player1, "Darkthicket Wolf");
            harness.assertNotOnBattlefield(player2, "Fortress Crab");
            // Players should NOT take damage
            harness.assertLife(player1, 20);
            harness.assertLife(player2, 20);
        }

        @Test
        @DisplayName("Goes to graveyard after resolving")
        void goesToGraveyardAfterResolving() {
            harness.setHand(player1, List.of(new BlasphemousAct()));
            harness.addMana(player1, ManaColor.RED, 9);

            harness.castAndResolveSorcery(player1, 0, 0);

            GameData gd = harness.getGameData();
            assertThat(gd.stack).isEmpty();
            harness.assertInGraveyard(player1, "Blasphemous Act");
        }

        @Test
        void indestructibleCreatureReceivesExactlyThirteenDamageAndArtifactsAreUntouched() {
            harness.addToBattlefield(player1, new ManorGargoyle());
            harness.addToBattlefield(player2, new SilverInlaidDagger());
            harness.setHand(player1, List.of(new BlasphemousAct()));
            harness.addMana(player1, ManaColor.RED, 8);

            harness.castAndResolveSorcery(player1, 0, 0);

            harness.assertOnBattlefield(player1, "Manor Gargoyle");
            assertThat(harness.getGameData().playerBattlefields.get(player1.getId()).getFirst()
                    .getMarkedDamage()).isEqualTo(13);
            harness.assertOnBattlefield(player2, "Silver-Inlaid Dagger");
            assertThat(harness.getGameData().playerBattlefields.get(player2.getId()).getFirst()
                    .getMarkedDamage()).isZero();
        }
    }
}
