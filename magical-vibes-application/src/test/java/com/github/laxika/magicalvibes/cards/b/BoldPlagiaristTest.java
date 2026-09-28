package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FumeSpitter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoldPlagiarist.class, FumeSpitter.class, GrizzlyBears.class})
class BoldPlagiaristTest extends BaseCardTest {

    @Test
    void copiesCountersOpponentPutsOnTheirOwnCreature() {
        Permanent plagiarist = harness.addToBattlefieldAndReturn(player1, new BoldPlagiarist());
        Permanent spitter = harness.addToBattlefieldAndReturn(player2, new FumeSpitter());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(spitter), null, target.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(plagiarist.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotCopyCountersOpponentPutsOnYourCreature() {
        Permanent plagiarist = harness.addToBattlefieldAndReturn(player1, new BoldPlagiarist());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent spitter = harness.addToBattlefieldAndReturn(player2, new FumeSpitter());

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(spitter), null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(plagiarist.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }
}
