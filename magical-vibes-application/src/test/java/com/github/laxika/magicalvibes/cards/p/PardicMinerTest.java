package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.GameActionAvailabilityService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PardicMinerTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Pardic Miner stops the target player from playing lands this turn")
    void stopsTargetPlayerFromPlayingLands() {
        harness.addToBattlefield(player1, new PardicMiner());
        harness.setHand(player2, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Pardic Miner");
        harness.assertInGraveyard(player1, "Pardic Miner");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        GameActionAvailabilityService gameActionAvailabilityService = harness.getGameActionAvailabilityService();
        assertThat(gameActionAvailabilityService.getPlayableCardIndices(gd, player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Pardic Miner's restriction expires at the end of the turn")
    void restrictionExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new PardicMiner());
        harness.setHand(player2, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        GameActionAvailabilityService gameActionAvailabilityService = harness.getGameActionAvailabilityService();
        assertThat(gameActionAvailabilityService.getPlayableCardIndices(gd, player2.getId())).contains(0);
    }
}
