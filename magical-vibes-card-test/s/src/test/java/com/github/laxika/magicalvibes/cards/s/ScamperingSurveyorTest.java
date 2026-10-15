package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CavernousMaw;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScamperingSurveyor.class, CavernousMaw.class, Forest.class})
class ScamperingSurveyorTest extends BaseCardTest {

    @Test
    void searchesForABasicLandOrCaveAndPutsItOntoTheBattlefieldTapped() {
        Card basicLand = new Forest();
        Card cave = new CavernousMaw();
        Card nonmatching = new ScamperingSurveyor();
        castSurveyor(List.of(basicLand, cave, nonmatching));

        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        PendingInteraction.LibrarySearch search =
                gameData.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(search.params().cards()).containsExactlyInAnyOrder(basicLand, cave);

        int caveIndex = search.params().cards().indexOf(cave);
        harness.handleCardChosen(player1, caveIndex);

        assertThat(gameData.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == cave && permanent.isTapped());
        assertThat(gameData.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(basicLand, nonmatching);
        assertThat(gameData.interaction.activeInteraction()).isNull();
    }

    @Test
    void putsABasicLandOntoTheBattlefieldTapped() {
        Card basicLand = new Forest();
        castSurveyor(List.of(basicLand));

        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        harness.handleCardChosen(player1, 0);

        assertThat(gameData.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == basicLand && permanent.isTapped());
        assertThat(gameData.interaction.activeInteraction()).isNull();
    }

    @Test
    void canFailToFindEvenWhenBasicLandsAndCavesAreAvailable() {
        Card basicLand = new Forest();
        Card cave = new CavernousMaw();
        castSurveyor(List.of(basicLand, cave));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(basicLand, cave);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1)
                .noneMatch(permanent -> permanent.getCard() == basicLand || permanent.getCard() == cave);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void resolvesWithoutFindingAnIneligibleCard() {
        Card nonmatching = new ScamperingSurveyor();
        castSurveyor(List.of(nonmatching));

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonmatching);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1)
                .noneMatch(permanent -> permanent.getCard() == nonmatching);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void resolvesWithAnEmptyLibrary() {
        castSurveyor(List.of());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void castSurveyor(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.castFromHand(player1, new ScamperingSurveyor(), "{4}");
    }
}
