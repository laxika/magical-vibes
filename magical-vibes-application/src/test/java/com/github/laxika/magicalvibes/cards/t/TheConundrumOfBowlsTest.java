package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
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

@CardUsed({TheConundrumOfBowls.class, GrizzlyBears.class, HillGiant.class, Opt.class})
class TheConundrumOfBowlsTest extends BaseCardTest {

    @Test
    void chapterISeeksCardWithManaValueLessThanHandSize() {
        Card sought = new Opt();
        Card equal = new GrizzlyBears();
        Card greater = new HillGiant();
        harness.setHand(player1, List.of(new Opt(), new Opt()));
        harness.setLibrary(player1, List.of(equal, sought, greater));
        addSaga(0);

        triggerChapter();

        assertThat(gd.playerHands.get(player1.getId())).contains(sought);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(equal, greater);
    }

    @Test
    void chapterIISeeksCardWithManaValueGreaterThanHandSize() {
        Card lower = new Opt();
        Card equal = new GrizzlyBears();
        Card greater = new HillGiant();
        harness.setHand(player1, List.of(new Opt(), new Opt()));
        harness.setLibrary(player1, List.of(lower, equal, greater));
        addSaga(1);

        triggerChapter();

        assertThat(gd.playerHands.get(player1.getId())).contains(greater);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(lower, equal);
    }

    @Test
    void chapterIIIOffersSpellWithManaValueEqualToHandSizeForFree() {
        Card equal = new GrizzlyBears();
        Card greater = new HillGiant();
        harness.setHand(player1, List.of(equal, greater));
        addSaga(2);

        triggerChapter();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(equal.getId()));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(greater);
    }

    private Permanent addSaga(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheConundrumOfBowls());
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
