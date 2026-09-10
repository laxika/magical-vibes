package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WarpedResearcher.class, Censor.class, GrizzlyBears.class})
class WarpedResearcherTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling a card gives Warped Researcher flying and shroud")
    void cyclingGrantsFlyingAndShroud() {
        Permanent researcher = harness.addToBattlefieldAndReturn(player1, new WarpedResearcher());
        setUpCycling(player1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(researcher.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(researcher.hasKeyword(Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("An opponent cycling a card gives Warped Researcher flying and shroud")
    void opponentsCyclingGrantsFlyingAndShroud() {
        Permanent researcher = harness.addToBattlefieldAndReturn(player1, new WarpedResearcher());
        setUpCycling(player2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateHandAbility(player2, 0, null);
        harness.passBothPriorities();

        assertThat(researcher.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(researcher.hasKeyword(Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("The granted keywords wear off at end of turn")
    void grantedKeywordsWearOffAtEndOfTurn() {
        Permanent researcher = harness.addToBattlefieldAndReturn(player1, new WarpedResearcher());
        setUpCycling(player1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(researcher.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(researcher.hasKeyword(Keyword.SHROUD)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(researcher.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(researcher.hasKeyword(Keyword.SHROUD)).isFalse();
    }

    private void setUpCycling(Player player) {
        harness.setHand(player, List.of(new Censor()));
        harness.setLibrary(player, List.of(new GrizzlyBears()));
        harness.addMana(player, ManaColor.BLUE, 1);
    }
}
