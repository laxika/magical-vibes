package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AetherAdept;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.ThaliaGuardianOfThraben;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WizardsLightningTest extends BaseCardTest {

    @Nested
    @DisplayName("Damage")
    class Damage {

        @Test
        @DisplayName("Deals 3 damage to target player")
        void deals3DamageToPlayer() {
            harness.setHand(player1, List.of(new WizardsLightning()));
            harness.addMana(player1, ManaColor.RED, 3);

            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        }

        @Test
        @DisplayName("Deals 3 damage to target creature")
        void deals3DamageToCreature() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
            harness.setHand(player1, List.of(new WizardsLightning()));
            harness.addMana(player1, ManaColor.RED, 3);

            harness.castInstant(player1, 0, creatureId);
            harness.passBothPriorities();

            Permanent permanent = gd.playerBattlefields.get(player2.getId()).stream()
                    .filter(p -> p.getId().equals(creatureId))
                    .findFirst()
                    .orElse(null);
            // 3 damage kills a 2/2, so it should be in graveyard
            assertThat(permanent).isNull();
            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Goes to graveyard after resolving")
        void goesToGraveyardAfterResolving() {
            harness.setHand(player1, List.of(new WizardsLightning()));
            harness.addMana(player1, ManaColor.RED, 3);

            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Wizard's Lightning"));
        }
    }

    @Nested
    @DisplayName("Cost reduction")
    class CostReduction {

        @Test
        @DisplayName("Costs full {2}{R} without a Wizard on the battlefield")
        void fullCostWithoutWizard() {
            harness.setHand(player1, List.of(new WizardsLightning()));
            harness.addMana(player1, ManaColor.RED, 3);

            harness.castInstant(player1, 0, player2.getId());

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Cannot cast with only 2 mana and no Wizard")
        void cannotCastWithInsufficientManaNoWizard() {
            harness.setHand(player1, List.of(new WizardsLightning()));
            harness.addMana(player1, ManaColor.RED, 2);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        @DisplayName("Costs only {R} when controlling a Wizard")
        void reducedCostWithWizard() {
            // AetherAdept is a Human Wizard
            harness.addToBattlefield(player1, new AetherAdept());
            harness.setHand(player1, List.of(new WizardsLightning()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castInstant(player1, 0, player2.getId());

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Deals 3 damage even when cast at reduced cost")
        void deals3DamageAtReducedCost() {
            harness.addToBattlefield(player1, new AetherAdept());
            harness.setHand(player1, List.of(new WizardsLightning()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        }

        @Test
        @DisplayName("Cannot cast with 0 mana even with a Wizard")
        void cannotCastWith0ManaEvenWithWizard() {
            harness.addToBattlefield(player1, new AetherAdept());
            harness.setHand(player1, List.of(new WizardsLightning()));

            assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }
    }

    @Nested
    @DisplayName("Cost reduction with cost increase interaction")
    class CostReductionWithCostIncreaseInteraction {

        @Test
        @DisplayName("Wizard reduction and Thalia increase partially cancel: costs {1}{R} with Wizard and Thalia")
        void wizardReductionAndThaliaIncreasePartiallyCancel() {
            // AetherAdept is a Human Wizard — gives {2} reduction
            harness.addToBattlefield(player1, new AetherAdept());
            // Thalia increases noncreature spells by {1}
            harness.addToBattlefield(player2, new ThaliaGuardianOfThraben());
            harness.setHand(player1, List.of(new WizardsLightning()));
            // Base {2}{R}, -2 Wizard, +1 Thalia = net -1 → {1}{R}
            harness.addMana(player1, ManaColor.RED, 2);

            harness.castInstant(player1, 0, player2.getId());

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Cannot cast with only {R} when Wizard reduction is offset by Thalia")
        void cannotCastWithOnlyRedWhenThaliaOffsetsWizard() {
            harness.addToBattlefield(player1, new AetherAdept());
            harness.addToBattlefield(player2, new ThaliaGuardianOfThraben());
            harness.setHand(player1, List.of(new WizardsLightning()));
            // Only {R} — not enough, needs {1}{R}
            harness.addMana(player1, ManaColor.RED, 1);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        @DisplayName("Still deals 3 damage when cost is modified by both Wizard and Thalia")
        void stillDeals3DamageWithBothModifiers() {
            harness.addToBattlefield(player1, new AetherAdept());
            harness.addToBattlefield(player2, new ThaliaGuardianOfThraben());
            harness.setHand(player1, List.of(new WizardsLightning()));
            harness.addMana(player1, ManaColor.RED, 2);

            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        }
    }
}
