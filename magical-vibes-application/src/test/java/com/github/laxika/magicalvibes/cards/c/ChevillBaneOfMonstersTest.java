package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChevillBaneOfMonsters.class, Forest.class, GrizzlyBears.class})
class ChevillBaneOfMonstersTest extends BaseCardTest {

    @Test
    void putsBountyCounterOnTargetOpponentCreatureAtUpkeep() {
        harness.addToBattlefield(player1, new ChevillBaneOfMonsters());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.BOUNTY)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerIfAnOpponentHasABountyCounter() {
        harness.addToBattlefield(player1, new ChevillBaneOfMonsters());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.BOUNTY, 1);

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void triggersForBountiedOpponentPermanentAndDrawsACard() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new ChevillBaneOfMonsters());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        forest.setCounterCount(CounterType.BOUNTY, 1);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, forest));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotTriggerForUnbountiedOrOwnPermanents() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new ChevillBaneOfMonsters());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        ownBears.setCounterCount(CounterType.BOUNTY, 1);
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, ownBears));
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        opponentBears.setCounterCount(CounterType.BOUNTY, 0);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, opponentBears));
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
