package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.c.Channel;
import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YavimayaBloomsageChannel.class, Channel.class, CrawWurm.class, GrizzlyBears.class})
class YavimayaBloomsageChannelTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a counter on a target creature you control and prepares at power seven")
    void preparesWhenTargetReachesSevenPower() {
        Permanent bloomsage = addBloomsage();
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new CrawWurm());

        advanceToEndStep(player1);
        harness.handlePermanentChosen(player1, wurm.getId());
        harness.passBothPriorities();

        assertThat(wurm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, wurm)).isEqualTo(7);
        assertThat(bloomsage.isPrepared()).isTrue();
        assertThat(gd.findExiledCard(bloomsage.getPreparedSpellCardId())).isNotNull();
    }

    @Test
    @DisplayName("Adds the counter but does not prepare when the target remains below seven power")
    void doesNotPrepareBelowSevenPower() {
        Permanent bloomsage = addBloomsage();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToEndStep(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bloomsage.isPrepared()).isFalse();
    }

    @Test
    @DisplayName("The end-step trigger only offers creatures you control")
    void onlyTargetsOwnCreatures() {
        addBloomsage();
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToEndStep(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opposingCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addBloomsage() {
        Permanent bloomsage = harness.addToBattlefieldAndReturn(player1, new YavimayaBloomsageChannel());
        bloomsage.setSummoningSick(false);
        return bloomsage;
    }

    private void advanceToEndStep(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
