package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.y.YavimayaSapherd;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FireNationSentinels.class, GrizzlyBears.class, Shock.class, YavimayaSapherd.class})
class FireNationSentinelsTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on each creature you control when an opponent's nontoken creature dies")
    void putsCountersOnEachControlledCreatureWhenOpponentNontokenCreatureDies() {
        Permanent sentinels = harness.addToBattlefieldAndReturn(player1, new FireNationSentinels());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        destroyWithShock(player1, opponentCreature.getId());

        assertThat(sentinels.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when an opponent's creature token dies")
    void doesNotTriggerWhenOpponentCreatureTokenDies() {
        Permanent sentinels = harness.addToBattlefieldAndReturn(player1, new FireNationSentinels());
        harness.enterBattlefieldAndReturn(player2, new YavimayaSapherd());
        resolveAllTriggers();

        Permanent saprolingToken = findPermanent(player2, "Saproling");
        destroyWithShock(player1, saprolingToken.getId());

        assertThat(sentinels.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void destroyWithShock(com.github.laxika.magicalvibes.model.Player caster, UUID targetId) {
        harness.forceActivePlayer(caster);
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castInstant(caster, 0, targetId);
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}
