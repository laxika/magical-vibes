package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RendSpirit;
import com.github.laxika.magicalvibes.cards.y.YoseiTheMorningStar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SavorTheMoment.class, SafeholdSentry.class, YoseiTheMorningStar.class, RendSpirit.class,
        SphinxOfTheSecondSun.class})
class SavorTheMomentTest extends BaseCardTest {

    private void advanceTurn(Player activePlayer) {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passUntil(activePlayer, TurnStep.PRECOMBAT_MAIN);
    }

    private void cast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new SavorTheMoment(), "{1}{U}{U}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Resolving queues one extra turn for the caster")
    void resolvingQueuesOneExtraTurn() {
        cast();

        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.extraTurns).containsExactly(player1.getId());
    }

    @Test
    @DisplayName("The extra turn is taken by the caster after the current turn ends")
    void extraTurnTakenByCaster() {
        int turnBefore = gd.turnNumber;
        cast();

        advanceTurn(player1);

        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.turnNumber).isEqualTo(turnBefore + 1);
        assertThat(gd.extraTurns).isEmpty();
    }

    @Test
    @DisplayName("Permanents stay tapped on the extra turn — its untap step is skipped")
    void untapStepSkippedOnExtraTurn() {
        Permanent sentry = addCreatureReady(player1, new SafeholdSentry());
        sentry.tap();
        cast();

        advanceTurn(player1); // into the extra turn

        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(sentry.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Summoning sickness still clears on the skipped extra turn so creatures can act")
    void summoningSicknessStillClears() {
        Permanent sentry = harness.addToBattlefieldAndReturn(player1, new SafeholdSentry());
        sentry.setSummoningSick(true);
        cast();

        advanceTurn(player1); // into the extra turn

        assertThat(sentry.isSummoningSick()).isFalse();
    }

    @Test
    @DisplayName("Normal turn order and untapping resume after the extra turn")
    void normalTurnOrderResumes() {
        Permanent sentry = addCreatureReady(player1, new SafeholdSentry());
        sentry.tap();
        int turnBefore = gd.turnNumber;
        cast();

        advanceTurn(player1); // extra turn (untap skipped)
        advanceTurn(player2); // opponent's normal turn
        advanceTurn(player1); // player's next normal turn

        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.turnNumber).isEqualTo(turnBefore + 3);
        assertThat(sentry.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Each of two consecutive extra turns skips its own untap step")
    void consecutiveExtraTurnsEachSkipUntap() {
        Permanent sentry = addCreatureReady(player1, new SafeholdSentry());
        sentry.tap();
        cast();
        cast();
        int turnBefore = gd.turnNumber;

        advanceTurn(player1);
        assertThat(gd.turnNumber).isEqualTo(turnBefore + 1);
        assertThat(sentry.isTapped()).isTrue();

        advanceTurn(player1);
        assertThat(gd.turnNumber).isEqualTo(turnBefore + 2);
        assertThat(sentry.isTapped()).isTrue();

        advanceTurn(player2);
        advanceTurn(player1);
        assertThat(gd.turnNumber).isEqualTo(turnBefore + 4);
        assertThat(sentry.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Skipping untap still allows the extra turn's draw step")
    void extraTurnStillDrawsCard() {
        SafeholdSentry drawnCard = new SafeholdSentry();
        harness.setLibrary(player1, List.of(drawnCard));
        cast();

        advanceTurn(player1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Yosei's next-untap skip survives Savor the Moment's already skipped step")
    void nextUntapSkipWaitsUntilAfterExtraTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent sentry = addCreatureReady(player1, new SafeholdSentry());
        Permanent yosei = harness.addToBattlefieldAndReturn(player1, new YoseiTheMorningStar());
        harness.setHand(player1, List.of(new RendSpirit()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, yosei.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handlePermanentChosen(player1, sentry.getId());
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);
        assertThat(sentry.isTapped()).isTrue();
        cast();

        advanceTurn(player1);
        assertThat(sentry.isTapped()).isTrue();

        advanceTurn(player2);
        advanceTurn(player1);
        assertThat(sentry.isTapped()).isTrue();

        advanceTurn(player2);
        advanceTurn(player1);
        assertThat(sentry.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Additional beginning phases during the extra turn also skip untapping")
    void additionalBeginningPhaseAlsoSkipsUntap() {
        Permanent sentry = addCreatureReady(player1, new SafeholdSentry());
        sentry.tap();
        harness.addToBattlefield(player1, new SphinxOfTheSecondSun());
        harness.setLibrary(player1, List.of(new SafeholdSentry(), new SafeholdSentry()));
        cast();

        advanceTurn(player1);
        assertThat(sentry.isTapped()).isTrue();
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);

        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(sentry.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }
}
