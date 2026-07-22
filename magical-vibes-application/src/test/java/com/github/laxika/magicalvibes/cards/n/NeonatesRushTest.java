package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BaronyVampire;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    class Resolution {

        @Test
        @DisplayName("Deals 1 damage to target creature and 1 to its controller, then draws a card")
        void dealsDamageAndDraws() {
            harness.addToBattlefield(player2, createCreature("Large Beast", 3, 3));
            harness.setHand(player1, List.of(new NeonatesRush()));
            harness.addMana(player1, ManaColor.RED, 3);
            harness.setLife(player2, 20);

            UUID targetId = harness.getPermanentId(player2, "Large Beast");
            harness.castInstant(player1, 0, targetId);
            harness.passBothPriorities();

            harness.assertOnBattlefield(player2, "Large Beast");
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
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
            harness.castInstant(player1, 0, targetId);
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Fragile");
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
            assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Cost reduction")
    class CostReduction {

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
            harness.castInstant(player1, 0, targetId);
            harness.passBothPriorities();

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
            assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        }
    }
}
