package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoundingFelidar.class, GrizzlyBears.class})
class BoundingFelidarTest extends BaseCardTest {

    @Test
    @DisplayName("Saddle 2 taps another creature and saddles Bounding Felidar")
    void saddleTapsAnotherCreature() {
        Permanent felidar = addCreatureReady(player1, new BoundingFelidar());
        Permanent helper = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(felidar.isSaddled()).isTrue();
        assertThat(helper.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Attacking while saddled puts counters on other creatures and gains life for each")
    void attacksWhileSaddled() {
        Permanent felidar = addCreatureReady(player1, new BoundingFelidar());
        Permanent firstHelper = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondHelper = addCreatureReady(player1, new GrizzlyBears());
        felidar.setSaddled(true);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(felidar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(firstHelper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondHelper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("The attack trigger checks saddled when attackers are declared")
    void doesNotTriggerWhenNotSaddledAtDeclaration() {
        Permanent felidar = addCreatureReady(player1, new BoundingFelidar());
        Permanent helper = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        felidar.setSaddled(true);
        resolveAllTriggers();

        assertThat(helper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }
}
