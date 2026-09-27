package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NaktamunShinesAgain.class, GrizzlyBears.class, HillGiant.class})
class NaktamunShinesAgainTest extends BaseCardTest {

    @Test
    void chapterIPerpetuallyBoostsOwnedLowManaValueCreatureCards() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addSaga(0);

        triggerChapter();

        assertThat(bears.getEffectivePower()).isEqualTo(3);
        assertThat(gd.perpetualCardPowerModifiers).containsEntry(bears.getCard().getId(), 1);

        bears.resetModifiers();
        assertThat(bears.getEffectivePower()).isEqualTo(3);
    }

    @Test
    void chapterIISeeksRandomMatchingCreatureOntoTheBattlefield() {
        harness.setLibrary(player1, List.of(new HillGiant(), new GrizzlyBears()));
        addSaga(1);

        triggerChapter();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Hill Giant");
        assertThat(gd.playersWhoSearchedLibraryThisTurn).doesNotContain(player1.getId());
    }

    @Test
    void chapterIIIGrantsFlyingToYourLowManaValueCreaturesUntilEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        addSaga(2);

        triggerChapter();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, giant, Keyword.FLYING)).isFalse();
    }

    private Permanent addSaga(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new NaktamunShinesAgain());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void triggerChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
