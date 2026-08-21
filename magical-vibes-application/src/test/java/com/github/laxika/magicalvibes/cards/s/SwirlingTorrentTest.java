package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
