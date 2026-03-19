package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

class AcademyJourneymageTest extends BaseCardTest {

    @Nested
    @DisplayName("ETB bounce")
    class EtbBounce {

        @Test
        @DisplayName("ETB trigger goes on the stack when Academy Journeymage enters")
        void etbTriggerGoesOnStack() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            castJourneymage(player2, "Grizzly Bears");
            harness.passBothPriorities(); // resolve creature spell

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Academy Journeymage");
        }

        @Test
        @DisplayName("ETB resolves: target creature is returned to opponent's hand")
        void etbBouncesOpponentCreature() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            castJourneymage(player2, "Grizzly Bears");
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            assertThat(gd.playerHands.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Academy Journeymage enters the battlefield after resolution")
        void journeymageEntersBattlefield() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            castJourneymage(player2, "Grizzly Bears");
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Academy Journeymage"));
        }

        @Test
        @DisplayName("Cannot target own creature")
        void cannotTargetOwnCreature() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            UUID ownBearId = harness.getPermanentId(player1, "Grizzly Bears");
            harness.setHand(player1, List.of(new AcademyJourneymage()));
            harness.addMana(player1, ManaColor.BLUE, 5);

            assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, ownBearId, null))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Cost reduction")
    class CostReduction {

        @Test
        @DisplayName("Costs full {4}{U} without a Wizard on the battlefield")
        void fullCostWithoutWizard() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new AcademyJourneymage()));
            harness.addMana(player1, ManaColor.BLUE, 5);
            UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

            harness.castCreature(player1, 0, 0, targetId);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Cannot cast with only 4 mana and no Wizard")
        void cannotCastWithInsufficientManaNoWizard() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
            harness.setHand(player1, List.of(new AcademyJourneymage()));
            harness.addMana(player1, ManaColor.BLUE, 4);

            assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, targetId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        @DisplayName("Costs {3}{U} when controlling a Wizard")
        void reducedCostWithWizard() {
            // AetherAdept is a Human Wizard
            harness.addToBattlefield(player1, new AetherAdept());
            harness.addToBattlefield(player2, new GrizzlyBears());
            UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
            harness.setHand(player1, List.of(new AcademyJourneymage()));
            harness.addMana(player1, ManaColor.BLUE, 4);

            harness.castCreature(player1, 0, 0, targetId);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Cannot cast with only 3 mana even with a Wizard")
        void cannotCastWith3ManaEvenWithWizard() {
            harness.addToBattlefield(player1, new AetherAdept());
            harness.addToBattlefield(player2, new GrizzlyBears());
            UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
            harness.setHand(player1, List.of(new AcademyJourneymage()));
            harness.addMana(player1, ManaColor.BLUE, 3);

            assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, targetId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }
    }

    // ===== Helpers =====

    private void castJourneymage(com.github.laxika.magicalvibes.model.Player targetOwner, String targetName) {
        UUID targetId = harness.getPermanentId(targetOwner, targetName);
        harness.setHand(player1, List.of(new AcademyJourneymage()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        gs.playCard(gd, player1, 0, 0, targetId, null);
    }
}
