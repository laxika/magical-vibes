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
    @DisplayName("After gaining life, each opponent sacrifices a creature and a graveyard creature returns to hand")
    void sacrificesAndReturnsCreatureAfterLifeGain() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new WitchOfTheMoors());
        harness.addToBattlefield(player2, new GrizzlyBears());
        GrizzlyBears returned = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returned));
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(returned.getId()));

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(returned.getId()));
    }

    @Test
    @DisplayName("Does not trigger when its controller has not gained life")
    void doesNotTriggerWithoutLifeGain() {
        harness.addToBattlefield(player1, new WitchOfTheMoors());
        harness.addToBattlefield(player2, new GrizzlyBears());
        GrizzlyBears returned = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returned));

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(returned);
    }

    @Test
    @DisplayName("Up to one allows declining the graveyard return")
    void canDeclineGraveyardReturn() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new WitchOfTheMoors());
        harness.addToBattlefield(player2, new GrizzlyBears());
        GrizzlyBears returned = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returned));
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        advanceToEndStep(player1);
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
