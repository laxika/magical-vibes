package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.GameActionAvailabilityService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AggressiveMining.class, Forest.class})
class AggressiveMiningTest extends BaseCardTest {

    @Test
    @DisplayName("Controller can't play lands while Aggressive Mining is on the battlefield")
    void controllerCantPlayLands() {
        harness.addToBattlefield(player1, new AggressiveMining());

        harness.setHand(player1, List.of(new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameActionAvailabilityService gbs = harness.getGameActionAvailabilityService();
        assertThat(gbs.getPlayableCardIndices(gd, player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Opponents can still play lands")
    void opponentUnaffected() {
        harness.addToBattlefield(player1, new AggressiveMining());

        harness.setHand(player2, List.of(new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameActionAvailabilityService gbs = harness.getGameActionAvailabilityService();
        assertThat(gbs.getPlayableCardIndices(gd, player2.getId())).contains(0);
    }

    @Test
    @DisplayName("Sacrificing a land draws two cards")
    void sacrificeLandDrawsTwo() {
        harness.addToBattlefield(player1, new AggressiveMining());
        harness.addToBattlefield(player1, new Forest());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(perm -> perm.getCard() instanceof Forest);
    }

    @Test
    @DisplayName("Ability can only be activated once each turn")
    void onlyOncePerTurn() {
        harness.addToBattlefield(player1, new AggressiveMining());
        harness.addToBattlefield(player1, new Forest());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.addToBattlefield(player1, new Forest());
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void sacrificeIsPaidBeforeCardsAreDrawn() {
        harness.addToBattlefield(player1, new AggressiveMining());
        harness.addToBattlefield(player1, new Forest());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void cannotSacrificeAnOpponentsLand() {
        harness.addToBattlefield(player1, new AggressiveMining());
        harness.addToBattlefield(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canActivateDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new AggressiveMining());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    void eachCopyHasItsOwnActivationLimit() {
        harness.addToBattlefield(player1, new AggressiveMining());
        harness.addToBattlefield(player1, new AggressiveMining());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new Forest());
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }

    @Test
    void landPlayIsRejectedByTheActionService() {
        harness.addToBattlefield(player1, new AggressiveMining());
        harness.setHand(player1, List.of(new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    void activationLimitResetsOnTheNextPlayersTurn() {
        harness.addToBattlefield(player1, new AggressiveMining());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.addToBattlefield(player1, new Forest());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }

    @Test
    void cannotActivateAgainWhileFirstActivationIsOnTheStack() {
        harness.addToBattlefield(player1, new AggressiveMining());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.addToBattlefield(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
