package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Excommunicate.class, GrizzlyBears.class, Forest.class})
class ExcommunicateTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Excommunicate targeting a creature puts it on the stack")
    void castingTargetingCreaturePutsOnStack() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new Excommunicate()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Excommunicate");
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        UUID landId = harness.getPermanentId(player2, "Forest");

        harness.setHand(player1, List.of(new Excommunicate()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, landId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Resolving puts creature on top of owner's library")
    void resolvingPutsCreatureOnTopOfLibrary() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        int deckSizeBefore = harness.getGameData().playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new Excommunicate()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        // Creature removed from battlefield
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        // Creature NOT in graveyard
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        // Creature on top of library (first element)
        List<Card> deck = gd.playerDecks.get(player2.getId());
        assertThat(deck).hasSize(deckSizeBefore + 1);
        assertThat(deck.getFirst().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Resolving on a token removes it from the library as a state-based action")
    void resolvingOnTokenRemovesItFromLibrary() {
        Card token = new Card();
        token.setName("Vampire");
        token.setType(com.github.laxika.magicalvibes.model.CardType.CREATURE);
        token.setPower(1);
        token.setToughness(1);
        token.setToken(true);
        harness.addToBattlefield(player2, token);
        UUID targetId = harness.getPermanentId(player2, "Vampire");
        int deckSizeBefore = harness.getGameData().playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new Excommunicate()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castAndResolveSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Vampire");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);
        assertThat(gd.playerDecks.get(player2.getId())).noneMatch(Card::isToken);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("Vampire") && log.contains("ceases to exist"));
    }

    @Test
    @DisplayName("Excommunicate goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new Excommunicate()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Excommunicate");
    }

    @Test
    @DisplayName("Fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        int deckSizeBefore = harness.getGameData().playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new Excommunicate()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, targetId);

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Library should be unchanged
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        // Excommunicate still goes to graveyard
        harness.assertInGraveyard(player1, "Excommunicate");
    }

    @Test
    @DisplayName("A creature controlled by another player goes to its owner's library")
    void controlledCreatureReturnsToOwnersLibrary() {
        Card creature = new GrizzlyBears();
        creature.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, creature);
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        List<Card> ownersLibraryBefore = List.copyOf(gd.playerDecks.get(player1.getId()));
        List<Card> controllersLibraryBefore = List.copyOf(gd.playerDecks.get(player2.getId()));
        harness.setHand(player1, List.of(new Excommunicate()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(creature);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, ownersLibraryBefore.size() + 1))
                .containsExactlyElementsOf(ownersLibraryBefore);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(controllersLibraryBefore);
        harness.assertInGraveyard(player1, "Excommunicate");
    }

    @Test
    @DisplayName("Can put your own creature on top of an empty library")
    void ownCreatureGoesOnTopOfEmptyLibrary() {
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player1, creature);
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new Excommunicate()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Excommunicate");
    }

}
