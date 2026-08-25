package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CrypticCaves;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScamperingSurveyor.class, CrypticCaves.class, Forest.class, GrizzlyBears.class})
class ScamperingSurveyorTest extends BaseCardTest {

    @Test
    void searchesForABasicLandOrCaveAndPutsItOntoTheBattlefieldTapped() {
        Card basicLand = new Forest();
        Card cave = new CrypticCaves();
        Card nonmatching = new GrizzlyBears();
        castSurveyor(List.of(basicLand, cave, nonmatching));

        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        PendingInteraction.LibrarySearch search =
                gameData.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(search.params().cards()).containsExactlyInAnyOrder(basicLand, cave);

        int caveIndex = search.params().cards().indexOf(cave);
        harness.getGameService().handleInteractionAnswer(gameData, player1,
                new InteractionAnswer.LibraryCardChosen(caveIndex));

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
        harness.getGameService().handleInteractionAnswer(gameData, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gameData.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == basicLand && permanent.isTapped());
        assertThat(gameData.interaction.activeInteraction()).isNull();
    }

    private void castSurveyor(List<Card> library) {
        harness.setHand(player1, List.of(new ScamperingSurveyor()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setLibrary(player1, library);
        harness.castCreature(player1, 0);
    }
}
