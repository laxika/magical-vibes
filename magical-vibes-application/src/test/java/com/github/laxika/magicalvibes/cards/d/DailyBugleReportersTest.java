package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DailyBugleReporters.class, GrizzlyBears.class, HillGiant.class, Island.class})
class DailyBugleReportersTest extends BaseCardTest {

    @Test
    void puffPiecePutsCountersOnTwoTargetCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castWithPuffPiece(List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void puffPieceCannotTargetNoncreaturePermanent() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());

        assertThatThrownBy(() -> castWithPuffPiece(List.of(island.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void investigativeJournalismReturnsCreatureWithManaValueTwoOrLess() {
        Card expensiveCreature = new HillGiant();
        Card eligibleCreature = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(expensiveCreature, eligibleCreature)));

        harness.setHand(player1, List.of(new DailyBugleReporters()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, 1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(eligibleCreature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(eligibleCreature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Hill Giant");
    }

    private void castWithPuffPiece(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new DailyBugleReporters()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, targetIds);
    }
}
