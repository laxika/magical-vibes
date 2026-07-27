package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MightOfOaks;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FlashfreezeTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts it on the stack targeting a green spell")
    void castingPutsOnStackTargetingGreenSpell() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new Flashfreeze()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        StackEntry flashfreezeEntry = gd.stack.getLast();
        assertThat(flashfreezeEntry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(flashfreezeEntry.getCard().getName()).isEqualTo("Flashfreeze");
        assertThat(flashfreezeEntry.getTargetId()).isEqualTo(elves.getId());
    }

    @Test
    @DisplayName("Cannot target a blue spell")
    void cannotTargetBlueSpell() {
        AirElemental airElemental = new AirElemental();
        harness.setHand(player1, List.of(airElemental));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.setHand(player2, List.of(new Flashfreeze()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, airElemental.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Resolving counters a green creature spell")
    void countersGreenCreatureSpell() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new Flashfreeze()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId());
        harness.passBothPriorities();

        // Countered spell goes to owner's graveyard
        harness.assertInGraveyard(player1, "Llanowar Elves");
        // Does not enter the battlefield
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Resolving counters a green instant spell")
    void countersGreenInstantSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new Flashfreeze()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, might.getId());
        harness.passBothPriorities();

        // Might of Oaks countered and in graveyard
        harness.assertInGraveyard(player1, "Might of Oaks");
    }

    @Test
    @DisplayName("Flashfreeze goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new Flashfreeze()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player2, "Flashfreeze");
        assertThat(gd.stack).isEmpty();
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Fizzles if target spell is no longer on the stack")
    void fizzlesIfTargetSpellRemoved() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new Flashfreeze()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId());

        // Remove target from stack before Flashfreeze resolves
        GameData gd = harness.getGameData();
        gd.stack.removeIf(se -> se.getCard().getName().equals("Llanowar Elves"));

        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        // Flashfreeze still goes to graveyard
        harness.assertInGraveyard(player2, "Flashfreeze");
    }
}

