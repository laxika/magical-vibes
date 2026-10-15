package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AjanisMantra.class})
class AjanisMantraTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger may gain 1 life")
    void upkeepTriggerMayGainLife() {
        harness.addToBattlefield(player1, new AjanisMantra());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.passUntil(TurnStep.UPKEEP);
        harness.passBothPriorities(); // resolve triggered ability → MayEffect prompts
        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Upkeep trigger can be declined")
    void upkeepTriggerCanBeDeclined() {
        harness.addToBattlefield(player1, new AjanisMantra());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.passUntil(TurnStep.UPKEEP);
        harness.passBothPriorities(); // resolve triggered ability → MayEffect prompts
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not trigger on opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new AjanisMantra());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Multiple upkeeps accumulate life gain")
    void multipleUpkeepsAccumulateLifeGain() {
        harness.addToBattlefield(player1, new AjanisMantra());

        // First upkeep
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.passUntil(TurnStep.UPKEEP);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);

        // Second upkeep
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.passUntil(TurnStep.UPKEEP);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Each Mantra offers an independent life gain choice")
    void multipleCopiesOfferIndependentChoices() {
        harness.addToBattlefield(player1, new AjanisMantra());
        harness.addToBattlefield(player1, new AjanisMantra());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gd.stack).hasSize(2);
        harness.assertLife(player1, 20);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.assertLife(player1, 20);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only the active player's Mantra triggers and gains life")
    void eachPlayersMantraTriggersOnlyOnTheirOwnUpkeep() {
        harness.addToBattlefield(player1, new AjanisMantra());
        harness.addToBattlefield(player2, new AjanisMantra());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 21);
        assertThat(gd.stack).isEmpty();
    }
}
