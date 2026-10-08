package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AcademyJourneymage;
import com.github.laxika.magicalvibes.cards.c.CabalEvangel;
import com.github.laxika.magicalvibes.cards.j.JayaBallard;
import com.github.laxika.magicalvibes.cards.t.ThaliaGuardianOfThraben;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WizardsLightning.class, AcademyJourneymage.class, CabalEvangel.class,
        JayaBallard.class, ThaliaGuardianOfThraben.class})
class WizardsLightningTest extends BaseCardTest {

    @Nested
    @DisplayName("Damage")
    @CardUsed({WizardsLightning.class, CabalEvangel.class, JayaBallard.class})
    class Damage {

        @Test
        @DisplayName("Deals 3 damage to a planeswalker by removing loyalty counters")
        void deals3DamageToPlaneswalker() {
            Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JayaBallard());
            planeswalker.setCounterCount(CounterType.LOYALTY, 5);
            harness.setHand(player1, List.of(new WizardsLightning()));
            harness.addMana(player1, ManaColor.RED, 3);

            harness.castInstant(player1, 0, planeswalker.getId());
            harness.passBothPriorities();

            assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
            harness.assertOnBattlefield(player2, "Jaya Ballard");
            harness.assertLife(player2, 20);
        }

        @Test
        @DisplayName("Can target its controller")
        void canDamageItsController() {
            harness.setHand(player1, List.of(new WizardsLightning()));
            harness.addMana(player1, ManaColor.RED, 3);

            harness.castInstant(player1, 0, player1.getId());
            harness.passBothPriorities();

            harness.assertLife(player1, 17);
            harness.assertLife(player2, 20);
        }

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
            harness.addToBattlefield(player2, new CabalEvangel());
            UUID creatureId = harness.getPermanentId(player2, "Cabal Evangel");
            harness.setHand(player1, List.of(new WizardsLightning()));
            harness.addMana(player1, ManaColor.RED, 3);

            harness.castInstant(player1, 0, creatureId);
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Cabal Evangel");
            harness.assertInGraveyard(player2, "Cabal Evangel");
        }

        @Test
        @DisplayName("Goes to graveyard after resolving")
        void goesToGraveyardAfterResolving() {
            harness.setHand(player1, List.of(new WizardsLightning()));
            harness.addMana(player1, ManaColor.RED, 3);

            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();

            harness.assertInGraveyard(player1, "Wizard's Lightning");
        }
    }

    @Nested
    @DisplayName("Cost reduction")
    @CardUsed({WizardsLightning.class, AcademyJourneymage.class})
    class CostReduction {

        @Test
        @DisplayName("An opponent's Wizard does not reduce the cost")
        void opponentsWizardDoesNotReduceCost() {
            harness.addToBattlefield(player2, new AcademyJourneymage());
            harness.setHand(player1, List.of(new WizardsLightning()));
            harness.addMana(player1, ManaColor.RED, 2);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        @DisplayName("A Wizard in the graveyard does not reduce the cost")
        void wizardInGraveyardDoesNotReduceCost() {
            harness.setGraveyard(player1, List.of(new AcademyJourneymage()));
            harness.setHand(player1, List.of(new WizardsLightning()));
            harness.addMana(player1, ManaColor.RED, 2);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        @DisplayName("Multiple Wizards still reduce the cost by only two generic mana")
        void multipleWizardsDoNotMultiplyReduction() {
            harness.addToBattlefield(player1, new AcademyJourneymage());
            harness.addToBattlefield(player1, new AcademyJourneymage());
            harness.setHand(player1, List.of(new WizardsLightning()));
            harness.addMana(player1, ManaColor.RED, 3);

            harness.castInstant(player1, 0, player2.getId());

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        }

        @Test
        @DisplayName("The Wizard reduction cannot pay the red mana requirement")
        void wizardReductionStillRequiresRedMana() {
            harness.addToBattlefield(player1, new AcademyJourneymage());
            harness.setHand(player1, List.of(new WizardsLightning()));
            harness.addMana(player1, ManaColor.BLUE, 3);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        @DisplayName("Losing the Wizard after casting does not affect resolution")
        void losingWizardAfterCastingDoesNotAffectResolution() {
            harness.addToBattlefield(player1, new AcademyJourneymage());
            UUID wizardId = harness.getPermanentId(player1, "Academy Journeymage");
            harness.setHand(player1, List.of(new WizardsLightning()));
            harness.setHand(player2, List.of(new WizardsLightning()));
            harness.addMana(player1, ManaColor.RED, 1);
            harness.addMana(player2, ManaColor.RED, 3);

            harness.castInstant(player1, 0, player2.getId());
            harness.passPriority(player1);
            harness.castInstant(player2, 0, wizardId);
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player1, "Academy Journeymage");
            harness.assertInGraveyard(player1, "Academy Journeymage");
            assertThat(gd.stack).hasSize(1);
            harness.passBothPriorities();

            harness.assertLife(player2, 17);
            harness.assertInGraveyard(player1, "Wizard's Lightning");
        }

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
            // AcademyJourneymage is a Human Wizard
            harness.addToBattlefield(player1, new AcademyJourneymage());
            harness.setHand(player1, List.of(new WizardsLightning()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castInstant(player1, 0, player2.getId());

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Deals 3 damage even when cast at reduced cost")
        void deals3DamageAtReducedCost() {
            harness.addToBattlefield(player1, new AcademyJourneymage());
            harness.setHand(player1, List.of(new WizardsLightning()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        }

        @Test
        @DisplayName("Cannot cast with 0 mana even with a Wizard")
        void cannotCastWith0ManaEvenWithWizard() {
            harness.addToBattlefield(player1, new AcademyJourneymage());
            harness.setHand(player1, List.of(new WizardsLightning()));

            assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }
    }

    @Nested
    @DisplayName("Cost reduction with cost increase interaction")
    @CardUsed({WizardsLightning.class, AcademyJourneymage.class, ThaliaGuardianOfThraben.class})
    class CostReductionWithCostIncreaseInteraction {

        @Test
        @DisplayName("Multiple Wizards do not multiply the reduction against Thalia's tax")
        void multipleWizardsReduceCostOnlyOnceWithThalia() {
            harness.addToBattlefield(player1, new AcademyJourneymage());
            harness.addToBattlefield(player1, new AcademyJourneymage());
            harness.addToBattlefield(player2, new ThaliaGuardianOfThraben());
            harness.setHand(player1, List.of(new WizardsLightning()));
            harness.addMana(player1, ManaColor.RED, 2);

            harness.castInstant(player1, 0, player2.getId());

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Wizard reduction and Thalia increase partially cancel: costs {1}{R} with Wizard and Thalia")
        void wizardReductionAndThaliaIncreasePartiallyCancel() {
            // AcademyJourneymage is a Human Wizard — gives {2} reduction
            harness.addToBattlefield(player1, new AcademyJourneymage());
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
            harness.addToBattlefield(player1, new AcademyJourneymage());
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
            harness.addToBattlefield(player1, new AcademyJourneymage());
            harness.addToBattlefield(player2, new ThaliaGuardianOfThraben());
            harness.setHand(player1, List.of(new WizardsLightning()));
            harness.addMana(player1, ManaColor.RED, 2);

            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        }
    }
}
