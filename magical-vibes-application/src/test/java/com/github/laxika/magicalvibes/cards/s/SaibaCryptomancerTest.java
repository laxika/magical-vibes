package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.o.OmenHawker;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SaibaCryptomancer.class, OmenHawker.class})
class SaibaCryptomancerTest extends BaseCardTest {

    @Test
    @DisplayName("Backup puts a +1/+1 counter on another creature and grants hexproof")
    void backsUpAnotherCreature() {
        Permanent hawker = harness.addToBattlefieldAndReturn(player1, new OmenHawker());
        castSaibaCryptomancer();

        resolveEtbTargeting(hawker);

        assertThat(hawker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(hawker.hasKeyword(Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Backup's granted hexproof expires at the end of the turn")
    void grantedHexproofExpiresAtEndOfTurn() {
        Permanent hawker = harness.addToBattlefieldAndReturn(player1, new OmenHawker());
        castSaibaCryptomancer();
        resolveEtbTargeting(hawker);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(hawker.hasKeyword(Keyword.HEXPROOF)).isFalse();
        assertThat(hawker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Backup can target Saiba Cryptomancer itself")
    void backsUpItself() {
        castSaibaCryptomancer();
        Permanent cryptomancer = gd.playerBattlefields.get(player1.getId()).getFirst();

        resolveEtbTargeting(cryptomancer);

        assertThat(cryptomancer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(cryptomancer.hasKeyword(Keyword.HEXPROOF)).isTrue();
        assertThat(cryptomancer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Backup can give an opposing creature a counter and hexproof")
    void backsUpOpposingCreature() {
        Permanent hawker = harness.addToBattlefieldAndReturn(player2, new OmenHawker());
        castSaibaCryptomancer();

        resolveEtbTargeting(hawker);

        assertThat(hawker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(hawker.hasKeyword(Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Flash allows casting during the opponent's end step")
    void castsDuringOpponentsEndStep() {
        Permanent hawker = harness.addToBattlefieldAndReturn(player1, new OmenHawker());
        gd.activePlayerId = player2.getId();
        harness.forceStep(TurnStep.END_STEP);
        castSaibaCryptomancer();

        resolveEtbTargeting(hawker);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof SaibaCryptomancer);
        assertThat(hawker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(hawker.hasKeyword(Keyword.HEXPROOF)).isTrue();
    }

    private void castSaibaCryptomancer() {
        harness.castFromHand(player1, new SaibaCryptomancer(), "{1}{U}");
        harness.passBothPriorities();
    }

    private void resolveEtbTargeting(Permanent target) {
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
