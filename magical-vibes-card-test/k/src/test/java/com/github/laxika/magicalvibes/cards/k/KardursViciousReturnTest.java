package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FearlessPup;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KardursViciousReturn.class, Forest.class, FearlessPup.class})
class KardursViciousReturnTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I may sacrifice a creature to deal 3 damage to any target")
    void chapterIMaySacrificeCreatureForDamage() {
        Permanent saga = addSaga(0);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FearlessPup());
        int opponentLife = gd.getLife(player2.getId());

        advanceToChapter();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife - 3);
        harness.assertNotOnBattlefield(player1, "Fearless Pup");
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Chapter II makes each player discard a card")
    void chapterIIMakesEachPlayerDiscard() {
        addSaga(1);
        harness.setHand(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new FearlessPup()));

        advanceToChapter();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player2, "Fearless Pup");
    }

    @Test
    @DisplayName("Chapter III returns a targeted creature with a counter and haste until the next turn")
    void chapterIIIReturnsCreatureWithCounterAndHasteUntilNextTurn() {
        addSaga(2);
        FearlessPup creatureCard = new FearlessPup();
        harness.setGraveyard(player1, List.of(creatureCard));

        advanceToChapter();
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(creatureCard.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creatureCard.getId()));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Fearless Pup");
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isTrue();
        harness.passUntil(player1, TurnStep.UPKEEP);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isFalse();
    }

    private Permanent addSaga(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new KardursViciousReturn());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Chapter I can be declined without sacrificing or dealing damage")
    void chapterICanBeDeclined() {
        addSaga(0);
        harness.addToBattlefield(player1, new FearlessPup());
        int opponentLife = gd.getLife(player2.getId());

        advanceToChapter();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Fearless Pup");
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Chapter II keeps the first choice hidden until everyone has chosen")
    void chapterIIDiscardsOnlyAfterAllPlayersChoose() {
        addSaga(1);
        Forest firstChoice = new Forest();
        Forest secondChoice = new Forest();
        harness.setHand(player1, List.of(firstChoice));
        harness.setHand(player2, List.of(secondChoice));

        advanceToChapter();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.assertNotInGraveyard(player1, "Forest");
        harness.assertNotInGraveyard(player2, "Forest");

        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Chapter III targets only creature cards in its controller's graveyard")
    void chapterIIITargetsOnlyOwnCreatureCards() {
        addSaga(2);
        FearlessPup ownCreature = new FearlessPup();
        harness.setGraveyard(player1, List.of(ownCreature, new Forest()));
        harness.setGraveyard(player2, List.of(new FearlessPup()));

        advanceToChapter();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(ownCreature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ownCreature.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fearless Pup");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player2, "Fearless Pup");
        harness.assertNotOnBattlefield(player1, "Kardur's Vicious Return");
    }
}
