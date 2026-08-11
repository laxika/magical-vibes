package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StingSlingerTest extends BaseCardTest {

    @Test
    void blightsCreatureAndDealsDamageToEachOpponent() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Permanent stingSlinger = harness.addToBattlefieldAndReturn(player1, new StingSlinger());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        stingSlinger.setSummoningSick(false);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int startingLife = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife - 2);
        assertThat(stingSlinger.isTapped()).isTrue();
    }
}
