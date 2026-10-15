package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FumeSpitter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Triskelion;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoldPlagiarist.class, FumeSpitter.class, GrizzlyBears.class, Triskelion.class})
class BoldPlagiaristTest extends BaseCardTest {

    @Test
    void copiesCountersOpponentPutsOnTheirOwnCreature() {
        Permanent plagiarist = harness.addToBattlefieldAndReturn(player1, new BoldPlagiarist());
        Permanent spitter = harness.addToBattlefieldAndReturn(player2, new FumeSpitter());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(spitter), null, target.getId());
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

    @Test
    void copiesCountersEvenWhenTheyKillTheOriginalCreature() {
        Permanent plagiarist = harness.addToBattlefieldAndReturn(player1, new BoldPlagiarist());
        Permanent spitter = harness.addToBattlefieldAndReturn(player2, new FumeSpitter());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FumeSpitter());

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(spitter), null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(plagiarist.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    void copiedCountersArePlacedByTheOpponentAndDoNotTriggerTheirPlagiarist() {
        Permanent plagiarist = harness.addToBattlefieldAndReturn(player1, new BoldPlagiarist());
        Permanent opposingPlagiarist = harness.addToBattlefieldAndReturn(player2, new BoldPlagiarist());
        Permanent spitter = harness.addToBattlefieldAndReturn(player2, new FumeSpitter());

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(spitter), null, opposingPlagiarist.getId());
        resolveAllTriggers();

        assertThat(plagiarist.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(opposingPlagiarist.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingPlagiarist);
    }

    @Test
    void doesNotCopyCountersYouPutOnAnOpponentsCreature() {
        Permanent plagiarist = harness.addToBattlefieldAndReturn(player1, new BoldPlagiarist());
        Permanent spitter = harness.addToBattlefieldAndReturn(player1, new FumeSpitter());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(spitter), null, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(plagiarist.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    void copiesAllCountersPlacedAsAnOpponentsCreatureEnters() {
        Permanent plagiarist = harness.addToBattlefieldAndReturn(player1, new BoldPlagiarist());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new Triskelion(), "{6}");
        resolveAllTriggers();

        assertThat(plagiarist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void canBeCastDuringAnOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.castFromHand(player1, new BoldPlagiarist(), "{3}{B}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof BoldPlagiarist);
    }
}
