package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AjaniTheGreathearted.class, ChandraNalaar.class, GrizzlyBears.class})
class AjaniTheGreatheartedTest extends BaseCardTest {

    @Test
    @DisplayName("Static ability grants vigilance to creatures you control")
    void staticAbilityGrantsVigilanceToControlledCreatures() {
        addReadyAjani(4);
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentBear, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("+1 gains 3 life and adds a loyalty counter")
    void plusOneGainsLife() {
        Permanent ajani = addReadyAjani(4);
        harness.setLife(player1, 7);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("-2 puts counters on controlled creatures and other planeswalkers")
    void minusTwoPutsCountersOnCreaturesAndOtherPlaneswalkers() {
        Permanent ajani = addReadyAjani(4);
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent chandra = harness.addToBattlefieldAndReturn(player1, new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(ownBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    private Permanent addReadyAjani(int loyalty) {
        Permanent ajani = new Permanent(new AjaniTheGreathearted());
        ajani.setCounterCount(CounterType.LOYALTY, loyalty);
        ajani.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(ajani);
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        return ajani;
    }
}
