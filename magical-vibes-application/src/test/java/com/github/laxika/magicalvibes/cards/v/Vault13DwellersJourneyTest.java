package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Vault13DwellersJourney.class, GrizzlyBears.class})
class Vault13DwellersJourneyTest extends BaseCardTest {

    @Test
    void chapterIExilesAtMostOneCreaturePerPlayer() {
        Permanent saga = addSagaWithLore(0);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        triggerChapter();

        harness.passBothPriorities();
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactlyInAnyOrder(ownCreature.getId(), opponentCreature.getId());
        assertThat(choice.validPermanentIds()).doesNotContain(saga.getId());

        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId())
                .containsExactlyInAnyOrder(ownCreature.getCard().getId(), opponentCreature.getCard().getId());
    }

    @Test
    void chapterIIGainsLifeAndScriesTwo() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLife(player1, 20);
        addSagaWithLore(1);

        triggerChapter();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
        harness.passBothPriorities();
    }

    @Test
    void chapterIIIReturnsTwoToOwnersAndBottomsTheRest() {
        Permanent saga = addSagaWithLore(2);
        GrizzlyBears ownReturned = new GrizzlyBears();
        GrizzlyBears opponentReturned = new GrizzlyBears();
        GrizzlyBears ownBottom = new GrizzlyBears();
        gd.addToExile(player1.getId(), ownReturned, saga.getId());
        gd.addToExile(player2.getId(), opponentReturned, saga.getId());
        gd.addToExile(player1.getId(), ownBottom, saga.getId());

        triggerChapter();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.minCount()).isEqualTo(2);
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultipleCardsChosen(player1,
                List.of(ownReturned.getId(), opponentReturned.getId()));
        harness.passBothPriorities();

        assertThat(findCardOnBattlefield(player1, ownReturned.getId())).isNotNull();
        assertThat(findCardOnBattlefield(player2, opponentReturned.getId())).isNotNull();
        assertThat(findCardOnBattlefield(player1, ownBottom.getId())).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(card -> card.getId())
                .contains(ownBottom.getId());
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new Vault13DwellersJourney());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void triggerChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent findCardOnBattlefield(com.github.laxika.magicalvibes.model.Player player,
                                            java.util.UUID cardId) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(cardId))
                .findFirst()
                .orElse(null);
    }
}
