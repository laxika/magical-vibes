package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MentalMisstep.class, GrizzlyBears.class, LlanowarElves.class, Shock.class})
class MentalMisstepTest extends BaseCardTest {

    @Test
    @DisplayName("Can target a creature spell with mana value 1")
    void canTargetManaValue1CreatureSpell() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new MentalMisstep()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        StackEntry misstepEntry = gd.stack.getLast();
        assertThat(misstepEntry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(misstepEntry.getCard().getName()).isEqualTo("Mental Misstep");
        assertThat(misstepEntry.getTargetId()).isEqualTo(elves.getId());
    }

    @Test
    @DisplayName("Can target an instant spell with mana value 1")
    void canTargetManaValue1InstantSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.setHand(player2, List.of(new MentalMisstep()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, shock.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        StackEntry misstepEntry = gd.stack.getLast();
        assertThat(misstepEntry.getCard().getName()).isEqualTo("Mental Misstep");
        assertThat(misstepEntry.getTargetId()).isEqualTo(shock.getId());
    }

    @Test
    @DisplayName("Cannot target a spell with mana value 2")
    void cannotTargetManaValue2Spell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new MentalMisstep()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Resolving counters a mana value 1 creature spell")
    void countersManaValue1CreatureSpell() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new MentalMisstep()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, elves.getId());

        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Mental Misstep goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new MentalMisstep()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, elves.getId());

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player2, "Mental Misstep");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can be cast by paying 2 life instead of blue mana")
    void canBeCastWithPhyrexianMana() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new MentalMisstep()));
        // No blue mana; pay with life.

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, elves.getId());

        // Countered spell goes to graveyard
        harness.assertInGraveyard(player1, "Llanowar Elves");
        // Player 2 paid 2 life
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Fizzles if target spell is no longer on the stack")
    void fizzlesIfTargetSpellRemoved() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new MentalMisstep()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId());

        GameData gd = harness.getGameData();
        gd.stack.removeIf(se -> se.getCard().getName().equals("Llanowar Elves"));

        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player2, "Mental Misstep");
    }

    @Test
    @DisplayName("A Mental Misstep paid for with life still has mana value one")
    void countersMentalMisstepPaidWithLife() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves, new MentalMisstep()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new MentalMisstep()));

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId());
        var opposingMisstepId = harness.getGameData().stack.getLast().getCard().getId();
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, opposingMisstepId);

        harness.assertInGraveyard(player1, "Mental Misstep");
        harness.assertInGraveyard(player2, "Mental Misstep");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
        assertThat(harness.getGameData().stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Cannot pay the Phyrexian cost with only one life and no blue mana")
    void cannotPayPhyrexianCostWithInsufficientLife() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new MentalMisstep()));
        harness.setLife(player2, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, elves.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player2, 1);
        harness.assertInHand(player2, "Mental Misstep");
        assertThat(harness.getGameData().stack).hasSize(1);
    }
}
