package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.TurnStep;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VergeRangers.class, Forest.class})
class VergeRangersTest extends BaseCardTest {

    @Test
    @DisplayName("allows playing a land from the top when an opponent controls more lands")
    void allowsLandFromTopWhenOpponentHasMoreLands() {
        harness.addToBattlefield(player1, new VergeRangers());
        harness.addToBattlefield(player2, new Forest());
        Forest topLand = new Forest();
        harness.setLibrary(player1, List.of(topLand));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(topLand);
    }

    @Test
    @DisplayName("does not allow playing a land from the top when the land counts are tied")
    void doesNotAllowLandFromTopWhenLandCountsAreTied() {
        harness.addToBattlefield(player1, new VergeRangers());
        Forest topLand = new Forest();
        harness.setLibrary(player1, List.of(topLand));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topLand);
    }
}
