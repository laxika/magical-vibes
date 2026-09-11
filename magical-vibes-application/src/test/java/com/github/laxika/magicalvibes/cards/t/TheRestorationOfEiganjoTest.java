package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.ArchitectOfRestoration;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheRestorationOfEiganjo.class, ArchitectOfRestoration.class, Forest.class,
        GrizzlyBears.class, HillGiant.class, Plains.class, Shock.class})
class TheRestorationOfEiganjoTest extends BaseCardTest {

    @Test
    void chapterISearchesForBasicPlains() {
        addSagaWithLore(0);
        Plains plains = new Plains();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest, plains));

        advanceToNextChapter();

        PendingInteraction.LibrarySearch choice =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(choice.params().cards()).containsExactly(plains);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(plains);
    }

    @Test
    void chapterIIMayDiscardToReturnEligiblePermanentTapped() {
        addSagaWithLore(1);
        Shock discardedCard = new Shock();
        GrizzlyBears returnedCard = new GrizzlyBears();
        HillGiant ineligibleCard = new HillGiant();
        harness.setHand(player1, List.of(discardedCard));
        harness.setGraveyard(player1, List.of(returnedCard, ineligibleCard));

        advanceToNextChapter();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.cards()).extracting(Card::getId)
                .contains(returnedCard.getId()).doesNotContain(ineligibleCard.getId());
        harness.handleMultipleCardsChosen(player1, List.of(returnedCard.getId()));
        harness.passBothPriorities();

        Permanent returnedPermanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof GrizzlyBears)
                .findFirst()
                .orElseThrow();
        assertThat(returnedPermanent.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discardedCard);
    }

    @Test
    void chapterIIMayBeDeclined() {
        addSagaWithLore(1);
        GrizzlyBears returnedCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returnedCard));

        advanceToNextChapter();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard() instanceof GrizzlyBears);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(returnedCard);
    }

    @Test
    void chapterIIITransformsIntoArchitect() {
        Permanent saga = addSagaWithLore(2);

        advanceToNextChapter();

        Permanent architect = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof ArchitectOfRestoration)
                .findFirst()
                .orElseThrow();
        assertThat(architect).isNotSameAs(saga);
        assertThat(architect.isTransformed()).isTrue();
    }

    @Test
    void architectCreatesSpiritWhenAttacking() {
        addCreatureReady(player1, new ArchitectOfRestoration());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
    }

    @Test
    void architectCreatesSpiritWhenBlocking() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new ArchitectOfRestoration());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheRestorationOfEiganjo());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
