package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.e.EssenceWarden;
import com.github.laxika.magicalvibes.cards.h.HumbleDefector;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OmerKingOfRohan.class, HumbleDefector.class, EssenceWarden.class})
class OmerKingOfRohanTest extends BaseCardTest {

    @Test
    void entersWithCountersForOtherHumansAndResolvesBothTargets() {
        harness.addToBattlefield(player1, new HumbleDefector());
        harness.addToBattlefield(player1, new HumbleDefector());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EssenceWarden());

        Permanent omer = harness.enterBattlefieldAndReturn(player1, new OmerKingOfRohan());
        assertThat(omer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.monarchPlayerId).isEqualTo(player2.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    void countsOnlyOtherHumansYouControl() {
        Permanent omer = harness.enterBattlefieldAndReturn(player1, new OmerKingOfRohan());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
        harness.assertLife(player2, 18);
        assertThat(omer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void ignoresOpposingHumansAndControlledNonHumansForEntryCounters() {
        harness.addToBattlefield(player2, new HumbleDefector());
        harness.addToBattlefield(player1, new EssenceWarden());

        Permanent omer = harness.enterBattlefieldAndReturn(player1, new OmerKingOfRohan());

        assertThat(omer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void samePlayerCanBeBothTargetsOfOneTrigger() {
        harness.enterBattlefieldAndReturn(player1, new OmerKingOfRohan());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.monarchPlayerId).isEqualTo(player2.getId());
        harness.assertLife(player2, 18);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void damageUsesPowerAtResolution() {
        Permanent omer = harness.enterBattlefieldAndReturn(player1, new OmerKingOfRohan());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());

        harness.inMutationScope(() -> omer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3));
        harness.passBothPriorities();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
        harness.assertLife(player2, 15);
    }
}
