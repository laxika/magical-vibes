package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.GameActionAvailabilityService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PardicMiner.class, Forest.class})
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

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        GameActionAvailabilityService gameActionAvailabilityService = harness.getGameActionAvailabilityService();
        assertThat(gameActionAvailabilityService.getPlayableCardIndices(gd, player2.getId())).contains(0);
    }

    @Test
    @DisplayName("Pardic Miner can target its controller")
    void canTargetItsController() {
        harness.addToBattlefield(player1, new PardicMiner());
        harness.setHand(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        GameActionAvailabilityService gameActionAvailabilityService = harness.getGameActionAvailabilityService();
        assertThat(gameActionAvailabilityService.getPlayableCardIndices(gd, player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Pardic Miner's ability requires a target player")
    void requiresTargetPlayer() {
        harness.addToBattlefield(player1, new PardicMiner());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Pardic Miner");
        harness.assertNotInGraveyard(player1, "Pardic Miner");
    }

    @Test
    @DisplayName("Pardic Miner's ability can target only a player")
    void rejectsNonPlayerTarget() {
        harness.addToBattlefield(player1, new PardicMiner());
        harness.addToBattlefield(player2, new Forest());
        UUID forestId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forestId))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Pardic Miner");
        harness.assertNotInGraveyard(player1, "Pardic Miner");
    }
}
