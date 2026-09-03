package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BayFalcon;
import com.github.laxika.magicalvibes.cards.c.CharcoalDiamond;
import com.github.laxika.magicalvibes.cards.c.Commandeer;
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

@CardUsed({MemoryLapse.class, BayFalcon.class, CharcoalDiamond.class, Commandeer.class})
class MemoryLapseTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack targeting a spell")
    void castingPutsOnStackTargetingSpell() {
        BayFalcon falcon = new BayFalcon();
        harness.setHand(player1, List.of(falcon));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.setHand(player2, List.of(new MemoryLapse()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, falcon.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        StackEntry lapseEntry = gd.stack.getLast();
        assertThat(lapseEntry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(lapseEntry.getTargetId()).isEqualTo(falcon.getId());
    }

    @Test
    @DisplayName("Counters a spell and puts it on top of its owner's library instead of the graveyard")
    void countersAndPutsOnTopOfLibrary() {
        BayFalcon falcon = new BayFalcon();
        harness.setHand(player1, List.of(falcon));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.setHand(player2, List.of(new MemoryLapse()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, falcon.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Countered creature sits on top of its owner's library, not in graveyard or battlefield.
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Bay Falcon");
        harness.assertNotInGraveyard(player1, "Bay Falcon");
        harness.assertNotOnBattlefield(player1, "Bay Falcon");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Bay Falcon"));
    }

    @Test
    @DisplayName("Memory Lapse itself goes to its caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        BayFalcon falcon = new BayFalcon();
        harness.setHand(player1, List.of(falcon));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.setHand(player2, List.of(new MemoryLapse()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, falcon.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player2, "Memory Lapse");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Fizzles if target spell is no longer on the stack")
    void fizzlesIfTargetSpellRemoved() {
        BayFalcon falcon = new BayFalcon();
        harness.setHand(player1, List.of(falcon));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.setHand(player2, List.of(new MemoryLapse()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, falcon.getId());

        GameData gd = harness.getGameData();
        gd.stack.removeIf(se -> se.getCard().getName().equals("Bay Falcon"));

        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player2, "Memory Lapse");
    }

    @Test
    @DisplayName("Puts a countered spell on its owner's library when another player controls it")
    void putsControlledSpellOnItsOwnersLibrary() {
        CharcoalDiamond diamond = new CharcoalDiamond();
        harness.setHand(player1, List.of(diamond, new MemoryLapse()));
        harness.setHand(player2, List.of(new Commandeer(), new MemoryLapse(), new MemoryLapse()));
        harness.setLibrary(player1, List.of(new BayFalcon()));
        harness.setLibrary(player2, List.of(new MemoryLapse()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castArtifact(player1, 0);
        harness.passPriority(player1);
        harness.castInstantWithAlternateExileFromHand(player2, 0, diamond.getId(), List.of(1, 2));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player2.getId());

        harness.castInstant(player1, 0, diamond.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Charcoal Diamond");
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName()).isEqualTo("Memory Lapse");
    }
}
