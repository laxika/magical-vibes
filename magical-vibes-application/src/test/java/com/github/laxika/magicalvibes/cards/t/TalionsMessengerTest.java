package com.github.laxika.magicalvibes.cards.t;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.laxika.magicalvibes.cards.b.BarrowNaughty;
import com.github.laxika.magicalvibes.cards.c.CandyGrapple;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mintstrosity;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

@CardUsed({TalionsMessenger.class, BarrowNaughty.class, Forest.class, Mintstrosity.class, CandyGrapple.class})
class TalionsMessengerTest extends BaseCardTest {

    @Test
    void faerieAttackDrawsThenDiscardsAndPutsCounterOnTargetFaerie() {
        addCreatureReady(player1, new TalionsMessenger());
        Permanent faerie = addCreatureReady(player1, new BarrowNaughty());
        Forest drawn = new Forest();
        Mintstrosity discarded = new Mintstrosity();
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
        Permanent nonFaerie = addCreatureReady(player1, new Mintstrosity());
        Forest libraryCard = new Forest();
        Mintstrosity handCard = new Mintstrosity();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(handCard));

        declareAttackers(player1, List.of(1));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(nonFaerie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void reflexiveTargetMustBeAFaerieYouControl() {
        addCreatureReady(player1, new TalionsMessenger());
        addCreatureReady(player1, new BarrowNaughty());
        Permanent opponentFaerie = addCreatureReady(player2, new BarrowNaughty());
        Forest drawn = new Forest();
        Mintstrosity discarded = new Mintstrosity();
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

    @Test
    void multipleAttackingFaeriesTriggerOnlyOneLootAndCounter() {
        Permanent messenger = addCreatureReady(player1, new TalionsMessenger());
        Permanent faerie = addCreatureReady(player1, new BarrowNaughty());
        Forest drawn = new Forest();
        Forest remaining = new Forest();
        Mintstrosity discarded = new Mintstrosity();
        harness.setLibrary(player1, List.of(drawn, remaining));
        harness.setHand(player1, List.of(discarded));

        declareAttackers(player1, List.of(0, 1));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handlePermanentChosen(player1, messenger.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(messenger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(faerie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void messengerCanAttackAloneAndDiscardTheCardJustDrawn() {
        Permanent messenger = addCreatureReady(player1, new TalionsMessenger());
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handlePermanentChosen(player1, messenger.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(drawn);
        assertThat(messenger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void reflexiveTargetCannotBeANonFaerieYouControl() {
        Permanent messenger = addCreatureReady(player1, new TalionsMessenger());
        Permanent nonFaerie = addCreatureReady(player1, new Mintstrosity());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new Forest()));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonFaerie.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, messenger.getId());
        harness.passBothPriorities();
        assertThat(nonFaerie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(messenger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void opponentsFaerieAttackDoesNotTriggerMessenger() {
        Permanent messenger = addCreatureReady(player1, new TalionsMessenger());
        addCreatureReady(player2, new BarrowNaughty());
        Forest libraryCard = new Forest();
        Mintstrosity handCard = new Mintstrosity();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(handCard));

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(messenger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void lootStillResolvesWhenTheOnlyFaerieDiesBeforeTheTriggerResolves() {
        Permanent messenger = addCreatureReady(player1, new TalionsMessenger());
        Forest drawn = new Forest();
        Forest discarded = new Forest();
        CandyGrapple removal = new CandyGrapple();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(discarded));
        harness.setHand(player2, List.of(removal));
        harness.addMana(player2, ManaColor.BLACK, 2);

        declareAttackers(player1, List.of(0));
        harness.castInstant(player2, 0, messenger.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(messenger.getCard(), discarded);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void playersCanRespondToTheReflexiveTriggerByRemovingItsTarget() {
        Permanent messenger = addCreatureReady(player1, new TalionsMessenger());
        Permanent faerie = addCreatureReady(player1, new BarrowNaughty());
        Forest drawn = new Forest();
        Mintstrosity discarded = new Mintstrosity();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(discarded));
        harness.setHand(player2, List.of(new CandyGrapple()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handlePermanentChosen(player1, faerie.getId());

        assertThat(faerie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.castInstant(player2, 0, faerie.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(messenger);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded, faerie.getCard());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(messenger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(faerie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
