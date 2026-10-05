package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.cards.c.CarnageTyrant;
import com.github.laxika.magicalvibes.testutil.CardUsed;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.d.DeeprootWarrior;
import com.github.laxika.magicalvibes.cards.h.HeadstrongBrute;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LookoutsDispersal.class, DeeprootWarrior.class, HeadstrongBrute.class, CarnageTyrant.class})
class LookoutsDispersalTest extends BaseCardTest {

    @Test
    @DisplayName("Counters spell when opponent has no mana to pay")
    void countersWhenOpponentCannotPay() {
        DeeprootWarrior warrior = new DeeprootWarrior();
        harness.setHand(player1, List.of(warrior));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new LookoutsDispersal()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, warrior.getId());

        harness.assertInGraveyard(player1, "Deeproot Warrior");
        harness.assertNotOnBattlefield(player1, "Deeproot Warrior");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Spell is not countered when opponent pays {4}")
    void spellNotCounteredWhenOpponentPays() {
        DeeprootWarrior warrior = new DeeprootWarrior();
        harness.setHand(player1, List.of(warrior));
        harness.addMana(player1, ManaColor.GREEN, 6); // 2 to cast, 4 to pay

        harness.setHand(player2, List.of(new LookoutsDispersal()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, warrior.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());

        // Player1 pays {4}
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotInGraveyard(player1, "Deeproot Warrior");

        // Resolve the warrior spell
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Deeproot Warrior");
    }

    @Test
    @DisplayName("Spell is countered when opponent declines to pay")
    void spellCounteredWhenOpponentDeclines() {
        DeeprootWarrior warrior = new DeeprootWarrior();
        harness.setHand(player1, List.of(warrior));
        harness.addMana(player1, ManaColor.GREEN, 6); // 2 to cast, 4 available

        harness.setHand(player2, List.of(new LookoutsDispersal()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, warrior.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        // Player1 declines to pay
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Deeproot Warrior");
        harness.assertNotOnBattlefield(player1, "Deeproot Warrior");
    }

    @Test
    @DisplayName("Opponent's mana pool is reduced after paying {4}")
    void manaPoolReducedAfterPaying() {
        DeeprootWarrior warrior = new DeeprootWarrior();
        harness.setHand(player1, List.of(warrior));
        harness.addMana(player1, ManaColor.GREEN, 6); // 2 to cast, 4 to pay

        harness.setHand(player2, List.of(new LookoutsDispersal()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, warrior.getId());

        int manaBefore = gd.playerManaPools.get(player1.getId()).getTotal();
        assertThat(manaBefore).isEqualTo(4); // 6 added - 2 to cast

        harness.handleMayAbilityChosen(player1, true);

        int manaAfter = gd.playerManaPools.get(player1.getId()).getTotal();
        assertThat(manaAfter).isEqualTo(0); // 4 - 4 paid
    }

    @Nested
    @DisplayName("Cost reduction")
    @CardUsed({LookoutsDispersal.class, DeeprootWarrior.class, HeadstrongBrute.class})
    class CostReduction {

        @Test
        void opponentsPirateDoesNotReduceCost() {
            harness.addToBattlefield(player1, new HeadstrongBrute());
            HeadstrongBrute spell = new HeadstrongBrute();
            harness.setHand(player1, List.of(spell));
            harness.addMana(player1, ManaColor.RED, 3);
            harness.setHand(player2, List.of(new LookoutsDispersal()));
            harness.addMana(player2, ManaColor.BLUE, 2);
            harness.castCreature(player1, 0);
            harness.passPriority(player1);

            assertThatThrownBy(() -> harness.castInstant(player2, 0, spell.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        void multiplePiratesReduceCostOnlyOnce() {
            harness.addToBattlefield(player2, new HeadstrongBrute());
            harness.addToBattlefield(player2, new HeadstrongBrute());
            HeadstrongBrute spell = new HeadstrongBrute();
            harness.setHand(player1, List.of(spell));
            harness.addMana(player1, ManaColor.RED, 3);
            harness.setHand(player2, List.of(new LookoutsDispersal()));
            harness.addMana(player2, ManaColor.BLUE, 3);
            harness.castCreature(player1, 0);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, spell.getId());

            assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(1);
        }

        @Test
        @DisplayName("Costs full {2}{U} without a Pirate on the battlefield")
        void fullCostWithoutPirate() {
            DeeprootWarrior warrior = new DeeprootWarrior();
            harness.setHand(player1, List.of(warrior));
            harness.addMana(player1, ManaColor.GREEN, 2);

            harness.setHand(player2, List.of(new LookoutsDispersal()));
            harness.addMana(player2, ManaColor.BLUE, 3);

            harness.castCreature(player1, 0);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, warrior.getId());

            assertThat(gd.stack).hasSize(2);
            assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Cannot cast with only 2 mana and no Pirate")
        void cannotCastWithInsufficientManaNoPirate() {
            DeeprootWarrior warrior = new DeeprootWarrior();
            harness.setHand(player1, List.of(warrior));
            harness.addMana(player1, ManaColor.GREEN, 2);

            harness.setHand(player2, List.of(new LookoutsDispersal()));
            harness.addMana(player2, ManaColor.BLUE, 2);

            harness.castCreature(player1, 0);
            harness.passPriority(player1);

            assertThatThrownBy(() -> harness.castInstant(player2, 0, warrior.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        @DisplayName("Costs {1}{U} when controlling a Pirate")
        void reducedCostWithPirate() {
            // HeadstrongBrute is an Orc Pirate
            harness.addToBattlefield(player2, new HeadstrongBrute());

            DeeprootWarrior warrior = new DeeprootWarrior();
            harness.setHand(player1, List.of(warrior));
            harness.addMana(player1, ManaColor.GREEN, 2);

            harness.setHand(player2, List.of(new LookoutsDispersal()));
            harness.addMana(player2, ManaColor.BLUE, 2);

            harness.castCreature(player1, 0);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, warrior.getId());

            assertThat(gd.stack).hasSize(2);
            assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Cannot cast with only 1 mana even with a Pirate")
        void cannotCastWith1ManaEvenWithPirate() {
            harness.addToBattlefield(player2, new HeadstrongBrute());

            DeeprootWarrior warrior = new DeeprootWarrior();
            harness.setHand(player1, List.of(warrior));
            harness.addMana(player1, ManaColor.GREEN, 2);

            harness.setHand(player2, List.of(new LookoutsDispersal()));
            harness.addMana(player2, ManaColor.BLUE, 1);

            harness.castCreature(player1, 0);
            harness.passPriority(player1);

            assertThatThrownBy(() -> harness.castInstant(player2, 0, warrior.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }
    }

    @Test
    @CardUsed({LookoutsDispersal.class, CarnageTyrant.class})
    void controllerCanPayEvenWhenSpellCannotBeCountered() {
        CarnageTyrant tyrant = new CarnageTyrant();
        harness.setHand(player1, List.of(tyrant));
        harness.addMana(player1, ManaColor.GREEN, 10);
        harness.setHand(player2, List.of(new LookoutsDispersal()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, tyrant.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Carnage Tyrant");
    }

    @Test
    @DisplayName("Fizzles if target spell is no longer on the stack")
    void fizzlesIfTargetSpellRemoved() {
        DeeprootWarrior warrior = new DeeprootWarrior();
        harness.setHand(player1, List.of(warrior));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new LookoutsDispersal()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, warrior.getId());

        gd.stack.removeIf(se -> se.getCard().getName().equals("Deeproot Warrior"));

        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player2, "Lookout's Dispersal");
    }

    @Test
    @DisplayName("Goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        DeeprootWarrior warrior = new DeeprootWarrior();
        harness.setHand(player1, List.of(warrior));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new LookoutsDispersal()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, warrior.getId());

        harness.assertInGraveyard(player2, "Lookout's Dispersal");
        assertThat(gd.stack).isEmpty();
    }
}
