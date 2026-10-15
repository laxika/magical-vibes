package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RegalBloodlord.class, Revitalize.class})
class RegalBloodlordTest extends BaseCardTest {

    private void advanceToEndStep(Player activePlayer) {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.forceActivePlayer(activePlayer);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }

    @Test
    @DisplayName("Creates a 1/1 black flying Bat token at end step if you gained life this turn")
    void createsBatTokenWhenLifeGained() {
        harness.addToBattlefield(player1, new RegalBloodlord());
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        advanceToEndStep(player1);
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        harness.passBothPriorities();

        var bats = findPermanents(player1, "Bat");
        assertThat(bats).hasSize(1);
        assertThat(bats).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
        });
    }

    @Test
    @DisplayName("Creates no token at end step if you did not gain life this turn")
    void noTokenWithoutLifeGain() {
        harness.addToBattlefield(player1, new RegalBloodlord());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Bat")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Triggers on an opponent's end step when you gained life")
    void triggersOnOpponentEndStep() {
        harness.addToBattlefield(player1, new RegalBloodlord());
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        advanceToEndStep(player2);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Bat")).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger when only an opponent gained life")
    void opponentLifeGainDoesNotTrigger() {
        harness.addToBattlefield(player1, new RegalBloodlord());
        gd.lifeGainedThisTurn.put(player2.getId(), 1);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Bat")).isEmpty();
    }

    @Test
    @DisplayName("Life gained before Bloodlord enters still qualifies at the end step")
    void lifeGainedBeforeEnteringCounts() {
        harness.setLibrary(player1, List.of(new Revitalize()));
        harness.castFromHand(player1, new Revitalize(), "{1}{W}");
        harness.passBothPriorities();
        harness.assertLife(player1, 23);
        harness.addToBattlefield(player1, new RegalBloodlord());

        advanceToEndStep(player1);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Bat")).hasSize(1);
    }

    @Test
    @DisplayName("Gaining life after the end step begins does not trigger Bloodlord")
    void lifeGainedAfterEndStepBeginsIsTooLate() {
        harness.addToBattlefield(player1, new RegalBloodlord());
        harness.setLibrary(player1, List.of(new Revitalize()));
        advanceToEndStep(player1);
        assertThat(gd.stack).isEmpty();

        harness.castFromHand(player1, new Revitalize(), "{1}{W}");
        harness.passBothPriorities();
        harness.assertLife(player1, 23);

        assertThat(findPermanents(player1, "Bat")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Multiple life gains create only one Bat for each Bloodlord")
    void multipleLifeGainsStillCreateOneBatPerBloodlord() {
        harness.addToBattlefield(player1, new RegalBloodlord());
        harness.addToBattlefield(player1, new RegalBloodlord());
        harness.setLibrary(player1, List.of(new Revitalize(), new Revitalize()));
        harness.castFromHand(player1, new Revitalize(), "{1}{W}");
        harness.passBothPriorities();
        harness.castFromHand(player1, new Revitalize(), "{1}{W}");
        harness.passBothPriorities();
        harness.assertLife(player1, 26);

        advanceToEndStep(player1);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Bat")).hasSize(2);
    }

    @Test
    @DisplayName("The end-step ability resolves even after Bloodlord leaves the battlefield")
    void triggerSurvivesSourceLeaving() {
        harness.addToBattlefield(player1, new RegalBloodlord());
        gd.lifeGainedThisTurn.put(player1.getId(), 1);
        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).clear();

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Bat")).hasSize(1);
    }
}
