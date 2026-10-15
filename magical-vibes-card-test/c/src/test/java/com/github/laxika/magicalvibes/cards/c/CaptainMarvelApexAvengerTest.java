package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FumeSpitter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaptainMarvelApexAvenger.class, FumeSpitter.class, GrizzlyBears.class, WoodlandChangeling.class})
class CaptainMarvelApexAvengerTest extends BaseCardTest {

    @Test
    void copiesCountersEvenWhenTheOtherCreatureDiesFromThem() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new CaptainMarvelApexAvenger());
        Permanent spitter = harness.addToBattlefieldAndReturn(player1, new FumeSpitter());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FumeSpitter());

        harness.forceActivePlayer(player1);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(spitter), null, target.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(captain.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerForAChangelingBecauseItIsAKree() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new CaptainMarvelApexAvenger());
        Permanent spitter = harness.addToBattlefieldAndReturn(player1, new FumeSpitter());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WoodlandChangeling());

        harness.forceActivePlayer(player1);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(spitter), null, target.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(captain.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void mayDeclineToCopyCounters() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new CaptainMarvelApexAvenger());
        Permanent spitter = harness.addToBattlefieldAndReturn(player1, new FumeSpitter());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(spitter), null, target.getId());
        harness.passBothPriorities();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(captain.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void doesNotTriggerWhenAnOpponentPutsCountersOnAnotherCreature() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new CaptainMarvelApexAvenger());
        Permanent spitter = harness.addToBattlefieldAndReturn(player2, new FumeSpitter());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(spitter), null, target.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(captain.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }


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
