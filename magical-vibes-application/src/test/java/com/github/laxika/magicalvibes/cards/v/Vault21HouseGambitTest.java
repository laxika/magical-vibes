package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Vault21HouseGambit.class, GrizzlyBears.class, HillGiant.class, Mountain.class, Opt.class})
class Vault21HouseGambitTest extends BaseCardTest {

    @Test
    void chaptersIAndIIDiscardThenDraw() {
        Card discarded = new GrizzlyBears();
        Card drawn = new HillGiant();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));
        addSaga(0);

        triggerChapter();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
    }

    @Test
    void chapterIIIRevealsUpToFiveNonlandsAndCreatesTreasureForEachDuplicate() {
        Card bearsA = new GrizzlyBears();
        Card bearsB = new GrizzlyBears();
        Card giantsA = new HillGiant();
        Card giantsB = new HillGiant();
        Card unique = new Opt();
        Card land = new Mountain();
        Permanent saga = addSaga(2);
        harness.setHand(player1, List.of(bearsA, bearsB, giantsA, giantsB, unique, land));

        triggerChapter();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class);
        assertThat(choice.validCardIds()).containsExactly(
                bearsA.getId(), bearsB.getId(), giantsA.getId(), giantsB.getId(), unique.getId());
        harness.handleMultipleCardsChosen(player1,
                List.of(bearsA.getId(), bearsB.getId(), giantsA.getId(), giantsB.getId(), unique.getId()));

        assertThat(findPermanents(player1, "Treasure")).hasSize(4);
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(bearsA, bearsB, giantsA, giantsB, unique, land);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
    }

    private Permanent addSaga(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new Vault21HouseGambit());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void triggerChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}
