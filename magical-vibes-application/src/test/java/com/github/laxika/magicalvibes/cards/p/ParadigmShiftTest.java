package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ParadigmShiftTest extends BaseCardTest {

    private long exiledCountFor(GameData gd, java.util.UUID ownerId) {
        return gd.exiledCards.stream().filter(e -> ownerId.equals(e.ownerId())).count();
    }

    @Test
    @DisplayName("Exiles the whole library, then the graveyard becomes the new library")
    void exilesLibraryThenShufflesGraveyardIn() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GiantSpider()));
        harness.setGraveyard(player1, List.of(new GiantSpider(), new GiantSpider()));
        harness.setHand(player1, List.of(new ParadigmShift()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Only the two graveyard cards remain in the library — the old library was exiled first.
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId()))
                .allMatch(c -> c.getName().equals("Giant Spider"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Giant Spider"));
        assertThat(exiledCountFor(gd, player1.getId())).isEqualTo(3);
    }

    @Test
    @DisplayName("With an empty graveyard the library ends up empty")
    void emptyGraveyardLeavesEmptyLibrary() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setGraveyard(player1, new ArrayList<>());
        harness.setHand(player1, List.of(new ParadigmShift()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(exiledCountFor(gd, player1.getId())).isEqualTo(2);
        // Paradigm Shift itself resolves before it hits the graveyard, so it is not shuffled back.
        harness.assertInGraveyard(player1, "Paradigm Shift");
    }

    @Test
    @DisplayName("Only the controller's zones are affected")
    void opponentZonesUntouched() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        Card opponentCard = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.setHand(player1, List.of(new ParadigmShift()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        int opponentDeckBefore = harness.getGameData().playerDecks.get(player2.getId()).size();

        harness.castSorcery(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentDeckBefore);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }
}
