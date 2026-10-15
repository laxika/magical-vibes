package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StarlitSoothsayer.class})
class StarlitSoothsayerTest extends BaseCardTest {

    @Test
    @DisplayName("Surveils 1 at your end step after you gained life")
    void surveilsAfterLifeGain() {
        Card topCard = new StarlitSoothsayer();
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
        Card topCard = new StarlitSoothsayer();
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

    @Test
    @DisplayName("May keep the surveilled card on top of the library")
    void keepsSurveilledCard() {
        Card topCard = new StarlitSoothsayer();
        harness.addToBattlefield(player1, new StarlitSoothsayer());
        harness.setLibrary(player1, List.of(topCard));
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        advanceToEndStep();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("Gaining and losing life causes only one surveil")
    void gainingAndLosingLifeTriggersOnce() {
        Card topCard = new StarlitSoothsayer();
        Card nextCard = new StarlitSoothsayer();
        harness.addToBattlefield(player1, new StarlitSoothsayer());
        harness.setLibrary(player1, List.of(topCard, nextCard));
        gd.lifeGainedThisTurn.put(player1.getId(), 1);
        gd.lifeLostThisTurn.put(player1.getId(), 1);

        advanceToEndStep();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
    }

    @Test
    @DisplayName("Opponent life changes do not satisfy the condition")
    void opponentLifeChangesDoNotTrigger() {
        harness.addToBattlefield(player1, new StarlitSoothsayer());
        gd.lifeGainedThisTurn.put(player2.getId(), 1);
        gd.lifeLostThisTurn.put(player2.getId(), 1);

        advanceToEndStep();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger during an opponent's end step")
    void doesNotTriggerOnOpponentEndStep() {
        harness.addToBattlefield(player2, new StarlitSoothsayer());
        gd.lifeGainedThisTurn.put(player2.getId(), 1);

        advanceToEndStep();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Surveilling an empty library does not cause a loss")
    void surveilsEmptyLibrary() {
        harness.addToBattlefield(player1, new StarlitSoothsayer());
        harness.setLibrary(player1, List.of());
        gd.lifeLostThisTurn.put(player1.getId(), 1);

        advanceToEndStep();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.status).isEqualTo(com.github.laxika.magicalvibes.model.GameStatus.RUNNING);
    }
    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
    }
}
