package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.p.PrimordialWurm;
import com.github.laxika.magicalvibes.cards.m.MerfolkTrickster;
import com.github.laxika.magicalvibes.cards.b.BlinkOfAnEye;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AcademyJourneymage.class, PrimordialWurm.class, MerfolkTrickster.class, BlinkOfAnEye.class})
class AcademyJourneymageTest extends BaseCardTest {

    @Nested
    @DisplayName("ETB bounce")
    @CardUsed({AcademyJourneymage.class, PrimordialWurm.class, BlinkOfAnEye.class})
    class EtbBounce {

        @Test
        @DisplayName("Bounce does not resolve when its target has already left the battlefield")
        @CardUsed({AcademyJourneymage.class, PrimordialWurm.class, BlinkOfAnEye.class})
        void targetLeavesBeforeTriggerResolves() {
            harness.addToBattlefield(player2, new PrimordialWurm());
            UUID targetId = harness.getPermanentId(player2, "Primordial Wurm");
            castJourneymage(player2, "Primordial Wurm");
            harness.passBothPriorities();
            harness.setHand(player2, List.of(new BlinkOfAnEye()));
            harness.addMana(player2, ManaColor.BLUE, 2);

            harness.castAndResolveInstant(player2, 0, targetId);
            harness.passBothPriorities();

            harness.assertOnBattlefield(player1, "Academy Journeymage");
            harness.assertNotOnBattlefield(player2, "Primordial Wurm");
            assertThat(gd.playerHands.get(player2.getId()))
                    .extracting(card -> card.getName()).containsExactly("Primordial Wurm");
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Can cast and enter when no opponent controls a creature")
        void entersWithoutLegalBounceTarget() {
            harness.addToBattlefield(player1, new PrimordialWurm());
            harness.setHand(player1, List.of(new AcademyJourneymage()));
            harness.addMana(player1, ManaColor.BLUE, 5);

            harness.castCreature(player1, 0);
            harness.passBothPriorities();

            harness.assertOnBattlefield(player1, "Academy Journeymage");
            harness.assertOnBattlefield(player1, "Primordial Wurm");
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Bounce returns an opponent-controlled creature to its owner")
        void bouncesToOwnerInsteadOfController() {
            harness.setHand(player1, List.of(new AcademyJourneymage()));
            harness.setHand(player2, List.of());
            harness.addToBattlefield(player2, new PrimordialWurm());
            UUID targetId = harness.getPermanentId(player2, "Primordial Wurm");
            gd.stolenCreatures.put(targetId, player1.getId());
            harness.addMana(player1, ManaColor.BLUE, 5);

            harness.castCreature(player1, 0, targetId);
            harness.passBothPriorities();
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Primordial Wurm");
            harness.assertInHand(player1, "Primordial Wurm");
            harness.assertNotInHand(player2, "Primordial Wurm");
        }

        @Test
        @DisplayName("ETB trigger goes on the stack when Academy Journeymage enters")
        void etbTriggerGoesOnStack() {
            harness.addToBattlefield(player2, new PrimordialWurm());
            castJourneymage(player2, "Primordial Wurm");
            harness.passBothPriorities(); // resolve creature spell

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Academy Journeymage");
        }

        @Test
        @DisplayName("ETB resolves: target creature is returned to opponent's hand")
        void etbBouncesOpponentCreature() {
            harness.addToBattlefield(player2, new PrimordialWurm());
            castJourneymage(player2, "Primordial Wurm");
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            harness.assertNotOnBattlefield(player2, "Primordial Wurm");
            harness.assertInHand(player2, "Primordial Wurm");
        }

        @Test
        @DisplayName("Academy Journeymage enters the battlefield after resolution")
        void journeymageEntersBattlefield() {
            harness.addToBattlefield(player2, new PrimordialWurm());
            castJourneymage(player2, "Primordial Wurm");
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            harness.assertOnBattlefield(player1, "Academy Journeymage");
        }

        @Test
        @DisplayName("Cannot target own creature")
        void cannotTargetOwnCreature() {
            harness.addToBattlefield(player1, new PrimordialWurm());
            UUID ownBearId = harness.getPermanentId(player1, "Primordial Wurm");
            harness.setHand(player1, List.of(new AcademyJourneymage()));
            harness.addMana(player1, ManaColor.BLUE, 5);

            assertThatThrownBy(() -> harness.castCreature(player1, 0, ownBearId))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Cost reduction")
    @CardUsed({AcademyJourneymage.class, PrimordialWurm.class, MerfolkTrickster.class})
    class CostReduction {

        @Test
        @DisplayName("An opponent's Wizard does not reduce the casting cost")
        void opponentsWizardDoesNotReduceCost() {
            harness.addToBattlefield(player2, new MerfolkTrickster());
            UUID targetId = harness.getPermanentId(player2, "Merfolk Trickster");
            harness.setHand(player1, List.of(new AcademyJourneymage()));
            harness.addMana(player1, ManaColor.BLUE, 4);

            assertThatThrownBy(() -> harness.castCreature(player1, 0, targetId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        @DisplayName("Multiple Wizards still reduce the cost by only one generic mana")
        void multipleWizardsReduceCostOnlyOnce() {
            harness.addToBattlefield(player1, new MerfolkTrickster());
            harness.addToBattlefield(player1, new MerfolkTrickster());
            harness.addToBattlefield(player2, new PrimordialWurm());
            UUID targetId = harness.getPermanentId(player2, "Primordial Wurm");
            harness.setHand(player1, List.of(new AcademyJourneymage()));
            harness.addMana(player1, ManaColor.BLUE, 5);

            harness.castCreature(player1, 0, targetId);

            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        }

        @Test
        @DisplayName("Cost reduction cannot pay the required blue mana")
        void stillRequiresBlueManaWithWizard() {
            harness.addToBattlefield(player1, new MerfolkTrickster());
            harness.addToBattlefield(player2, new PrimordialWurm());
            UUID targetId = harness.getPermanentId(player2, "Primordial Wurm");
            harness.setHand(player1, List.of(new AcademyJourneymage()));
            harness.addMana(player1, ManaColor.COLORLESS, 4);

            assertThatThrownBy(() -> harness.castCreature(player1, 0, targetId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        @DisplayName("Costs full {4}{U} without a Wizard on the battlefield")
        void fullCostWithoutWizard() {
            harness.addToBattlefield(player2, new PrimordialWurm());
            harness.setHand(player1, List.of(new AcademyJourneymage()));
            harness.addMana(player1, ManaColor.BLUE, 5);
            UUID targetId = harness.getPermanentId(player2, "Primordial Wurm");

            harness.castCreature(player1, 0, 0, targetId);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Cannot cast with only 4 mana and no Wizard")
        void cannotCastWithInsufficientManaNoWizard() {
            harness.addToBattlefield(player2, new PrimordialWurm());
            UUID targetId = harness.getPermanentId(player2, "Primordial Wurm");
            harness.setHand(player1, List.of(new AcademyJourneymage()));
            harness.addMana(player1, ManaColor.BLUE, 4);

            assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, targetId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        @DisplayName("Costs {3}{U} when controlling a Wizard")
        void reducedCostWithWizard() {
            // Merfolk Trickster is a Wizard
            harness.addToBattlefield(player1, new MerfolkTrickster());
            harness.addToBattlefield(player2, new PrimordialWurm());
            UUID targetId = harness.getPermanentId(player2, "Primordial Wurm");
            harness.setHand(player1, List.of(new AcademyJourneymage()));
            harness.addMana(player1, ManaColor.BLUE, 4);

            harness.castCreature(player1, 0, 0, targetId);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Cannot cast with only 3 mana even with a Wizard")
        void cannotCastWith3ManaEvenWithWizard() {
            harness.addToBattlefield(player1, new MerfolkTrickster());
            harness.addToBattlefield(player2, new PrimordialWurm());
            UUID targetId = harness.getPermanentId(player2, "Primordial Wurm");
            harness.setHand(player1, List.of(new AcademyJourneymage()));
            harness.addMana(player1, ManaColor.BLUE, 3);

            assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, targetId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }
    }

    private void castJourneymage(com.github.laxika.magicalvibes.model.Player targetOwner, String targetName) {
        UUID targetId = harness.getPermanentId(targetOwner, targetName);
        harness.setHand(player1, List.of(new AcademyJourneymage()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castCreature(player1, 0, targetId);
    }
}
