package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WaterElemental;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RedElementalBlast.class, WaterElemental.class, GrizzlyBears.class})
class RedElementalBlastTest extends BaseCardTest {

    @Nested
    @DisplayName("Mode 0: Counter target blue spell")
    class CounterBlueSpellMode {

        @Test
        @DisplayName("Counters a blue spell")
        void countersBlueSpell() {
            WaterElemental waterElemental = new WaterElemental();
            harness.setHand(player2, List.of(waterElemental));
            harness.addMana(player2, ManaColor.BLUE, 5);
            harness.setHand(player1, List.of(new RedElementalBlast()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.forceActivePlayer(player2);
            harness.castCreature(player2, 0);
            harness.passPriority(player2);

            harness.castInstant(player1, 0, 0, waterElemental.getId());
            harness.passBothPriorities();

            assertThat(gd.stack).isEmpty();
            harness.assertInGraveyard(player2, "Water Elemental");
            harness.assertNotOnBattlefield(player2, "Water Elemental");
        }

        @Test
        @DisplayName("Cannot counter a blue permanent")
        void cannotCounterBluePermanent() {
            var waterElemental = harness.addToBattlefieldAndReturn(player2, new WaterElemental());
            harness.setHand(player1, List.of(new RedElementalBlast()));
            harness.addMana(player1, ManaColor.RED, 1);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, waterElemental.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Cannot counter a non-blue spell")
        void cannotCounterNonBlueSpell() {
            GrizzlyBears bears = new GrizzlyBears();
            harness.setHand(player2, List.of(bears));
            harness.addMana(player2, ManaColor.GREEN, 2);
            harness.setHand(player1, List.of(new RedElementalBlast()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.forceActivePlayer(player2);
            harness.castCreature(player2, 0);
            harness.passPriority(player2);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, bears.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 1: Destroy target blue permanent")
    class DestroyBluePermanentMode {

        @Test
        @DisplayName("Destroys a blue permanent")
        void destroysBluePermanent() {
            var waterElemental = harness.addToBattlefieldAndReturn(player2, new WaterElemental());
            harness.setHand(player1, List.of(new RedElementalBlast()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castInstant(player1, 0, 1, waterElemental.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Water Elemental");
            harness.assertInGraveyard(player2, "Water Elemental");
        }

        @Test
        @DisplayName("Cannot destroy a blue spell")
        void cannotDestroyBlueSpell() {
            WaterElemental waterElemental = new WaterElemental();
            harness.setHand(player2, List.of(waterElemental));
            harness.addMana(player2, ManaColor.BLUE, 5);
            harness.setHand(player1, List.of(new RedElementalBlast()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.forceActivePlayer(player2);
            harness.castCreature(player2, 0);
            harness.passPriority(player2);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, waterElemental.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Cannot destroy a non-blue permanent")
        void cannotDestroyNonBluePermanent() {
            var bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new RedElementalBlast()));
            harness.addMana(player1, ManaColor.RED, 1);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, bears.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
