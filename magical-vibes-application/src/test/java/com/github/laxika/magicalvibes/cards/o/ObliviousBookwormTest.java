package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AinokTracker;
import com.github.laxika.magicalvibes.cards.c.CanyonLurkers;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ObliviousBookworm.class, AinokTracker.class, CanyonLurkers.class, GrizzlyBears.class})
class ObliviousBookwormTest extends BaseCardTest {

    @Test
    void acceptingDrawsThenDiscardsWithoutFaceDownActivity() {
        harness.addToBattlefield(player1, new ObliviousBookworm());
        Card drawn = new GrizzlyBears();
        Card discarded = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(discarded));

        resolveBookwormMay();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
    }

    @Test
    void doesNotDiscardAfterFaceDownPermanentEnteredThisTurn() {
        harness.addToBattlefield(player1, new ObliviousBookworm());
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new CanyonLurkers()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();

        resolveBookwormMay();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(drawn);
    }

    @Test
    void doesNotDiscardAfterPermanentTurnsFaceUpThisTurn() {
        harness.addToBattlefield(player1, new ObliviousBookworm());
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of());
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new AinokTracker());
        permanent.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        gs.turnPermanentFaceUpWithoutPayingManaCost(gd, permanent);
        harness.passBothPriorities();

        resolveBookwormMay();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void decliningDoesNotDraw() {
        harness.addToBattlefield(player1, new ObliviousBookworm());
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));

        advanceToBookwormMay();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawn);
    }

    private void resolveBookwormMay() {
        advanceToBookwormMay();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
    }

    private void advanceToBookwormMay() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
