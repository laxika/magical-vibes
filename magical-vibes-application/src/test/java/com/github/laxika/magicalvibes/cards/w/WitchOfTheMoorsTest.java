package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WitchOfTheMoors.class, GrizzlyBears.class})
class WitchOfTheMoorsTest extends BaseCardTest {

    @Test
    @DisplayName("After you gain life, each opponent sacrifices a creature and you may return a creature card")
    void sacrificesEachOpponentAndReturnsCreature() {
        harness.addToBattlefield(player1, new WitchOfTheMoors());
        harness.addToBattlefield(player2, new GrizzlyBears());
        GrizzlyBears graveyardBears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(graveyardBears));
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        advanceToEndStep(player1);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(graveyardBears.getId());

        harness.handleMultipleCardsChosen(player1, List.of(graveyardBears.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The ability does not trigger if you did not gain life")
    void doesNotTriggerWithoutLifeGain() {
        harness.addToBattlefield(player1, new WitchOfTheMoors());
        harness.addToBattlefield(player2, new GrizzlyBears());

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The creature return is optional")
    void mayDeclineCreatureReturn() {
        harness.addToBattlefield(player1, new WitchOfTheMoors());
        harness.addToBattlefield(player2, new GrizzlyBears());
        GrizzlyBears graveyardBears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(graveyardBears));
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        advanceToEndStep(player1);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
