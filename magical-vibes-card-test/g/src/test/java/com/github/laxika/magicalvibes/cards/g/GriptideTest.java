package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.d.DawntreaderElk;
import com.github.laxika.magicalvibes.cards.e.EvolvingWilds;
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

@CardUsed({Griptide.class, DawntreaderElk.class, EvolvingWilds.class})
class GriptideTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Griptide targeting a creature puts it on the stack")
    void castingTargetingCreaturePutsOnStack() {
        harness.addToBattlefield(player2, new DawntreaderElk());
        UUID targetId = harness.getPermanentId(player2, "Dawntreader Elk");

        harness.setHand(player1, List.of(new Griptide()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Griptide");
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        harness.addToBattlefield(player1, new DawntreaderElk());
        harness.addToBattlefield(player2, new EvolvingWilds());
        UUID landId = harness.getPermanentId(player2, "Evolving Wilds");

        harness.setHand(player1, List.of(new Griptide()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, landId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Resolving Griptide puts target creature on top of its owner's library")
    void resolvingPutsTargetCreatureOnTopOfOwnersLibrary() {
        harness.addToBattlefield(player2, new DawntreaderElk());
        UUID targetId = harness.getPermanentId(player2, "Dawntreader Elk");
        int deckSizeBefore = harness.getGameData().playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new Griptide()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Dawntreader Elk");
        harness.assertNotInGraveyard(player2, "Dawntreader Elk");

        List<Card> deck = gd.playerDecks.get(player2.getId());
        assertThat(deck).hasSize(deckSizeBefore + 1);
        assertThat(deck.getFirst().getName()).isEqualTo("Dawntreader Elk");
        harness.assertInGraveyard(player1, "Griptide");
    }

    @Test
    @DisplayName("Griptide fizzles if the target is removed before resolution")
    void fizzlesIfTargetRemovedBeforeResolution() {
        harness.addToBattlefield(player2, new DawntreaderElk());
        UUID targetId = harness.getPermanentId(player2, "Dawntreader Elk");
        int deckSizeBefore = harness.getGameData().playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new Griptide()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, targetId);
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Griptide");
    }

    @Test
    @DisplayName("A creature controlled by an opponent goes to its owner's library without shuffling")
    void returnsStolenCreatureToOwnersLibrary() {
        DawntreaderElk creature = new DawntreaderElk();
        creature.setOwnerId(player1.getId());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, creature).getId();
        EvolvingWilds first = new EvolvingWilds();
        DawntreaderElk second = new DawntreaderElk();
        harness.setLibrary(player1, List.of(first, second));
        List<Card> controllerLibrary = List.copyOf(gd.playerDecks.get(player2.getId()));
        harness.setHand(player1, List.of(new Griptide()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Dawntreader Elk");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature, first, second);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(controllerLibrary);
        harness.assertNotInGraveyard(player1, "Dawntreader Elk");
        harness.assertInGraveyard(player1, "Griptide");
    }

    @Test
    @DisplayName("Griptide can put its controller's own creature on top of their library")
    void canTargetOwnCreature() {
        DawntreaderElk creature = new DawntreaderElk();
        UUID targetId = harness.addToBattlefieldAndReturn(player1, creature).getId();
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new Griptide()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Dawntreader Elk");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        harness.assertInGraveyard(player1, "Griptide");
    }
}
