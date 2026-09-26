package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FumeSpitter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaptainMarvelApexAvenger.class, FumeSpitter.class, GrizzlyBears.class})
class CaptainMarvelApexAvengerTest extends BaseCardTest {

    @Test
    void copiesCountersPutOnAnotherPlayersCreature() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new CaptainMarvelApexAvenger());
        Permanent spitter = harness.addToBattlefieldAndReturn(player1, new FumeSpitter());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(spitter), 0, null, target.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(captain.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerWhenCountersArePutOnCaptainMarvel() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new CaptainMarvelApexAvenger());
        Permanent spitter = harness.addToBattlefieldAndReturn(player1, new FumeSpitter());

        harness.forceActivePlayer(player1);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(spitter), 0, null, captain.getId());
        harness.passBothPriorities();

        assertThat(captain.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void doesNotTriggerForAKreeCreature() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new CaptainMarvelApexAvenger());
        Permanent spitter = harness.addToBattlefieldAndReturn(player1, new FumeSpitter());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CaptainMarvelApexAvenger());

        harness.forceActivePlayer(player1);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(spitter), 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(captain.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
