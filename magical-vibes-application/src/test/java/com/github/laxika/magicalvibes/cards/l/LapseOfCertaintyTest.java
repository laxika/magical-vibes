package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DutifulKnowledgeSeeker;
import com.github.laxika.magicalvibes.cards.s.ScattershotArcher;
import com.github.laxika.magicalvibes.cards.v.VolcanicFallout;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
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

@CardUsed({LapseOfCertainty.class, GrizzlyBears.class})
class LapseOfCertaintyTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack targeting a spell")
    void castingPutsOnStackTargetingSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new LapseOfCertainty()));
        harness.addMana(player2, ManaColor.WHITE, 3);

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

        harness.setHand(player2, List.of(new LapseOfCertainty()));
        harness.addMana(player2, ManaColor.WHITE, 3);

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
    @DisplayName("Fizzles if target spell is no longer on the stack")
    void fizzlesIfTargetSpellRemoved() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new LapseOfCertainty()));
        harness.addMana(player2, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());

        GameData gd = harness.getGameData();
        gd.stack.removeIf(se -> se.getCard().getName().equals("Grizzly Bears"));

        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player2, "Lapse of Certainty");
    }

    @Test
    @CardUsed(VolcanicFallout.class)
    void uncounterableSpellRemainsOnStackAndResolves() {
        VolcanicFallout fallout = new VolcanicFallout();
        harness.setHand(player1, List.of(fallout));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setHand(player2, List.of(new LapseOfCertainty()));
        harness.addMana(player2, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, fallout.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(fallout);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(fallout);
        harness.assertInGraveyard(player2, "Lapse of Certainty");
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Volcanic Fallout");
    }

    @Test
    @CardUsed(ScattershotArcher.class)
    void canCounterOwnSpellOntoAnExistingLibrary() {
        ScattershotArcher archer = new ScattershotArcher();
        LapseOfCertainty libraryCard = new LapseOfCertainty();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(archer, new LapseOfCertainty()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player1, 0, archer.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(archer, libraryCard);
        harness.assertNotInGraveyard(player1, "Scattershot Archer");
        harness.assertInGraveyard(player1, "Lapse of Certainty");
    }

    @Test
    @CardUsed({ScattershotArcher.class, DutifulKnowledgeSeeker.class})
    void puttingCounteredSpellIntoLibraryTriggersKnowledgeSeeker() {
        Permanent seeker = harness.addToBattlefieldAndReturn(player2, new DutifulKnowledgeSeeker());
        ScattershotArcher archer = new ScattershotArcher();
        harness.setHand(player1, List.of(archer));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new LapseOfCertainty()));
        harness.addMana(player2, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, archer.getId());

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(archer);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(seeker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
