package com.github.laxika.magicalvibes.cards.t;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.laxika.magicalvibes.cards.f.FaerieInvaders;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

@CardUsed({TalionsMessenger.class, FaerieInvaders.class, Forest.class, GrizzlyBears.class})
class TalionsMessengerTest extends BaseCardTest {

    @Test
    void faerieAttackDrawsThenDiscardsAndPutsCounterOnTargetFaerie() {
        addCreatureReady(player1, new TalionsMessenger());
        Permanent faerie = addCreatureReady(player1, new FaerieInvaders());
        Forest drawn = new Forest();
        GrizzlyBears discarded = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(discarded));

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, faerie.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(faerie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void nonFaerieAttackDoesNotDrawOrDiscard() {
        addCreatureReady(player1, new TalionsMessenger());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Forest libraryCard = new Forest();
        GrizzlyBears handCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(handCard));

        declareAttackers(player1, List.of(1));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void reflexiveTargetMustBeAFaerieYouControl() {
        addCreatureReady(player1, new TalionsMessenger());
        addCreatureReady(player1, new FaerieInvaders());
        Permanent opponentFaerie = addCreatureReady(player2, new FaerieInvaders());
        Forest drawn = new Forest();
        GrizzlyBears discarded = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(discarded));

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentFaerie.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(opponentFaerie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
