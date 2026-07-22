package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BedlamRevelerTest extends BaseCardTest {

    @Nested
    @DisplayName("Cost reduction")
    class CostReduction {

        @Test
        @DisplayName("Can cast for full cost {6}{R}{R} with empty graveyard")
        void canCastForFullCost() {
            harness.setHand(player1, List.of(new BedlamReveler()));
            harness.addMana(player1, ManaColor.RED, 8);

            harness.castCreature(player1, 0);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Cost is reduced by 1 for each instant and sorcery card in graveyard")
        void costReducedByInstantAndSorceryCards() {
            harness.setGraveyard(player1, List.of(new Shock(), new LavaAxe(), new Shock()));
            harness.setHand(player1, List.of(new BedlamReveler()));
            // 3 instant/sorcery = reduce by 3 → {3}{R}{R} = 5 mana
            harness.addMana(player1, ManaColor.RED, 5);

            harness.castCreature(player1, 0);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Non-instant/sorcery cards in graveyard do not reduce the cost")
        void nonInstantSorceryCardsDoNotReduceCost() {
            harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
            harness.setHand(player1, List.of(new BedlamReveler()));
            harness.addMana(player1, ManaColor.RED, 7);

            assertThatThrownBy(() -> harness.castCreature(player1, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        @DisplayName("Cost cannot be reduced below the colored mana requirement {R}{R}")
        void costCannotGoBelowColoredMana() {
            harness.setGraveyard(player1, List.of(
                    new Shock(), new Shock(), new Shock(),
                    new Shock(), new Shock(), new Shock(),
                    new Shock(), new Shock(), new Shock()));
            harness.setHand(player1, List.of(new BedlamReveler()));
            harness.addMana(player1, ManaColor.RED, 2);

            harness.castCreature(player1, 0);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("ETB")
    class Etb {

        @Test
        @DisplayName("Enters: discard hand, then draw three cards")
        void discardsHandThenDrawsThree() {
            harness.setLibrary(player1, List.of(new Island(), new Island(), new Island(), new Island()));
            harness.setHand(player1, List.of(new BedlamReveler(), new GrizzlyBears(), new Shock()));
            harness.addMana(player1, ManaColor.RED, 8);

            harness.castCreature(player1, 0);
            harness.passBothPriorities(); // resolve creature
            harness.passBothPriorities(); // resolve ETB

            assertThat(gd.playerHands.get(player1.getId()))
                    .hasSize(3)
                    .allMatch(c -> c.getName().equals("Island"));
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .extracting(c -> c.getName())
                    .containsExactlyInAnyOrder("Grizzly Bears", "Shock");
            harness.assertOnBattlefield(player1, "Bedlam Reveler");
        }

        @Test
        @DisplayName("Enters with empty hand: still draws three")
        void emptyHandStillDrawsThree() {
            harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
            harness.setHand(player1, List.of(new BedlamReveler()));
            harness.addMana(player1, ManaColor.RED, 8);

            harness.castCreature(player1, 0);
            harness.passBothPriorities();
            harness.passBothPriorities();

            assertThat(gd.playerHands.get(player1.getId()))
                    .hasSize(3)
                    .allMatch(c -> c.getName().equals("Island"));
            assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        }
    }

    @Nested
    @DisplayName("Prowess")
    class Prowess {

        private Permanent addReveler() {
            harness.addToBattlefield(player1, new BedlamReveler());
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();
            return gd.playerBattlefields.get(player1.getId()).getFirst();
        }

        @Test
        @DisplayName("Casting a noncreature spell gives +1/+1 until end of turn")
        void noncreatureSpellPumps() {
            Permanent reveler = addReveler();

            harness.setHand(player1, List.of(new Shock()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities(); // resolve Shock
            harness.passBothPriorities(); // resolve prowess

            assertThat(gqs.getEffectivePower(gd, reveler)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, reveler)).isEqualTo(5);
        }

        @Test
        @DisplayName("Casting a creature spell does not trigger prowess")
        void creatureSpellDoesNotPump() {
            Permanent reveler = addReveler();

            harness.setHand(player1, List.of(new GrizzlyBears()));
            harness.addMana(player1, ManaColor.GREEN, 2);

            harness.castCreature(player1, 0);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
            assertThat(gqs.getEffectivePower(gd, reveler)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, reveler)).isEqualTo(4);
        }
    }
}
