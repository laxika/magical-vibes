package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PatrollingPeacemaker.class, Shock.class})
class PatrollingPeacemakerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two +1/+1 counters")
    void entersWithTwoPlusOnePlusOneCounters() {
        Permanent peacemaker = harness.enterBattlefieldAndReturn(player1, new PatrollingPeacemaker());

        assertThat(peacemaker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Proliferates when an opponent commits a crime")
    void proliferatesWhenOpponentCommitsCrime() {
        Permanent peacemaker = harness.enterBattlefieldAndReturn(player1, new PatrollingPeacemaker());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(peacemaker.getId()));

        assertThat(peacemaker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not trigger when its controller commits a crime")
    void doesNotTriggerForItsControllersCrime() {
        Permanent peacemaker = harness.enterBattlefieldAndReturn(player1, new PatrollingPeacemaker());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(peacemaker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
