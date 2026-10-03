package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BlackPoplarShaman;
import com.github.laxika.magicalvibes.cards.f.FlamekinBladewhirl;
import com.github.laxika.magicalvibes.cards.k.KithkinDaggerdare;
import com.github.laxika.magicalvibes.cards.w.WingsOfVelisVel;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConsumingBonfire.class, BlackPoplarShaman.class, FlamekinBladewhirl.class,
        KithkinDaggerdare.class, WoodlandChangeling.class, CloudcrownOak.class, WingsOfVelisVel.class})
class ConsumingBonfireTest extends BaseCardTest {

    @Nested
    @DisplayName("Mode 1: 4 damage to target non-Elemental creature")
    @CardUsed({ConsumingBonfire.class, KithkinDaggerdare.class, FlamekinBladewhirl.class,
            WoodlandChangeling.class, CloudcrownOak.class, WingsOfVelisVel.class})
    class NonElementalMode {

        @Test
        void firstModeDoesNotResolveWhenTargetBecomesElemental() {
            var target = harness.addToBattlefieldAndReturn(player2, new KithkinDaggerdare());
            harness.setHand(player1, List.of(new ConsumingBonfire()));
            harness.setHand(player2, List.of(new WingsOfVelisVel()));
            harness.addMana(player1, ManaColor.RED, 5);
            harness.addMana(player2, ManaColor.BLUE, 2);

            harness.castSorcery(player1, 0, 0, target.getId());
            harness.castInstant(player2, 0, target.getId());
            harness.passBothPriorities();
            harness.passBothPriorities();

            harness.assertOnBattlefield(player2, "Kithkin Daggerdare");
            assertThat(target.getMarkedDamage()).isZero();
            harness.assertInGraveyard(player1, "Consuming Bonfire");
        }

        @Test
        void dealsExactlyFourDamageToTreefolkWithoutSelectingSecondMode() {
            var target = harness.addToBattlefieldAndReturn(player2, new CloudcrownOak());
            target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
            harness.setHand(player1, List.of(new ConsumingBonfire()));
            harness.addMana(player1, ManaColor.RED, 5);

            harness.castSorcery(player1, 0, 0, target.getId());
            harness.passBothPriorities();

            harness.assertOnBattlefield(player2, "Cloudcrown Oak");
            assertThat(target.getMarkedDamage()).isEqualTo(4);
            harness.assertInGraveyard(player1, "Consuming Bonfire");
        }

        @Test
        void cannotTargetChangelingInFirstMode() {
            var target = harness.addToBattlefieldAndReturn(player2, new WoodlandChangeling());
            harness.setHand(player1, List.of(new ConsumingBonfire()));
            harness.addMana(player1, ManaColor.RED, 5);

            assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, target.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void canTargetOwnNonElementalCreature() {
            var target = harness.addToBattlefieldAndReturn(player1, new KithkinDaggerdare());
            harness.setHand(player1, List.of(new ConsumingBonfire()));
            harness.addMana(player1, ManaColor.RED, 5);

            harness.castSorcery(player1, 0, 0, target.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player1, "Kithkin Daggerdare");
            harness.assertInGraveyard(player1, "Kithkin Daggerdare");
        }

        @Test
        @DisplayName("Deals 4 damage to a non-Elemental creature")
        void deals4ToNonElemental() {
            var target = harness.addToBattlefieldAndReturn(player2, new KithkinDaggerdare());

            harness.setHand(player1, List.of(new ConsumingBonfire()));
            harness.addMana(player1, ManaColor.RED, 5);

            harness.castSorcery(player1, 0, 0, target.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Kithkin Daggerdare");
            harness.assertInGraveyard(player2, "Kithkin Daggerdare");
        }

        @Test
        @DisplayName("Cannot target an Elemental creature")
        void cannotTargetElemental() {
            var target = harness.addToBattlefieldAndReturn(player2, new FlamekinBladewhirl());

            harness.setHand(player1, List.of(new ConsumingBonfire()));
            harness.addMana(player1, ManaColor.RED, 5);

            assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, target.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 2: 7 damage to target Treefolk creature")
    @CardUsed({ConsumingBonfire.class, BlackPoplarShaman.class, KithkinDaggerdare.class,
            WoodlandChangeling.class, CloudcrownOak.class})
    class TreefolkMode {

        @Test
        void dealsExactlySevenDamage() {
            var target = harness.addToBattlefieldAndReturn(player2, new CloudcrownOak());
            target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
            harness.setHand(player1, List.of(new ConsumingBonfire()));
            harness.addMana(player1, ManaColor.RED, 5);

            harness.castSorcery(player1, 0, 1, target.getId());
            harness.passBothPriorities();

            harness.assertOnBattlefield(player2, "Cloudcrown Oak");
            assertThat(target.getMarkedDamage()).isEqualTo(7);
            harness.assertInGraveyard(player1, "Consuming Bonfire");
        }

        @Test
        void canTargetChangelingDespiteAlsoBeingElemental() {
            var target = harness.addToBattlefieldAndReturn(player2, new WoodlandChangeling());
            harness.setHand(player1, List.of(new ConsumingBonfire()));
            harness.addMana(player1, ManaColor.RED, 5);

            harness.castSorcery(player1, 0, 1, target.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Woodland Changeling");
            harness.assertInGraveyard(player2, "Woodland Changeling");
        }

        @Test
        @DisplayName("Deals 7 damage to a Treefolk creature")
        void deals7ToTreefolk() {
            var target = harness.addToBattlefieldAndReturn(player2, new BlackPoplarShaman());

            harness.setHand(player1, List.of(new ConsumingBonfire()));
            harness.addMana(player1, ManaColor.RED, 5);

            harness.castSorcery(player1, 0, 1, target.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Black Poplar Shaman");
            harness.assertInGraveyard(player2, "Black Poplar Shaman");
        }

        @Test
        @DisplayName("Cannot target a non-Treefolk creature")
        void cannotTargetNonTreefolk() {
            var target = harness.addToBattlefieldAndReturn(player2, new KithkinDaggerdare());

            harness.setHand(player1, List.of(new ConsumingBonfire()));
            harness.addMana(player1, ManaColor.RED, 5);

            assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, target.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
