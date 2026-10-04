package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BattlegroundGeist;
import com.github.laxika.magicalvibes.cards.i.IntangibleVirtue;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GeistlightSnare.class, BattlegroundGeist.class, IntangibleVirtue.class, LlanowarElves.class})
class GeistlightSnareTest extends BaseCardTest {

    @Test
    @DisplayName("Counters spell when opponent has no mana to pay")
    void countersWhenOpponentCannotPay() {
        LlanowarElves elves = new LlanowarElves();

        harness.setHand(player2, List.of(new GeistlightSnare()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castFromHand(player1, elves, "{G}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Spell is not countered when opponent pays {3}")
    void spellNotCounteredWhenOpponentPays() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new GeistlightSnare()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotInGraveyard(player1, "Llanowar Elves");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
    }


    @Test
    @DisplayName("Counters spell when its controller declines an affordable payment")
    void countersWhenControllerDeclinesPayment() {
        IntangibleVirtue virtue = new IntangibleVirtue();
        harness.setHand(player1, List.of(virtue));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.setHand(player2, List.of(new GeistlightSnare()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, virtue.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Intangible Virtue");
        harness.assertNotOnBattlefield(player1, "Intangible Virtue");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Nested
    @CardUsed({GeistlightSnare.class, BattlegroundGeist.class, IntangibleVirtue.class, LlanowarElves.class})
    @DisplayName("Cost reduction")
    class CostReduction {

        @Test
        @DisplayName("Costs full {2}{U} with neither Spirit nor enchantment")
        void fullCostWithoutDiscount() {
            LlanowarElves elves = new LlanowarElves();

            harness.setHand(player2, List.of(new GeistlightSnare()));
            harness.addMana(player2, ManaColor.BLUE, 3);

            harness.castFromHand(player1, elves, "{G}");
            harness.passPriority(player1);
            harness.castInstant(player2, 0, elves.getId());

            assertThat(gd.stack).hasSize(2);
            assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Cannot cast with only 2 mana and no discount")
        void cannotCastWithInsufficientManaNoDiscount() {
            LlanowarElves elves = new LlanowarElves();

            harness.setHand(player2, List.of(new GeistlightSnare()));
            harness.addMana(player2, ManaColor.BLUE, 2);

            harness.castFromHand(player1, elves, "{G}");
            harness.passPriority(player1);

            assertThatThrownBy(() -> harness.castInstant(player2, 0, elves.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        @DisplayName("Costs {1}{U} when controlling a Spirit")
        void reducedCostWithSpirit() {
            harness.addToBattlefield(player2, new BattlegroundGeist());

            LlanowarElves elves = new LlanowarElves();

            harness.setHand(player2, List.of(new GeistlightSnare()));
            harness.addMana(player2, ManaColor.BLUE, 2);

            harness.castFromHand(player1, elves, "{G}");
            harness.passPriority(player1);
            harness.castInstant(player2, 0, elves.getId());

            assertThat(gd.stack).hasSize(2);
            assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Costs {1}{U} when controlling an enchantment")
        void reducedCostWithEnchantment() {
            harness.addToBattlefield(player2, new IntangibleVirtue());

            LlanowarElves elves = new LlanowarElves();

            harness.setHand(player2, List.of(new GeistlightSnare()));
            harness.addMana(player2, ManaColor.BLUE, 2);

            harness.castFromHand(player1, elves, "{G}");
            harness.passPriority(player1);
            harness.castInstant(player2, 0, elves.getId());

            assertThat(gd.stack).hasSize(2);
            assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Costs {U} when controlling both a Spirit and an enchantment")
        void reducedCostWithBoth() {
            harness.addToBattlefield(player2, new BattlegroundGeist());
            harness.addToBattlefield(player2, new IntangibleVirtue());

            LlanowarElves elves = new LlanowarElves();

            harness.setHand(player2, List.of(new GeistlightSnare()));
            harness.addMana(player2, ManaColor.BLUE, 1);

            harness.castFromHand(player1, elves, "{G}");
            harness.passPriority(player1);
            harness.castInstant(player2, 0, elves.getId());

            assertThat(gd.stack).hasSize(2);
            assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Opponent's Spirit and enchantment do not reduce the cost")
        void opponentsPermanentsDoNotReduceCost() {
            harness.addToBattlefield(player1, new BattlegroundGeist());
            harness.addToBattlefield(player1, new IntangibleVirtue());
            IntangibleVirtue spell = new IntangibleVirtue();
            harness.setHand(player1, List.of(spell));
            harness.addMana(player1, ManaColor.WHITE, 2);
            harness.setHand(player2, List.of(new GeistlightSnare()));
            harness.addMana(player2, ManaColor.BLUE, 3);

            harness.castEnchantment(player1, 0);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, spell.getId());

            assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
            harness.passBothPriorities();
            harness.assertInGraveyard(player1, "Intangible Virtue");
        }

        @Test
        @DisplayName("Multiple Spirits give only one generic mana reduction")
        void multipleSpiritsDoNotMultiplyReduction() {
            harness.addToBattlefield(player2, new BattlegroundGeist());
            harness.addToBattlefield(player2, new BattlegroundGeist());
            IntangibleVirtue spell = new IntangibleVirtue();
            harness.setHand(player1, List.of(spell));
            harness.addMana(player1, ManaColor.WHITE, 2);
            harness.setHand(player2, List.of(new GeistlightSnare()));
            harness.addMana(player2, ManaColor.BLUE, 2);

            harness.castEnchantment(player1, 0);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, spell.getId());

            assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
            harness.passBothPriorities();
            harness.assertInGraveyard(player1, "Intangible Virtue");
        }

        @Test
        @DisplayName("Multiple enchantments give only one generic mana reduction")
        void multipleEnchantmentsDoNotMultiplyReduction() {
            harness.addToBattlefield(player2, new IntangibleVirtue());
            harness.addToBattlefield(player2, new IntangibleVirtue());
            IntangibleVirtue spell = new IntangibleVirtue();
            harness.setHand(player1, List.of(spell));
            harness.addMana(player1, ManaColor.WHITE, 2);
            harness.setHand(player2, List.of(new GeistlightSnare()));
            harness.addMana(player2, ManaColor.BLUE, 2);

            harness.castEnchantment(player1, 0);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, spell.getId());

            assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
            harness.passBothPriorities();
            harness.assertInGraveyard(player1, "Intangible Virtue");
        }

        @Test
        @DisplayName("Both reductions still require blue mana")
        void reductionsDoNotRemoveBlueRequirement() {
            harness.addToBattlefield(player2, new BattlegroundGeist());
            harness.addToBattlefield(player2, new IntangibleVirtue());
            IntangibleVirtue spell = new IntangibleVirtue();
            harness.setHand(player1, List.of(spell));
            harness.addMana(player1, ManaColor.WHITE, 2);
            harness.setHand(player2, List.of(new GeistlightSnare()));
            harness.addMana(player2, ManaColor.WHITE, 1);

            harness.castEnchantment(player1, 0);
            harness.passPriority(player1);

            assertThatThrownBy(() -> harness.castInstant(player2, 0, spell.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }
    }
}
