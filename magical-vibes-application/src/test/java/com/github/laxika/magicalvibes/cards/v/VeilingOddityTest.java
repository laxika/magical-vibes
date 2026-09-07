package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VeilingOddity.class, GrizzlyBears.class})
class VeilingOddityTest extends BaseCardTest {

    @Test
    void suspendExilesWithFourTimeCounters() {
        VeilingOddity card = suspendCard();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
    }

    @Test
    void nonLastTimeCounterDoesNotMakeCreaturesUnblockable() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        suspendCard();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).containsValue(3);
        assertThat(gqs.hasCantBeBlocked(gd, ownCreature)).isFalse();
        assertThat(gqs.hasCantBeBlocked(gd, opposingCreature)).isFalse();
    }

    @Test
    void lastTimeCounterMakesAllCreaturesUnblockableUntilEndOfTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        suspendCard();

        for (int i = 0; i < 4; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }

        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gqs.hasCantBeBlocked(gd, ownCreature)).isTrue();
        assertThat(gqs.hasCantBeBlocked(gd, opposingCreature)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasCantBeBlocked(gd, ownCreature)).isFalse();
        assertThat(gqs.hasCantBeBlocked(gd, opposingCreature)).isFalse();
    }

    private VeilingOddity suspendCard() {
        VeilingOddity card = new VeilingOddity();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, null);
        return card;
    }
}
