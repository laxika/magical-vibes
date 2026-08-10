package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MindMaggotsTest extends BaseCardTest {

    @Test
    void discardsChosenCreatureCardsAndGetsTwoCountersPerCard() {
        harness.setHand(player1, List.of(new MindMaggots(), new GrizzlyBears(), new Mountain(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.XValueChoice countChoice =
                gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(countChoice).isNotNull();
        assertThat(countChoice.maxValue()).isEqualTo(2);

        harness.handleXValueChosen(player1, 2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 1);

        Permanent maggots = findPermanent(player1, "Mind Maggots");
        assertThat(maggots.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Mountain"))
                .hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Grizzly Bears"))
                .hasSize(2);
    }

    @Test
    void choosingZeroDiscardsNoCardsAndAddsNoCounters() {
        harness.setHand(player1, List.of(new MindMaggots(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);

        Permanent maggots = findPermanent(player1, "Mind Maggots");
        assertThat(maggots.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Grizzly Bears"))
                .hasSize(1);
    }

    @Test
    void doesNotOfferNoncreatureCardsForDiscard() {
        harness.setHand(player1, List.of(new MindMaggots(), new Mountain()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanent(player1, "Mind Maggots")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
