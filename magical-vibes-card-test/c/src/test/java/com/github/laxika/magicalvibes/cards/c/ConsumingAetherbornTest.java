package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.w.WaryThespian;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ConsumingAetherborn.class, WaryThespian.class})
class ConsumingAetherbornTest extends BaseCardTest {

    @Test
    @DisplayName("Backup puts a +1/+1 counter on another creature and grants lifelink")
    void backsUpAnotherCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new WaryThespian());
        Permanent aetherborn = castConsumingAetherborn();

        resolveEtbTargeting(bears);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.hasKeyword(Keyword.LIFELINK)).isTrue();
        assertThat(aetherborn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Backup targeting the source puts on the counter but does not grant lifelink")
    void backingUpSourceDoesNotGrantLifelink() {
        Permanent aetherborn = castConsumingAetherborn();

        resolveEtbTargeting(aetherborn);

        assertThat(aetherborn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(aetherborn.getGrantedKeywords()).doesNotContain(Keyword.LIFELINK);
    }

    @Test
    @DisplayName("Backup's granted lifelink expires at the end of the turn")
    void grantedLifelinkExpiresAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new WaryThespian());
        castConsumingAetherborn();
        resolveEtbTargeting(bears);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.LIFELINK)).isFalse();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Backup can put a counter on an opponent's creature and grant it lifelink")
    void backsUpOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WaryThespian());
        castConsumingAetherborn();

        resolveEtbTargeting(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.hasKeyword(Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Backup still grants its counter and lifelink after its source dies")
    void backupResolvesAfterSourceDies() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WaryThespian());
        Permanent source = castConsumingAetherborn();
        harness.handlePermanentChosen(player1, target.getId());

        source.setMarkedDamage(2);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.hasKeyword(Keyword.LIFELINK)).isTrue();
    }

    private Permanent castConsumingAetherborn() {
        harness.castFromHand(player1, new ConsumingAetherborn(), "{3}{B}");
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof ConsumingAetherborn)
                .findFirst()
                .orElseThrow();
    }

    private void resolveEtbTargeting(Permanent target) {
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
