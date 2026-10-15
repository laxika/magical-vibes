package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeymourFlux.class, Forest.class})
class SeymourFluxTest extends BaseCardTest {

    @Test
    void payingLifeDrawsAndPutsCounterOnSeymourFlux() {
        Permanent seymour = harness.addToBattlefieldAndReturn(player1, new SeymourFlux());
        Forest drawnCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        int lifeBefore = gd.getLife(player1.getId());

        triggerUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(seymour.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void decliningLifePaymentDoesNothing() {
        Permanent seymour = harness.addToBattlefieldAndReturn(player1, new SeymourFlux());
        Forest topCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(topCard));
        int lifeBefore = gd.getLife(player1.getId());

        triggerUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(seymour.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new SeymourFlux());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        triggerUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void drawsEvenWhenSeymourLeavesBeforeResolution() {
        Permanent seymour = harness.addToBattlefieldAndReturn(player1, new SeymourFlux());
        Forest drawnCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        int lifeBefore = gd.getLife(player1.getId());

        triggerUpkeep(player1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, seymour));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard, seymour.getCard());
        harness.assertNotOnBattlefield(player1, "Seymour Flux");
    }

    @Test
    void secondPlayerPaysAndDrawsDuringTheirOwnUpkeep() {
        Permanent seymour = harness.addToBattlefieldAndReturn(player2, new SeymourFlux());
        Forest drawnCard = new Forest();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(drawnCard));
        int lifeBefore = gd.getLife(player2.getId());
        int opponentLifeBefore = gd.getLife(player1.getId());

        triggerUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(opponentLifeBefore);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
        assertThat(seymour.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void successiveUpkeepsAccumulateCountersAndDrawOneCardEach() {
        Permanent seymour = harness.addToBattlefieldAndReturn(player1, new SeymourFlux());
        Forest firstCard = new Forest();
        Forest secondCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(firstCard, secondCard));
        int lifeBefore = gd.getLife(player1.getId());

        triggerUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        triggerUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstCard, secondCard);
        assertThat(seymour.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void triggerUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.passUntil(activePlayer, TurnStep.UPKEEP);
    }
}
