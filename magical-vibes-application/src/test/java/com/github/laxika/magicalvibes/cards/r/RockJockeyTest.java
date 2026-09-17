package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.GameActionAvailabilityService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RockJockey.class, Mountain.class})
class RockJockeyTest extends BaseCardTest {

    @Test
    @DisplayName("Can't be cast after its controller plays a land")
    void cannotBeCastAfterPlayingLand() {
        harness.setHand(player1, List.of(new Mountain(), new RockJockey()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        GameActionAvailabilityService availability = harness.getGameActionAvailabilityService();
        assertThat(availability.getPlayableCardIndices(gd, player1.getId())).isEmpty();
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Prevents its controller from playing lands after being cast")
    void preventsLandPlayAfterBeingCast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new RockJockey(), "{2}{R}");
        harness.setHand(player1, List.of(new Mountain()));
        harness.passBothPriorities();

        GameActionAvailabilityService availability = harness.getGameActionAvailabilityService();
        assertThat(availability.getPlayableCardIndices(gd, player1.getId())).isEmpty();
        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Its land-play restriction ends at the next turn")
    void landPlayRestrictionEndsAtNextTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new RockJockey(), "{2}{R}");
        harness.setHand(player1, List.of(new Mountain()));
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.ensurePriority(player1);

        GameActionAvailabilityService availability = harness.getGameActionAvailabilityService();
        assertThat(availability.getPlayableCardIndices(gd, player1.getId())).contains(0);
    }

    @Test
    @DisplayName("Doesn't restrict its opponent from playing lands")
    void doesNotRestrictOpponentFromPlayingLands() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new RockJockey(), "{2}{R}");
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Mountain()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameActionAvailabilityService availability = harness.getGameActionAvailabilityService();
        assertThat(availability.getPlayableCardIndices(gd, player2.getId())).contains(0);
        harness.playLand(player2, 0);
        harness.assertOnBattlefield(player2, "Mountain");
    }

    @Test
    @DisplayName("Doesn't restrict land plays when it entered without being cast")
    void doesNotRestrictLandPlayAfterEnteringWithoutBeingCast() {
        harness.enterBattlefieldAndReturn(player1, new RockJockey());
        harness.setHand(player1, List.of(new Mountain()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameActionAvailabilityService availability = harness.getGameActionAvailabilityService();
        assertThat(availability.getPlayableCardIndices(gd, player1.getId())).contains(0);
        harness.playLand(player1, 0);
        harness.assertOnBattlefield(player1, "Mountain");
    }
}
