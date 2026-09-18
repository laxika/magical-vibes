package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CurseOfChaos.class, Forest.class, GrizzlyBears.class, JaceBeleren.class})
class CurseOfChaosTest extends BaseCardTest {

    @Test
    @DisplayName("The attacking player may discard a card to draw a card")
    void attackingPlayerMayDiscardToDraw() {
        Forest discarded = new Forest();
        GrizzlyBears drawn = new GrizzlyBears();
        placeCurseOnPlayer1();
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player2, new ArrayList<>(List.of(discarded)));
        harness.setLibrary(player2, List.of(drawn));

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            harness.handleMayAbilityChosen(player2, true);
            harness.handleCardChosen(player2, 0);
        });
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("The attacking player may decline to discard")
    void attackingPlayerMayDecline() {
        Forest discarded = new Forest();
        GrizzlyBears drawn = new GrizzlyBears();
        placeCurseOnPlayer1();
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player2, new ArrayList<>(List.of(discarded)));
        harness.setLibrary(player2, List.of(drawn));

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handleMayAbilityChosen(player2, false));

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(discarded);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Attacking another player does not trigger")
    void attackingAnotherPlayerDoesNotTrigger() {
        placeCurseOnPlayer1();
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new Forest()));

        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player2, List.of(0), java.util.Map.of(0, planeswalker.getId()));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void placeCurseOnPlayer1() {
        Permanent curse = harness.addToBattlefieldAndReturn(player1, new CurseOfChaos());
        curse.setAttachedTo(player1.getId());
    }
}
