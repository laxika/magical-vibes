package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BaronyVampire;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({NeonatesRush.class, GrizzlyBears.class, BaronyVampire.class})
class NeonatesRushTest extends BaseCardTest {

    private static Card createCreature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(CardColor.GREEN);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    @Nested
    @DisplayName("Resolution")
    @CardUsed({NeonatesRush.class, GrizzlyBears.class})
    class Resolution {

        @Test
        void canDamageOwnCreatureAndItsController() {
            Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new NeonatesRush()));
            harness.setLibrary(player1, List.of(new GrizzlyBears()));
            harness.setLife(player1, 20);
            harness.setLife(player2, 20);
            harness.addMana(player1, ManaColor.RED, 3);

            harness.castAndResolveInstant(player1, 0, target.getId());

            assertThat(target.getMarkedDamage()).isEqualTo(1);
            harness.assertOnBattlefield(player1, "Grizzly Bears");
            harness.assertLife(player1, 19);
            harness.assertLife(player2, 20);
            harness.assertInHand(player1, "Grizzly Bears");
            harness.assertInGraveyard(player1, "Neonate's Rush");
        }

        @Test
        void doesNotDamageControllerOrDrawWhenTargetLeavesBattlefield() {
            Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new NeonatesRush()));
            harness.setLibrary(player1, List.of(new GrizzlyBears()));
            harness.setLife(player2, 20);
            harness.addMana(player1, ManaColor.RED, 3);

            harness.castInstant(player1, 0, target.getId());
            gd.playerBattlefields.get(player2.getId()).remove(target);
            harness.passBothPriorities();

            harness.assertLife(player2, 20);
            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
            assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
            harness.assertInGraveyard(player1, "Neonate's Rush");
        }

        @Test
        @DisplayName("Deals 1 damage to target creature and 1 to its controller, then draws a card")
        void dealsDamageAndDraws() {
            harness.addToBattlefield(player2, createCreature("Large Beast", 3, 3));
            harness.setHand(player1, List.of(new NeonatesRush()));
            harness.addMana(player1, ManaColor.RED, 3);
            harness.setLife(player2, 20);

            UUID targetId = harness.getPermanentId(player2, "Large Beast");
            harness.castAndResolveInstant(player1, 0, targetId);

            harness.assertOnBattlefield(player2, "Large Beast");
            harness.assertLife(player2, 19);
            assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
            harness.assertInGraveyard(player1, "Neonate's Rush");
        }

        @Test
        @DisplayName("Controller damage still applies when the creature dies to the 1 damage")
        void controllerDamageWhenCreatureDies() {
            harness.addToBattlefield(player2, createCreature("Fragile", 1, 1));
            harness.setHand(player1, List.of(new NeonatesRush()));
            harness.addMana(player1, ManaColor.RED, 3);
            harness.setLife(player2, 20);

            UUID targetId = harness.getPermanentId(player2, "Fragile");
            harness.castAndResolveInstant(player1, 0, targetId);

            harness.assertNotOnBattlefield(player2, "Fragile");
            harness.assertLife(player2, 19);
            assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Cost reduction")
    @CardUsed({NeonatesRush.class, GrizzlyBears.class, BaronyVampire.class})
    class CostReduction {

        @Test
        void opposingVampireDoesNotReduceCost() {
            Permanent target = harness.addToBattlefieldAndReturn(player2, new BaronyVampire());
            harness.setHand(player1, List.of(new NeonatesRush()));
            harness.addMana(player1, ManaColor.RED, 2);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        void multipleVampiresReduceCostOnlyOnce() {
            harness.addToBattlefield(player1, new BaronyVampire());
            harness.addToBattlefield(player1, new BaronyVampire());
            Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new NeonatesRush()));
            harness.addMana(player1, ManaColor.RED, 3);

            harness.castInstant(player1, 0, target.getId());

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        }

        @Test
        @DisplayName("Costs full {2}{R} without a Vampire")
        void fullCostWithoutVampire() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new NeonatesRush()));
            harness.addMana(player1, ManaColor.RED, 3);

            UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
            harness.castInstant(player1, 0, targetId);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Cannot cast with only {1}{R} and no Vampire")
        void cannotCastWithInsufficientManaNoVampire() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new NeonatesRush()));
            harness.addMana(player1, ManaColor.RED, 2);

            UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
            assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        @DisplayName("Costs {1}{R} when controlling a Vampire")
        void reducedCostWithVampire() {
            harness.addToBattlefield(player1, new BaronyVampire());
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new NeonatesRush()));
            harness.addMana(player1, ManaColor.RED, 2);

            UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
            harness.castInstant(player1, 0, targetId);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Resolves correctly when cast at reduced cost")
        void resolvesAtReducedCost() {
            harness.addToBattlefield(player1, new BaronyVampire());
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new NeonatesRush()));
            harness.addMana(player1, ManaColor.RED, 2);
            harness.setLife(player2, 20);

            UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
            harness.castAndResolveInstant(player1, 0, targetId);

            harness.assertLife(player2, 19);
            assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        }
    }
}
