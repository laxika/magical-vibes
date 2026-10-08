package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WitchOfTheMoors.class, GrizzlyBears.class, Swamp.class})
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

    @Test
    @DisplayName("An empty graveyard does not prevent the opponent's sacrifice")
    void sacrificesWithEmptyGraveyard() {
        harness.addToBattlefield(player1, new WitchOfTheMoors());
        harness.addToBattlefield(player2, new WitchOfTheMoors());
        harness.setGraveyard(player1, List.of());
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Witch of the Moors");
        harness.assertOnBattlefield(player1, "Witch of the Moors");
    }

    @Test
    @DisplayName("Returns the chosen creature even when the opponent has no creatures")
    void returnsCreatureWithoutSacrifice() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new WitchOfTheMoors());
        WitchOfTheMoors returned = new WitchOfTheMoors();
        harness.setGraveyard(player1, List.of(returned));
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        advanceToEndStep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(returned.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Witch of the Moors");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger during the opponent's end step")
    void doesNotTriggerOnOpponentEndStep() {
        harness.addToBattlefield(player1, new WitchOfTheMoors());
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Witch of the Moors");
    }

    @Test
    @DisplayName("Life gained by the opponent does not enable the ability")
    void opponentLifeGainDoesNotEnableTrigger() {
        harness.addToBattlefield(player1, new WitchOfTheMoors());
        harness.addToBattlefield(player2, new WitchOfTheMoors());
        gd.lifeGainedThisTurn.put(player2.getId(), 1);

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Witch of the Moors");
    }

    @Test
    @DisplayName("Only creature cards in the controller's graveyard are offered as targets")
    void offersOnlyOwnGraveyardCreatures() {
        harness.addToBattlefield(player1, new WitchOfTheMoors());
        WitchOfTheMoors returned = new WitchOfTheMoors();
        Swamp land = new Swamp();
        harness.setGraveyard(player1, List.of(returned, land));
        harness.setGraveyard(player2, List.of(new WitchOfTheMoors()));
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        advanceToEndStep(player1);

        PendingInteraction.MultiGraveyardChoice choice =
                (PendingInteraction.MultiGraveyardChoice) gd.interaction.activeInteraction();
        assertThat(choice.cards()).containsExactly(returned);
        harness.handleMultipleCardsChosen(player1, List.of(returned.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(land);
        harness.assertInHand(player1, "Witch of the Moors");
        harness.assertInGraveyard(player2, "Witch of the Moors");
    }

    @Test
    @DisplayName("Losing the sole graveyard target prevents the entire ability from resolving")
    void illegalTargetPreventsSacrifice() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new WitchOfTheMoors());
        harness.addToBattlefield(player2, new WitchOfTheMoors());
        WitchOfTheMoors returned = new WitchOfTheMoors();
        harness.setGraveyard(player1, List.of(returned));
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        advanceToEndStep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(returned.getId()));
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Witch of the Moors");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The opponent chooses which creature to sacrifice before the return completes")
    void opponentChoosesSacrifice() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new WitchOfTheMoors());
        WitchOfTheMoors kept = new WitchOfTheMoors();
        WitchOfTheMoors sacrificed = new WitchOfTheMoors();
        harness.addToBattlefield(player2, kept);
        harness.addToBattlefield(player2, sacrificed);
        WitchOfTheMoors returned = new WitchOfTheMoors();
        harness.setGraveyard(player1, List.of(returned));
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        advanceToEndStep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(returned.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.handleMultiplePermanentsChosen(player2, List.of(gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(sacrificed.getId()))
                .findFirst().orElseThrow().getId()));

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(sacrificed);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getId()).containsExactly(kept.getId());
        harness.assertInHand(player1, "Witch of the Moors");
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
