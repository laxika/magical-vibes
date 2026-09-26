package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BeaconOfCreation.class, Forest.class, Mountain.class})
class BeaconOfCreationTest extends BaseCardTest {

    @Test
    void createsOneInsectForEachForestAndShufflesIntoLibrary() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Mountain());
        int librarySizeBefore = gd.playerDecks.get(player1.getId()).size();
        BeaconOfCreation beacon = new BeaconOfCreation();
        harness.castFromHand(player1, beacon, "{3}{G}");

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
    void createsOneOneGreenInsectCreatureTokens() {
        harness.addToBattlefield(player1, new Forest());
        harness.castFromHand(player1, new BeaconOfCreation(), "{3}{G}");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Insect"))
                .singleElement()
                .satisfies(insect -> {
                    assertThat(insect.getCard().isToken()).isTrue();
                    assertThat(insect.getCard().getType()).isEqualTo(CardType.CREATURE);
                    assertThat(insect.getEffectivePower()).isEqualTo(1);
                    assertThat(insect.getEffectiveToughness()).isEqualTo(1);
                    assertThat(insect.getCard().getColor()).isEqualTo(CardColor.GREEN);
                    assertThat(insect.getCard().getSubtypes()).containsExactly(CardSubtype.INSECT);
                });
    }

    @Test
    void doesNotCountForestsControlledByAnotherPlayer() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Forest());
        harness.castFromHand(player1, new BeaconOfCreation(), "{3}{G}");

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Insect")).isZero();
    }
}
