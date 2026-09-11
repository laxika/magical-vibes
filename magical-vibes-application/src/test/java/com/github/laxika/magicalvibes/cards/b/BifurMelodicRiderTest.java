package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BifurMelodicRider.class, FountainOfYouth.class, GrizzlyBears.class})
class BifurMelodicRiderTest extends BaseCardTest {

    @Test
    void putsACounterOnTargetCreatureWhenEnteringAndAttacking() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent bifur = harness.enterBattlefieldAndReturn(player1, new BifurMelodicRider());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        bifur.setSummoningSick(false);
        declareAttackers(List.of(1));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void enduringStoryMakesBifursDwarfTriggersTriggerAgain() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.addToBattlefield(player1, new FountainOfYouth());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent bifur = harness.enterBattlefieldAndReturn(player1, new BifurMelodicRider());

        assertThat(gd.playersWithEnduringStory).contains(player1.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        bifur.setSummoningSick(false);
        declareAttackers(List.of(3));
        harness.handlePermanentChosen(player1, target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }
}
