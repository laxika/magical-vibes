package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostPerCreatureOnBattlefieldEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BlasphemousActTest extends BaseCardTest {

    @Test
    @DisplayName("Blasphemous Act has correct effects")
    void hasCorrectEffects() {
        BlasphemousAct card = new BlasphemousAct();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(ReduceOwnCastCostPerCreatureOnBattlefieldEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(MassDamageEffect.class);
    }

    @Nested
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
            assertThat(entry.getCard().getName()).isEqualTo("Blasphemous Act");
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Cost is reduced by 1 for each creature on the battlefield")
        void costReducedByCreatureCount() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.addToBattlefield(player2, new GrizzlyBears());
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
            // Add 9 creatures — would reduce generic cost to -1, but it floors at 0
            for (int i = 0; i < 5; i++) {
                harness.addToBattlefield(player1, new GrizzlyBears());
            }
            for (int i = 0; i < 4; i++) {
                harness.addToBattlefield(player2, new GrizzlyBears());
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
            harness.addToBattlefield(player1, new GrizzlyBears()); // 1 creature = cost {7}{R} = 8
            harness.setHand(player1, List.of(new BlasphemousAct()));
            harness.addMana(player1, ManaColor.RED, 7); // Need 8, only have 7

            assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }
    }

    @Nested
    @DisplayName("Damage effect")
    class DamageEffect {

        @Test
        @DisplayName("Deals 13 damage to each creature")
        void deals13DamageToEachCreature() {
            harness.addToBattlefield(player1, new GrizzlyBears()); // 2/2
            harness.addToBattlefield(player2, new GiantSpider());  // 2/4
            harness.setHand(player1, List.of(new BlasphemousAct()));
            harness.addMana(player1, ManaColor.RED, 9);
            harness.setLife(player1, 20);
            harness.setLife(player2, 20);

            harness.castSorcery(player1, 0, 0);
            harness.passBothPriorities();

            GameData gd = harness.getGameData();
            // Both creatures should be dead — 13 damage kills anything
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Giant Spider"));
            // Players should NOT take damage
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        }

        @Test
        @DisplayName("Goes to graveyard after resolving")
        void goesToGraveyardAfterResolving() {
            harness.setHand(player1, List.of(new BlasphemousAct()));
            harness.addMana(player1, ManaColor.RED, 9);

            harness.castSorcery(player1, 0, 0);
            harness.passBothPriorities();

            GameData gd = harness.getGameData();
            assertThat(gd.stack).isEmpty();
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Blasphemous Act"));
        }
    }
}
