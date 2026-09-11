package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.Commandeer;
import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.d.DreamTwist;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MemoryLapse.class, GrizzlyBears.class, DarkRitual.class})
class MemoryLapseTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack targeting a spell")
    void castingPutsOnStackTargetingSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new MemoryLapse()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        StackEntry lapseEntry = gd.stack.getLast();
        assertThat(lapseEntry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(lapseEntry.getTargetId()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Counters a spell and puts it on top of its owner's library instead of the graveyard")
    void countersAndPutsOnTopOfLibrary() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new MemoryLapse()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        GameData gd = harness.getGameData();
        // Countered creature sits on top of its owner's library, not in graveyard or battlefield.
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Counters a noncreature spell and puts it on top of its owner's library")
    void countersNonCreatureSpellAndPutsOnTopOfLibrary() {
        DarkRitual ritual = new DarkRitual();
        harness.setHand(player1, List.of(ritual));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.setHand(player2, List.of(new MemoryLapse()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, ritual.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Dark Ritual");
        harness.assertNotInGraveyard(player1, "Dark Ritual");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Memory Lapse itself goes to its caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new MemoryLapse()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player2, "Memory Lapse");
        assertThat(gd.stack).isEmpty();
    }

    @CardUsed(DreamTwist.class)
    @Test
    @DisplayName("Exiles a spell cast with flashback instead of putting it on top of its owner's library")
    void exilesFlashbackSpellInsteadOfPuttingItOnTopOfLibrary() {
        DreamTwist twist = new DreamTwist();
        harness.setGraveyard(player1, List.of(twist));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFlashback(player1, 0, player2.getId());
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new MemoryLapse()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, twist.getId());

        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(twist);
        harness.assertNotInGraveyard(player1, "Dream Twist");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(twist.getId()));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Fizzles if target spell is no longer on the stack")
    void fizzlesIfTargetSpellRemoved() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new MemoryLapse()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());

        GameData gd = harness.getGameData();
        gd.stack.removeIf(se -> se.getCard().getName().equals("Grizzly Bears"));

        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player2, "Memory Lapse");
    }

    @CardUsed(Commandeer.class)
    @Test
    @DisplayName("Puts a countered spell on its owner's library when another player controls it")
    void putsControlledSpellOnItsOwnersLibrary() {
        DarkRitual ritual = new DarkRitual();
        harness.setHand(player1, List.of(ritual, new MemoryLapse()));
        harness.setHand(player2, List.of(new Commandeer(), new MemoryLapse(), new MemoryLapse()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new MemoryLapse()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0);
        harness.passPriority(player1);
        harness.castInstantWithAlternateExileFromHand(player2, 0, ritual.getId(), List.of(1, 2));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player2.getId());

        harness.castAndResolveInstant(player1, 0, ritual.getId());

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Dark Ritual");
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName()).isEqualTo("Memory Lapse");
    }
}
