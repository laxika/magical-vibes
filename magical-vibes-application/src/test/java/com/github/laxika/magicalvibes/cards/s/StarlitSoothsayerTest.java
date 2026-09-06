package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StarlitSoothsayer.class, GrizzlyBears.class})
class StarlitSoothsayerTest extends BaseCardTest {

    @Test
    @DisplayName("Surveils 1 at your end step after you gained life")
    void surveilsAfterLifeGain() {
        Card topCard = new GrizzlyBears();
        harness.addToBattlefield(player1, new StarlitSoothsayer());
        harness.setLibrary(player1, List.of(topCard));
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        advanceToEndStep();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Surveils 1 at your end step after you lost life")
    void surveilsAfterLifeLoss() {
        Card topCard = new GrizzlyBears();
        harness.addToBattlefield(player1, new StarlitSoothsayer());
        harness.setLibrary(player1, List.of(topCard));
        gd.lifeLostThisTurn.put(player1.getId(), 1);

        advanceToEndStep();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Does not surveil when its controller neither gained nor lost life")
    void doesNotSurveilWithoutLifeChange() {
        harness.addToBattlefield(player1, new StarlitSoothsayer());

        advanceToEndStep();

        assertThat(gd.stack).isEmpty();
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
