package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SummonKujata.class, AirElemental.class, GrizzlyBears.class})
class SummonKujataTest extends BaseCardTest {

    @Test
    void chapterIDealsThreeDamageToUpToTwoTargetCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        addSagaWithLore(0);

        triggerChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(first.getId(), second.getId(), third.getId());
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(first.getMarkedDamage()).isEqualTo(3);
        assertThat(second.getMarkedDamage()).isEqualTo(3);
        assertThat(third.getMarkedDamage()).isZero();
    }

    @Test
    void chapterIIMakesUpToThreeCreaturesUnableToBlockThisTurn() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent fourth = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addSagaWithLore(1);

        triggerChapter();

        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.handlePermanentChosen(player1, third.getId());
        harness.passBothPriorities();

        assertThat(first.isCantBlockThisTurn()).isTrue();
        assertThat(second.isCantBlockThisTurn()).isTrue();
        assertThat(third.isCantBlockThisTurn()).isTrue();
        assertThat(fourth.isCantBlockThisTurn()).isFalse();
    }

    @Test
    void chapterIIIDiscardsDrawsAndDealsDiscardedManaValueToEachOpponent() {
        GrizzlyBears discarded = new GrizzlyBears();
        GrizzlyBears retained = new GrizzlyBears();
        GrizzlyBears firstDraw = new GrizzlyBears();
        GrizzlyBears secondDraw = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(discarded, retained)));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        addSagaWithLore(2);

        triggerChapter();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).contains(retained, firstDraw, secondDraw);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new SummonKujata());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void triggerChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
