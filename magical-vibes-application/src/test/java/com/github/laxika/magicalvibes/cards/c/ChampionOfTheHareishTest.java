package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChampionOfTheHareish.class, GrizzlyBears.class})
class ChampionOfTheHareishTest extends BaseCardTest {

    @Test
    void writesAnInitialBuddyAndAddsUnfamiliarTypes() {
        Permanent champion = harness.enterBattlefieldAndReturn(player1, new ChampionOfTheHareish());
        harness.handleListChoice(player1, "RABBIT");

        assertThat(gd.getBuddyList(player1.getId())).containsExactly(CardSubtype.RABBIT);

        triggerPermanentEntry(player1, addCreatureReady(player1, new GrizzlyBears()));
        assertThat(gd.getBuddyList(player1.getId())).containsExactlyInAnyOrder(
                CardSubtype.RABBIT, CardSubtype.BEAR);
        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        triggerPermanentEntry(player1, addCreatureReady(player1, new GrizzlyBears()));
        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void triggerPermanentEntry(Player controller, Permanent enteringPermanent) {
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .checkAllyCreatureEntersTriggers(gd, controller.getId(), enteringPermanent.getCard(), 0));
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
