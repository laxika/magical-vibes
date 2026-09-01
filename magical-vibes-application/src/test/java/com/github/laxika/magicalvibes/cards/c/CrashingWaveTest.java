package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrashingWave.class, GrizzlyBears.class})
class CrashingWaveTest extends BaseCardTest {

    @Test
    void waterbendsTapsTargetsAndDistributesCountersAmongTappedOpponentCreatures() {
        Permanent waterbendSourceOne = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent waterbendSourceTwo = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent alreadyTapped = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        alreadyTapped.tap();
        harness.setHand(player1, List.of(new CrashingWave()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        gs.playCard(gd, player1, 0, 2, null, null,
                List.of(firstTarget.getId(), secondTarget.getId()), List.of(), false,
                null, null, null, null, null, false, null, null, null,
                List.of(waterbendSourceOne.getId(), waterbendSourceTwo.getId()));
        harness.passBothPriorities();

        assertThat(waterbendSourceOne.isTapped()).isTrue();
        assertThat(waterbendSourceTwo.isTapped()).isTrue();
        assertThat(firstTarget.isTapped()).isTrue();
        assertThat(secondTarget.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();

        harness.handleXValueChosen(player1, 1);
        harness.handleXValueChosen(player1, 1);
        harness.handleXValueChosen(player1, 1);

        assertThat(firstTarget.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(secondTarget.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(alreadyTapped.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(waterbendSourceOne.getCounterCount(CounterType.STUN)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
