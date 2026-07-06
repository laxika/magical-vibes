package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostIfTargetingStackEntryEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BrushOffTest extends BaseCardTest {

    

    @Nested
    @DisplayName("Cost reduction")
    class CostReduction {

        @Test
        @DisplayName("Costs {1}{U} when targeting an instant spell")
        void reducedCostWhenTargetingInstant() {
            Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
            Shock shock = new Shock();
            harness.setHand(player1, List.of(shock));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.setHand(player2, List.of(new BrushOff()));
            harness.addMana(player2, ManaColor.BLUE, 2);

            harness.castInstant(player1, 0, bears.getId());
            harness.passPriority(player1);
            harness.castInstant(player2, 0, shock.getId());

            assertThat(gd.stack).hasSize(2);
            assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        }

        @Test
        @DisplayName("Costs {1}{U} when targeting a sorcery spell")
        void reducedCostWhenTargetingSorcery() {
            Divination divination = new Divination();
            harness.setHand(player1, List.of(divination));
            harness.addMana(player1, ManaColor.BLUE, 3);

            harness.setHand(player2, List.of(new BrushOff()));
            harness.addMana(player2, ManaColor.BLUE, 2);

            harness.castSorcery(player1, 0, 0);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, divination.getId());

            assertThat(gd.stack).hasSize(2);
            assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        }

        @Test
        @DisplayName("Costs full {2}{U}{U} when targeting a creature spell")
        void fullCostWhenTargetingCreatureSpell() {
            GrizzlyBears bears = new GrizzlyBears();
            harness.setHand(player1, List.of(bears));
            harness.addMana(player1, ManaColor.GREEN, 2);

            harness.setHand(player2, List.of(new BrushOff()));
            harness.addMana(player2, ManaColor.BLUE, 4);

            harness.castCreature(player1, 0);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, bears.getId());

            assertThat(gd.stack).hasSize(2);
            assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        }

        @Test
        @DisplayName("Cannot cast with reduced-cost mana when targeting a creature spell")
        void cannotCastWithReducedCostManaForCreatureTarget() {
            GrizzlyBears bears = new GrizzlyBears();
            harness.setHand(player1, List.of(bears));
            harness.addMana(player1, ManaColor.GREEN, 2);

            harness.setHand(player2, List.of(new BrushOff()));
            harness.addMana(player2, ManaColor.BLUE, 2);

            harness.castCreature(player1, 0);
            harness.passPriority(player1);

            assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Cannot use reduced cost when targeting creature even if an instant is on the stack")
        void cannotUseReducedCostWithCreatureTargetWhileInstantOnStack() {
            Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
            Shock shock = new Shock();
            GrizzlyBears bearsToCast = new GrizzlyBears();
            harness.setHand(player1, List.of(bearsToCast, shock));
            harness.addMana(player1, ManaColor.GREEN, 2);
            harness.addMana(player1, ManaColor.RED, 1);

            harness.setHand(player2, List.of(new BrushOff()));
            harness.addMana(player2, ManaColor.BLUE, 2);

            harness.castCreature(player1, 0);
            harness.passPriority(player1);
            harness.addMana(player1, ManaColor.RED, 1);
            harness.castInstant(player1, 0, bears.getId());
            harness.passPriority(player1);

            assertThatThrownBy(() -> harness.castInstant(player2, 0, bearsToCast.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Countering")
    class Countering {

        @Test
        @DisplayName("Counters an instant spell at reduced cost")
        void countersInstantAtReducedCost() {
            Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
            Shock shock = new Shock();
            harness.setHand(player1, List.of(shock));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.setHand(player2, List.of(new BrushOff()));
            harness.addMana(player2, ManaColor.BLUE, 2);

            harness.castInstant(player1, 0, bears.getId());
            harness.passPriority(player1);
            harness.castInstant(player2, 0, shock.getId());
            harness.passBothPriorities();

            GameData gd = harness.getGameData();
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Shock"));
            assertThat(gd.stack).noneMatch(se -> se.getCard().getName().equals("Shock"));
            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Brush Off"));
        }

        @Test
        @DisplayName("Counters a creature spell at full cost")
        void countersCreatureAtFullCost() {
            GrizzlyBears bears = new GrizzlyBears();
            harness.setHand(player1, List.of(bears));
            harness.addMana(player1, ManaColor.GREEN, 2);

            harness.setHand(player2, List.of(new BrushOff()));
            harness.addMana(player2, ManaColor.BLUE, 4);

            harness.castCreature(player1, 0);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, bears.getId());
            harness.passBothPriorities();

            GameData gd = harness.getGameData();
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        }
    }
}
