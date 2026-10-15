package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FearlessSkald.class, FurnaceHostCharger.class})
class FearlessSkaldTest extends BaseCardTest {

    @Test
    @DisplayName("Backup puts a +1/+1 counter on another creature and grants double strike")
    void backsUpAnotherCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FurnaceHostCharger());
        castSkald();

        resolveEtbTargeting(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Backup targeting the source puts on the counter but does not grant double strike")
    void backingUpSourceDoesNotGrantDoubleStrike() {
        castSkald();
        Permanent skald = findPermanent(player1, "Fearless Skald");

        resolveEtbTargeting(skald);

        assertThat(skald.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(skald.getGrantedKeywords()).doesNotContain(Keyword.DOUBLE_STRIKE);
    }

    @Test
    @DisplayName("Backup's granted double strike expires at the end of the turn")
    void grantedDoubleStrikeExpiresAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FurnaceHostCharger());
        castSkald();
        resolveEtbTargeting(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Backup can put a counter and grant double strike to an opponent's creature")
    void backsUpOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FurnaceHostCharger());
        castSkald();

        resolveEtbTargeting(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Backup still resolves after its source leaves the battlefield")
    void backupResolvesWithoutSource() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FurnaceHostCharger());
        castSkald();
        Permanent skald = findPermanent(player1, "Fearless Skald");
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(skald);
        gd.playerGraveyards.get(player1.getId()).add(skald.getCard());

        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
    }

    private void castSkald() {
        harness.castFromHand(player1, new FearlessSkald(), "{4}{R}");
        harness.passBothPriorities();
    }

    private void resolveEtbTargeting(Permanent target) {
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
