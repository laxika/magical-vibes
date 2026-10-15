package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.t.TalrandSkySummoner;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LoyalDrake.class, TalrandSkySummoner.class})
class LoyalDrakeTest extends BaseCardTest {

    @Test
    void drawsAtBeginningOfCombatWhileControllingCommander() {
        Card commander = new TalrandSkySummoner();
        gd.makeCommander(player1.getId(), commander);
        harness.addToBattlefield(player1, commander);
        harness.addToBattlefield(player1, new LoyalDrake());
        harness.setLibrary(player1, List.of(new LoyalDrake()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        advanceToBeginningOfCombat();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    void doesNotDrawWithoutControllingCommander() {
        harness.addToBattlefield(player1, new LoyalDrake());
        harness.setLibrary(player1, List.of(new LoyalDrake()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        advanceToBeginningOfCombat();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    void doesNotTriggerDuringOpponentsCombat() {
        Card commander = new TalrandSkySummoner();
        gd.makeCommander(player1.getId(), commander);
        harness.addToBattlefield(player1, commander);
        harness.addToBattlefield(player1, new LoyalDrake());
        harness.setLibrary(player1, List.of(new LoyalDrake()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    void controllingOpponentsCommanderDoesNotSatisfyLieutenant() {
        Card commander = new TalrandSkySummoner();
        gd.makeCommander(player2.getId(), commander);
        harness.addToBattlefield(player1, commander);
        harness.addToBattlefield(player1, new LoyalDrake());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTriggerWhenOpponentControlsYourCommander() {
        Card commander = new TalrandSkySummoner();
        gd.makeCommander(player1.getId(), commander);
        harness.addToBattlefield(player2, commander);
        harness.addToBattlefield(player1, new LoyalDrake());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void losingControlOfCommanderBeforeResolutionPreventsDraw() {
        Card commander = new TalrandSkySummoner();
        gd.makeCommander(player1.getId(), commander);
        var commanderPermanent = harness.addToBattlefieldAndReturn(player1, commander);
        harness.addToBattlefield(player1, new LoyalDrake());
        harness.setLibrary(player1, List.of(new LoyalDrake()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(commanderPermanent);
        gd.playerBattlefields.get(player2.getId()).add(commanderPermanent);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void commanderEnteringAfterCombatBeginsDoesNotCreateTrigger() {
        Card commander = new TalrandSkySummoner();
        gd.makeCommander(player1.getId(), commander);
        harness.addToBattlefield(player1, new LoyalDrake());
        harness.setLibrary(player1, List.of(new LoyalDrake()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.stack).isEmpty();
        harness.addToBattlefield(player1, commander);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    void triggerStillDrawsAfterDrakeLeavesBattlefield() {
        Card commander = new TalrandSkySummoner();
        gd.makeCommander(player1.getId(), commander);
        harness.addToBattlefield(player1, commander);
        var drake = harness.addToBattlefieldAndReturn(player1, new LoyalDrake());
        harness.setLibrary(player1, List.of(new LoyalDrake()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(drake);
        gd.playerGraveyards.get(player1.getId()).add(drake.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.stack).isEmpty();
    }

    private void advanceToBeginningOfCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();
    }
}
