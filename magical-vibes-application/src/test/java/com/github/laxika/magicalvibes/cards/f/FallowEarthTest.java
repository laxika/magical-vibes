package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FallowEarth.class, Forest.class, GrizzlyBears.class})
class FallowEarthTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving puts target land on top of owner's library")
    void resolvingPutsLandOnTopOfLibrary() {
        harness.addToBattlefield(player2, new Forest());
        UUID landId = harness.getPermanentId(player2, "Forest");

        int deckSizeBefore = harness.getGameData().playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new FallowEarth()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, landId);

        GameData gd = harness.getGameData();
        // Land removed from battlefield
        harness.assertNotOnBattlefield(player2, "Forest");
        // Land NOT in graveyard
        harness.assertNotInGraveyard(player2, "Forest");
        // Land on top of owner's library
        List<Card> deck = gd.playerDecks.get(player2.getId());
        assertThat(deck).hasSize(deckSizeBefore + 1);
        assertThat(deck.getFirst().getName()).isEqualTo("Forest");
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonLand() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new FallowEarth()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A land controlled by another player goes to its owner's library without shuffling")
    void putsBorrowedLandOnOwnersLibrary() {
        Forest land = new Forest();
        land.setOwnerId(player1.getId());
        UUID landId = harness.addToBattlefieldAndReturn(player2, land).getId();
        GrizzlyBears first = new GrizzlyBears();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new FallowEarth()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, landId);

        harness.assertNotOnBattlefield(player2, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land, first, second);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        harness.assertNotInGraveyard(player1, "Forest");
        harness.assertNotInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Can put your own land on top of an empty library")
    void putsOwnLandOnEmptyLibrary() {
        Forest land = new Forest();
        UUID landId = harness.addToBattlefieldAndReturn(player1, land).getId();
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new FallowEarth()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, landId);

        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        harness.assertInGraveyard(player1, "Fallow Earth");
    }

    @Test
    @DisplayName("Fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new Forest());
        UUID landId = harness.getPermanentId(player2, "Forest");

        int deckSizeBefore = harness.getGameData().playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new FallowEarth()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, landId);

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }
}
