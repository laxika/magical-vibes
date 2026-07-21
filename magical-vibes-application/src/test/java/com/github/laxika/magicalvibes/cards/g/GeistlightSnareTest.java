package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BattlegroundGeist;
import com.github.laxika.magicalvibes.cards.i.IntangibleVirtue;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GeistlightSnareTest extends BaseCardTest {

    @Test
    @DisplayName("Counters spell when opponent has no mana to pay")
    void countersWhenOpponentCannotPay() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new GeistlightSnare()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
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

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Llanowar Elves"));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
    }

    @Nested
    @DisplayName("Cost reduction")
    class CostReduction {

        @Test
        @DisplayName("Costs full {2}{U} with neither Spirit nor enchantment")
        void fullCostWithoutDiscount() {
            LlanowarElves elves = new LlanowarElves();
            harness.setHand(player1, List.of(elves));
            harness.addMana(player1, ManaColor.GREEN, 1);

            harness.setHand(player2, List.of(new GeistlightSnare()));
            harness.addMana(player2, ManaColor.BLUE, 3);

            harness.castCreature(player1, 0);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, elves.getId());

            assertThat(gd.stack).hasSize(2);
            assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Cannot cast with only 2 mana and no discount")
        void cannotCastWithInsufficientManaNoDiscount() {
            LlanowarElves elves = new LlanowarElves();
            harness.setHand(player1, List.of(elves));
            harness.addMana(player1, ManaColor.GREEN, 1);

            harness.setHand(player2, List.of(new GeistlightSnare()));
            harness.addMana(player2, ManaColor.BLUE, 2);

            harness.castCreature(player1, 0);
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
            harness.setHand(player1, List.of(elves));
            harness.addMana(player1, ManaColor.GREEN, 1);

            harness.setHand(player2, List.of(new GeistlightSnare()));
            harness.addMana(player2, ManaColor.BLUE, 2);

            harness.castCreature(player1, 0);
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
            harness.setHand(player1, List.of(elves));
            harness.addMana(player1, ManaColor.GREEN, 1);

            harness.setHand(player2, List.of(new GeistlightSnare()));
            harness.addMana(player2, ManaColor.BLUE, 2);

            harness.castCreature(player1, 0);
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
            harness.setHand(player1, List.of(elves));
            harness.addMana(player1, ManaColor.GREEN, 1);

            harness.setHand(player2, List.of(new GeistlightSnare()));
            harness.addMana(player2, ManaColor.BLUE, 1);

            harness.castCreature(player1, 0);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, elves.getId());

            assertThat(gd.stack).hasSize(2);
            assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
        }
    }
}
