package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.v.VodalianArcanist;
import com.github.laxika.magicalvibes.cards.b.BalothGorger;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WizardsRetort.class, BalothGorger.class, VodalianArcanist.class})
class WizardsRetortTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a creature spell")
    void countersCreatureSpell() {
        BalothGorger bears = new BalothGorger();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new WizardsRetort()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertInGraveyard(player1, "Baloth Gorger");
        harness.assertNotOnBattlefield(player1, "Baloth Gorger");
    }

    @Test
    @DisplayName("Goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        BalothGorger bears = new BalothGorger();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new WizardsRetort()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertInGraveyard(player2, "Wizard's Retort");
        assertThat(gd.stack).isEmpty();
    }

    @Nested
    @DisplayName("Cost reduction")
    @CardUsed({WizardsRetort.class, BalothGorger.class, VodalianArcanist.class})
    class CostReduction {

        @Test
        @DisplayName("Costs full {1}{U}{U} without a Wizard on the battlefield")
        void fullCostWithoutWizard() {
            BalothGorger bears = new BalothGorger();
            harness.setHand(player1, List.of(bears));
            harness.addMana(player1, ManaColor.GREEN, 4);

            harness.setHand(player2, List.of(new WizardsRetort()));
            harness.addMana(player2, ManaColor.BLUE, 3);

            harness.castCreature(player1, 0);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, bears.getId());

            assertThat(gd.stack).hasSize(2);
            assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Cannot cast with only 2 mana and no Wizard")
        void cannotCastWithInsufficientManaNoWizard() {
            BalothGorger bears = new BalothGorger();
            harness.setHand(player1, List.of(bears));
            harness.addMana(player1, ManaColor.GREEN, 4);

            harness.setHand(player2, List.of(new WizardsRetort()));
            harness.addMana(player2, ManaColor.BLUE, 2);

            harness.castCreature(player1, 0);
            harness.passPriority(player1);

            assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        @DisplayName("Costs {U}{U} when controlling a Wizard")
        void reducedCostWithWizard() {
            // Vodalian Arcanist is a Merfolk Wizard
            harness.addToBattlefield(player2, new VodalianArcanist());

            BalothGorger bears = new BalothGorger();
            harness.setHand(player1, List.of(bears));
            harness.addMana(player1, ManaColor.GREEN, 4);

            harness.setHand(player2, List.of(new WizardsRetort()));
            harness.addMana(player2, ManaColor.BLUE, 2);

            harness.castCreature(player1, 0);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, bears.getId());

            assertThat(gd.stack).hasSize(2);
            assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Cannot cast with only 1 mana even with a Wizard")
        void cannotCastWith1ManaEvenWithWizard() {
            harness.addToBattlefield(player2, new VodalianArcanist());

            BalothGorger bears = new BalothGorger();
            harness.setHand(player1, List.of(bears));
            harness.addMana(player1, ManaColor.GREEN, 4);

            harness.setHand(player2, List.of(new WizardsRetort()));
            harness.addMana(player2, ManaColor.BLUE, 1);

            harness.castCreature(player1, 0);
            harness.passPriority(player1);

            assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }
    }

    @Test
    @DisplayName("Fizzles if target spell is no longer on the stack")
    void fizzlesIfTargetSpellRemoved() {
        BalothGorger bears = new BalothGorger();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new WizardsRetort()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());

        gd.stack.removeIf(se -> se.getCard().getName().equals("Baloth Gorger"));

        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player2, "Wizard's Retort");
    }

    @Test
    void opponentsWizardDoesNotReduceCost() {
        harness.addToBattlefield(player1, new VodalianArcanist());
        BalothGorger target = new BalothGorger();
        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player2, List.of(new WizardsRetort()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void nonWizardDoesNotReduceCost() {
        harness.addToBattlefield(player2, new BalothGorger());
        BalothGorger target = new BalothGorger();
        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player2, List.of(new WizardsRetort()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void multipleWizardsStillRequireTwoBlueMana() {
        harness.addToBattlefield(player2, new VodalianArcanist());
        harness.addToBattlefield(player2, new VodalianArcanist());
        BalothGorger target = new BalothGorger();
        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player2, List.of(new WizardsRetort()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());

        harness.assertInGraveyard(player1, "Baloth Gorger");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    void countersInstantSpell() {
        BalothGorger creature = new BalothGorger();
        WizardsRetort firstRetort = new WizardsRetort();
        harness.setHand(player1, List.of(creature, new WizardsRetort()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setHand(player2, List.of(firstRetort));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, creature.getId());
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, firstRetort.getId());

        harness.assertInGraveyard(player2, "Wizard's Retort");
        harness.assertInGraveyard(player1, "Wizard's Retort");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Baloth Gorger");
        harness.assertNotInGraveyard(player1, "Baloth Gorger");
    }
}
