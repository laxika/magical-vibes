package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.r.RhysticCave;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AugustaOrderReturned.class, GrizzlyBears.class, HillGiant.class,
        Abolish.class, RhysticCave.class})
class AugustaOrderReturnedTest extends BaseCardTest {

    @Test
    void exilesOneCardFromEachGraveyardAndCountsOnlyNonlands() {
        Permanent augusta = addCreatureReady(player1, new AugustaOrderReturned());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonattacker = addCreatureReady(player1, new HillGiant());

        Abolish ownSpell = new Abolish();
        RhysticCave ownLand = new RhysticCave();
        Abolish opponentSpell = new Abolish();
        RhysticCave opponentLand = new RhysticCave();
        harness.setGraveyard(player1, List.of(ownSpell, ownLand));
        harness.setGraveyard(player2, List.of(opponentSpell, opponentLand));

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(ownSpell.getId(), ownLand.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ownSpell.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(opponentSpell.getId(), opponentLand.getId());
        harness.handleMultipleCardsChosen(player2, List.of(opponentSpell.getId()));

        PendingInteraction.PermanentChoice targetChoice = gd.interaction
                .activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validIds()).contains(augusta.getId(), attacker.getId())
                .doesNotContain(nonattacker.getId());
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(ownSpell);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(opponentSpell);
        harness.assertInGraveyard(player1, "Rhystic Cave");
        harness.assertInGraveyard(player2, "Rhystic Cave");
    }

    @Test
    void doesNotQueueTheCounterAbilityWhenOnlyLandsAreExiled() {
        addCreatureReady(player1, new AugustaOrderReturned());
        RhysticCave ownLand = new RhysticCave();
        RhysticCave opponentLand = new RhysticCave();
        harness.setGraveyard(player1, List.of(ownLand));
        harness.setGraveyard(player2, List.of(opponentLand));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(ownLand);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(opponentLand);
    }

    @Test
    void putsOnlyOneCounterWhenOneLandAndOneNonlandAreExiled() {
        Permanent augusta = addCreatureReady(player1, new AugustaOrderReturned());
        AugustaOrderReturned ownCard = new AugustaOrderReturned();
        RhysticCave opponentLand = new RhysticCave();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentLand));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, augusta.getId());
        harness.passBothPriorities();

        assertThat(augusta.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(ownCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(opponentLand);
    }

    @Test
    void waitsForEveryPlayersChoiceBeforeExilingAnyCard() {
        Permanent augusta = addCreatureReady(player1, new AugustaOrderReturned());
        AugustaOrderReturned ownCard = new AugustaOrderReturned();
        AugustaOrderReturned ownOtherCard = new AugustaOrderReturned();
        AugustaOrderReturned opponentCard = new AugustaOrderReturned();
        AugustaOrderReturned opponentOtherCard = new AugustaOrderReturned();
        harness.setGraveyard(player1, List.of(ownCard, ownOtherCard));
        harness.setGraveyard(player2, List.of(opponentCard, opponentOtherCard));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(ownCard.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(opponentCard.getId(), opponentOtherCard.getId());
        boolean ownCardStillInGraveyard = gd.playerGraveyards.get(player1.getId()).contains(ownCard);
        boolean ownExileStillEmpty = gd.getPlayerExiledCards(player1.getId()).isEmpty();

        harness.handleMultipleCardsChosen(player2, List.of(opponentCard.getId()));
        harness.handlePermanentChosen(player1, augusta.getId());
        harness.passBothPriorities();

        assertThat(augusta.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(ownCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(opponentCard);
        assertThat(ownCardStillInGraveyard).isTrue();
        assertThat(ownExileStillEmpty).isTrue();
    }

    @Test
    void countsOpponentNonlandWhenControllersGraveyardIsEmpty() {
        Permanent augusta = addCreatureReady(player1, new AugustaOrderReturned());
        AugustaOrderReturned opponentCard = new AugustaOrderReturned();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(opponentCard));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, augusta.getId());
        harness.passBothPriorities();

        assertThat(augusta.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void doesNotQueueCounterAbilityWhenBothGraveyardsAreEmpty() {
        Permanent augusta = addCreatureReady(player1, new AugustaOrderReturned());
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(augusta.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }
}
