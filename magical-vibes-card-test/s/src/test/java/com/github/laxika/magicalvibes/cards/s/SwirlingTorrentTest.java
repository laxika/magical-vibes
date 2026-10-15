package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SwirlingTorrent.class, GrizzlyBears.class, Forest.class})
class SwirlingTorrentTest extends BaseCardTest {

    @Test
    @DisplayName("Top mode puts a target creature on top of its owner's library")
    void topModePutsCreatureOnLibrary() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();
        cast(new int[]{0}, List.of(creature.getId()));

        GameData gameData = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gameData.playerDecks.get(player2.getId())).hasSize(deckSizeBefore + 1);
        assertThat(gameData.playerDecks.get(player2.getId()).getFirst().getName())
                .isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Bounce mode returns a target creature to its owner's hand")
    void bounceModeReturnsCreatureToHand() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(new int[]{1}, List.of(creature.getId()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Both modes resolve with separate creature targets")
    void bothModesResolveWithSeparateTargets() {
        Permanent topCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent bouncedCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        cast(new int[]{0, 1}, List.of(topCreature.getId(), bouncedCreature.getId()));

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gameData.playerDecks.get(player2.getId())).hasSize(deckSizeBefore + 1);
        assertThat(gameData.playerDecks.get(player2.getId()).getFirst().getName())
                .isEqualTo("Grizzly Bears");
        assertThat(gameData.playerHands.get(player2.getId()).stream()
                .filter(card -> card.getName().equals("Grizzly Bears")))
                .hasSize(1);
    }

    @Test
    @DisplayName("Modes cannot target a noncreature permanent")
    void rejectsNoncreatureTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new SwirlingTorrent()));
        addMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 2, new int[]{0}, List.of(land.getId()), null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modes, List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new SwirlingTorrent()));
        addMana();
        harness.castModalSorceryWithModes(player1, 0, 1, 2, modes, targetIds, null);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Both modes can target the same creature and resolve in printed order")
    void bothModesCanShareTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        cast(new int[]{1, 0}, List.of(creature.getId(), creature.getId()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInHand(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore + 1);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(creature.getCard());
    }

    @Test
    @DisplayName("Bounce mode cannot target a noncreature permanent")
    void bounceModeRejectsNoncreatureTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new SwirlingTorrent()));
        addMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 2, new int[]{1}, List.of(land.getId()), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Neither mode resolves when their shared target leaves before resolution")
    void sharedTargetLeavingPreventsResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();
        harness.setHand(player1, List.of(new SwirlingTorrent()));
        addMana();
        harness.castModalSorceryWithModes(player1, 0, 1, 2,
                new int[]{0, 1}, List.of(creature.getId(), creature.getId()), null);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, creature));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);
        assertThat(gd.playerHands.get(player2.getId())).contains(creature.getCard());
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Swirling Torrent");
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("The remaining legal target is affected when the other target leaves")
    void resolvesRemainingLegalTarget(int removedMode) {
        Permanent topCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent bouncedCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();
        harness.setHand(player1, List.of(new SwirlingTorrent()));
        addMana();
        harness.castModalSorceryWithModes(player1, 0, 1, 2,
                new int[]{0, 1}, List.of(topCreature.getId(), bouncedCreature.getId()), null);

        Permanent removed = removedMode == 0 ? topCreature : bouncedCreature;
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, removed));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore + (removedMode == 0 ? 0 : 1));
        assertThat(gd.playerHands.get(player2.getId())).contains(bouncedCreature.getCard());
        if (removedMode == 0) {
            assertThat(gd.playerHands.get(player2.getId())).contains(topCreature.getCard());
        } else {
            assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(topCreature.getCard());
            assertThat(gd.playerHands.get(player2.getId())).doesNotContain(topCreature.getCard());
        }
        harness.assertInGraveyard(player1, "Swirling Torrent");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Each mode sends a stolen creature to its owner's zone")
    void stolenCreatureGoesToOwner(int mode) {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.stolenCreatures.put(creature.getId(), player2.getId());
        int ownerDeckSize = gd.playerDecks.get(player2.getId()).size();
        int controllerDeckSize = gd.playerDecks.get(player1.getId()).size();

        cast(new int[]{mode}, List.of(creature.getId()));

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(controllerDeckSize);
        if (mode == 0) {
            assertThat(gd.playerDecks.get(player2.getId())).hasSize(ownerDeckSize + 1);
            assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(creature.getCard());
            harness.assertNotInHand(player2, "Grizzly Bears");
        } else {
            assertThat(gd.playerDecks.get(player2.getId())).hasSize(ownerDeckSize);
            harness.assertInHand(player2, "Grizzly Bears");
        }
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
