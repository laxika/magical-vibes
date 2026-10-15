package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Stasis;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
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

@CardUsed({RedElementalBlast.class, WaterElemental.class, GrizzlyBears.class, Stasis.class, Unsummon.class})
class RedElementalBlastTest extends BaseCardTest {

    @Nested
    @CardUsed({RedElementalBlast.class, WaterElemental.class, GrizzlyBears.class})
    @DisplayName("Mode 0: Counter target blue spell")
    class CounterBlueSpellMode {

        @Test
        @DisplayName("Counters a blue spell")
        void countersBlueSpell() {
            WaterElemental waterElemental = new WaterElemental();
            harness.setHand(player1, List.of(new RedElementalBlast()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.forceActivePlayer(player2);
            harness.castFromHand(player2, waterElemental, "{3}{U}{U}");
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
            harness.setHand(player1, List.of(new RedElementalBlast()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.forceActivePlayer(player2);
            harness.castFromHand(player2, bears, "{1}{G}");
            harness.passPriority(player2);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, bears.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @CardUsed({RedElementalBlast.class, WaterElemental.class, GrizzlyBears.class, Stasis.class, Unsummon.class})
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
            harness.setHand(player1, List.of(new RedElementalBlast()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.forceActivePlayer(player2);
            harness.castFromHand(player2, waterElemental, "{3}{U}{U}");
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

        @Test
        @CardUsed({RedElementalBlast.class, Stasis.class})
        @DisplayName("Destroys a blue noncreature permanent")
        void destroysBlueNoncreaturePermanent() {
            var stasis = harness.addToBattlefieldAndReturn(player2, new Stasis());
            harness.setHand(player1, List.of(new RedElementalBlast()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castInstant(player1, 0, 1, stasis.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Stasis");
            harness.assertInGraveyard(player2, "Stasis");
            harness.assertInGraveyard(player1, "Red Elemental Blast");
        }

        @Test
        @CardUsed({RedElementalBlast.class, WaterElemental.class, Unsummon.class})
        @DisplayName("Does not destroy a creature returned to hand before resolution")
        void targetReturnedToHandBeforeResolution() {
            var waterElemental = harness.addToBattlefieldAndReturn(player2, new WaterElemental());
            harness.setHand(player1, List.of(new RedElementalBlast()));
            harness.addMana(player1, ManaColor.RED, 1);
            harness.setHand(player2, List.of(new Unsummon()));
            harness.addMana(player2, ManaColor.BLUE, 1);

            harness.castInstant(player1, 0, 1, waterElemental.getId());
            harness.passPriority(player1);
            harness.castInstant(player2, 0, waterElemental.getId());
            harness.passBothPriorities();
            harness.passBothPriorities();

            harness.assertInHand(player2, "Water Elemental");
            harness.assertNotInGraveyard(player2, "Water Elemental");
            harness.assertInGraveyard(player1, "Red Elemental Blast");
            assertThat(gd.stack).isEmpty();
        }
    }
}
