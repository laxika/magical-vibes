package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

@CardUsed({VergeRangers.class, Forest.class})
class VergeRangersTest extends BaseCardTest {

    @Test
    @DisplayName("Allows playing a land from the top when an opponent controls more lands")
    void playsLandFromLibraryTopWhenOpponentHasMoreLands() {
        harness.addToBattlefield(player1, new VergeRangers());
        harness.addToBattlefield(player2, new Forest());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(forest);
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not allow playing a land from the top when no opponent controls more lands")
    void doesNotPlayLandFromLibraryTopWithoutLandAdvantage() {
        harness.addToBattlefield(player1, new VergeRangers());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(forest);
    }
}
