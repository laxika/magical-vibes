package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.SnowCoveredPlains;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DireWolves.class, SnowCoveredPlains.class})
class DireWolvesTest extends BaseCardTest {

    @Test
    @DisplayName("No banding without a Plains")
    void noBandingWithoutPlains() {
        Permanent wolves = harness.addToBattlefieldAndReturn(player1, new DireWolves());

        assertThat(gqs.hasKeyword(gd, wolves, Keyword.BANDING)).isFalse();
    }

    @Test
    @DisplayName("Has banding while controlling a Plains")
    void hasBandingWithPlains() {
        Permanent wolves = harness.addToBattlefieldAndReturn(player1, new DireWolves());
        harness.addToBattlefield(player1, new SnowCoveredPlains());

        assertThat(gqs.hasKeyword(gd, wolves, Keyword.BANDING)).isTrue();
    }

    @Test
    @DisplayName("Loses banding when the Plains leaves")
    void losesBandingWhenPlainsLeaves() {
        Permanent wolves = harness.addToBattlefieldAndReturn(player1, new DireWolves());
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new SnowCoveredPlains());

        assertThat(gqs.hasKeyword(gd, wolves, Keyword.BANDING)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, plains));

        assertThat(gqs.hasKeyword(gd, wolves, Keyword.BANDING)).isFalse();
    }

    @Test
    @DisplayName("Opponent's Plains doesn't grant banding")
    void opponentPlainsDoesNotCount() {
        Permanent wolves = harness.addToBattlefieldAndReturn(player1, new DireWolves());
        harness.addToBattlefield(player2, new SnowCoveredPlains());

        assertThat(gqs.hasKeyword(gd, wolves, Keyword.BANDING)).isFalse();
    }

    @Test
    @DisplayName("Granted banding can be used to form a band")
    void grantedBandingCanFormBand() {
        Permanent firstWolves = addCreatureReady(player1, new DireWolves());
        Permanent secondWolves = addCreatureReady(player1, new DireWolves());
        harness.addToBattlefield(player1, new SnowCoveredPlains());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        harness.inMutationScope(() -> harness.getCombatAttackService()
                .declareAttackers(gd, player1, List.of(0, 1), null, List.of(List.of(0, 1))));

        assertThat(firstWolves.getBandId()).isNotNull();
        assertThat(secondWolves.getBandId()).isEqualTo(firstWolves.getBandId());
    }
}
