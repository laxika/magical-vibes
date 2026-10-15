package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaptainAmericaSkybound.class, GrizzlyBears.class, Unsummon.class})
class CaptainAmericaSkyboundTest extends BaseCardTest {

    @Test
    void battalionPutsCountersAndGrantsIndestructibleToAttackers() {
        Permanent captain = addCreatureReady(player1, new CaptainAmericaSkybound());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(captain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(otherAttacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonAttacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(captain.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(attacker.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(otherAttacker.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(nonAttacker.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(opposingCreature.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void battalionDoesNotTriggerWithoutTwoOtherAttackers() {
        Permanent captain = addCreatureReady(player1, new CaptainAmericaSkybound());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        assertThat(gd.stack).isEmpty();
        resolveAllTriggers();

        assertThat(captain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(captain.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void battalionDoesNotTriggerWhenCaptainDoesNotAttack() {
        Permanent captain = addCreatureReady(player1, new CaptainAmericaSkybound());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1, 2, 3));

        assertThat(gd.stack).isEmpty();
        assertThat(captain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(attacker.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void battalionStillResolvesAfterAnAttackerLeaves(boolean returnCaptain) {
        Permanent captain = addCreatureReady(player1, new CaptainAmericaSkybound());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent remainingAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        declareAttackers(player1, List.of(0, 1, 2));
        assertThat(gd.stack).hasSize(1);
        Permanent returned = returnCaptain ? captain : attacker;
        harness.castInstant(player1, 0, returned.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(returned);
        assertThat(gd.playerHands.get(player1.getId())).contains(returned.getCard());
        assertThat(remainingAttacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        resolveAllTriggers();

        Permanent otherRemainingAttacker = returnCaptain ? attacker : captain;
        assertThat(remainingAttacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(otherRemainingAttacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(remainingAttacker.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(otherRemainingAttacker.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(nonAttacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(nonAttacker.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void indestructibleWearsOffAtEndOfTurn() {
        Permanent captain = addCreatureReady(player1, new CaptainAmericaSkybound());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();
        assertThat(captain.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(captain.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(captain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
