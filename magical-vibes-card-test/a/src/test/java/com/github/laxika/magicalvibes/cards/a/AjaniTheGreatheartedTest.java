package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GideonBlackblade;
import com.github.laxika.magicalvibes.cards.p.PollenbrightDruid;
import com.github.laxika.magicalvibes.cards.s.SarkhanTheMasterless;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AjaniTheGreathearted.class, ChandraNalaar.class, GrizzlyBears.class,
        GideonBlackblade.class, PollenbrightDruid.class, SarkhanTheMasterless.class})
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

    @Test
    @DisplayName("Ajani has vigilance when Sarkhan turns him into a creature")
    void animatedAjaniHasVigilance() {
        Permanent ajani = addReadyAjani(5);
        Permanent sarkhan = harness.addToBattlefieldAndReturn(player1, new SarkhanTheMasterless());
        sarkhan.setCounterCount(CounterType.LOYALTY, 5);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, ajani)).isTrue();
        assertThat(gqs.hasKeyword(gd, ajani, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, sarkhan, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("-2 gives both kinds of counters to a creature planeswalker, excluding opponents")
    void creaturePlaneswalkerReceivesBothCounters() {
        addReadyAjani(5);
        Permanent ownGideon = harness.addToBattlefieldAndReturn(player1, new GideonBlackblade());
        ownGideon.setCounterCount(CounterType.LOYALTY, 4);
        Permanent opposingGideon = harness.addToBattlefieldAndReturn(player2, new GideonBlackblade());
        opposingGideon.setCounterCount(CounterType.LOYALTY, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(ownGideon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownGideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(opposingGideon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opposingGideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("-2 resolves even when paying its cost puts Ajani into the graveyard")
    void minusTwoResolvesAfterAjaniDiesToLoyaltyCost() {
        Permanent ajani = addReadyAjani(2);
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new PollenbrightDruid());
        Permanent sarkhan = harness.addToBattlefieldAndReturn(player1, new SarkhanTheMasterless());
        sarkhan.setCounterCount(CounterType.LOYALTY, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ajani);
        harness.passBothPriorities();

        assertThat(druid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(sarkhan.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    private Permanent addReadyAjani(int loyalty) {
        Permanent ajani = harness.addToBattlefieldAndReturn(player1, new AjaniTheGreathearted());
        ajani.setCounterCount(CounterType.LOYALTY, loyalty);
        ajani.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        return ajani;
    }
}
