package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ToluzCleverConductor.class, GrizzlyBears.class, Mountain.class})
class ToluzCleverConductorTest extends BaseCardTest {

    @Test
    void entersConnivesAndExilesNonlandDiscardWithIt() {
        Card discarded = new GrizzlyBears();
        harness.setHand(player1, List.of(new ToluzCleverConductor(), new Mountain()));
        harness.setLibrary(player1, List.of(discarded));
        addToluzMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent toluz = findPermanent(player1, "Toluz, Clever Conductor");
        discardByName("Grizzly Bears");
        harness.passBothPriorities();

        assertThat(toluz.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.findExiledCard(discarded.getId()).sourcePermanentId()).isEqualTo(toluz.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(discarded.getId()));
    }

    @Test
    void deathReturnsCardsExiledWithToluzToTheirOwnersHand() {
        Card discarded = new GrizzlyBears();
        harness.setHand(player1, List.of(new ToluzCleverConductor(), new Mountain()));
        harness.setLibrary(player1, List.of(discarded));
        addToluzMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent toluz = findPermanent(player1, "Toluz, Clever Conductor");
        discardByName("Grizzly Bears");
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, toluz));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(discarded);
        assertThat(gd.getCardsExiledByPermanent(toluz.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Toluz, Clever Conductor");
    }

    @Test
    void landDiscardedDuringConniveDoesNotAddCounter() {
        harness.setHand(player1, List.of(new ToluzCleverConductor(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Mountain()));
        addToluzMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent toluz = findPermanent(player1, "Toluz, Clever Conductor");
        discardByName("Mountain");
        harness.passBothPriorities();

        assertThat(toluz.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.getCardsExiledByPermanent(toluz.getId())).hasSize(1);
    }

    @Test
    void simultaneousCleanupDiscardsAreExiledByOneTrigger() {
        Permanent toluz = harness.addToBattlefieldAndReturn(player1, new ToluzCleverConductor());
        Card first = new Mountain();
        Card second = new Mountain();
        harness.setHand(player1, List.of(first, second, new Mountain(), new Mountain(),
                new Mountain(), new Mountain(), new Mountain(), new Mountain(), new Mountain()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);

        harness.passUntil(player1, TurnStep.CLEANUP);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(toluz.getId()))
                .containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void opponentDiscardDoesNotTriggerToluz() {
        Permanent toluz = harness.addToBattlefieldAndReturn(player1, new ToluzCleverConductor());
        Card discarded = new Mountain();
        harness.setHand(player2, List.of(discarded, new Mountain(), new Mountain(), new Mountain(),
                new Mountain(), new Mountain(), new Mountain(), new Mountain()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        harness.passUntil(player2, TurnStep.CLEANUP);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discarded);
        assertThat(gd.getCardsExiledByPermanent(toluz.getId())).isEmpty();
    }

    @Test
    void conniveStillDrawsAndDiscardsAfterToluzDies() {
        Card drawn = new Mountain();
        harness.setHand(player1, List.of(new ToluzCleverConductor()));
        harness.setLibrary(player1, List.of(drawn));
        addToluzMana();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent toluz = findPermanent(player1, "Toluz, Clever Conductor");

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, toluz));
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawn);
        assertThat(gd.getCardsExiledByPermanent(toluz.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void exilingToluzDoesNotReturnItsExiledCards() {
        Card discarded = new Mountain();
        harness.setHand(player1, List.of(new ToluzCleverConductor()));
        harness.setLibrary(player1, List.of(discarded));
        addToluzMana();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent toluz = findPermanent(player1, "Toluz, Clever Conductor");
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, toluz));

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(discarded);
        assertThat(gd.findExiledCard(discarded.getId()).sourcePermanentId()).isEqualTo(toluz.getId());
        assertThat(gd.stack).isEmpty();
    }

    private void addToluzMana() {
        harness.addMana(player1, ManaColor.BLUE, 3);
    }

    private void discardByName(String cardName) {
        List<Card> hand = gd.playerHands.get(player1.getId());
        int index = -1;
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).getName().equals(cardName)) {
                index = i;
                break;
            }
        }
        assertThat(index).as("card '%s' is in hand", cardName).isGreaterThanOrEqualTo(0);
        harness.handleCardChosen(player1, index);
    }
}
