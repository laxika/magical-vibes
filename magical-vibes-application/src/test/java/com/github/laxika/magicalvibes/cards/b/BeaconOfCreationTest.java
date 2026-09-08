package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BeaconOfCreationTest extends BaseCardTest {

    @Test
    void createsOneInsectForEachForestAndShufflesIntoLibrary() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new BeaconOfCreation()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        int librarySizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(countPermanents(player1, "Insect")).isEqualTo(2);
        assertThat(gameData.playerDecks.get(player1.getId())).hasSize(librarySizeBefore + 1);
        assertThat(gameData.playerDecks.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Beacon of Creation"))
                .hasSize(1);
        harness.assertNotInGraveyard(player1, "Beacon of Creation");
    }

    @Test
    void doesNotCountForestsControlledByAnotherPlayer() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new BeaconOfCreation()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Insect")).isZero();
    }
}
