package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FearlessFledgling.class, Forest.class})
class FearlessFledglingTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall puts a +1/+1 counter on Fearless Fledgling and gives it flying")
    void landfallBoostsAndGrantsFlying() {
        Permanent fledgling = harness.addToBattlefieldAndReturn(player1, new FearlessFledgling());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(fledgling.getEffectivePower()).isEqualTo(2);
        assertThat(fledgling.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, fledgling, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Landfall flying wears off but the +1/+1 counter remains")
    void flyingWearsOffButCounterRemains() {
        Permanent fledgling = harness.addToBattlefieldAndReturn(player1, new FearlessFledgling());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(fledgling.getEffectivePower()).isEqualTo(2);
        assertThat(fledgling.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, fledgling, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("An opponent's land does not trigger Fearless Fledgling")
    void opponentLandDoesNotTrigger() {
        Permanent fledgling = harness.addToBattlefieldAndReturn(player1, new FearlessFledgling());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(fledgling.getEffectivePower()).isEqualTo(1);
        assertThat(fledgling.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, fledgling, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Lands entering without being played each trigger one complete landfall ability")
    void multipleLandEntriesEachAddACounter() {
        Permanent fledgling = harness.addToBattlefieldAndReturn(player1, new FearlessFledgling());

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.enterBattlefieldAndReturn(player1, new Forest());

        assertThat(gd.stack).hasSize(2);
        assertThat(fledgling.getCounters().getOrDefault(CounterType.PLUS_ONE_PLUS_ONE, 0)).isZero();
        assertThat(gqs.hasKeyword(gd, fledgling, Keyword.FLYING)).isFalse();

        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(fledgling.getCounters().get(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, fledgling, Keyword.FLYING)).isTrue();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(fledgling.getCounters().get(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(fledgling.getEffectivePower()).isEqualTo(3);
        assertThat(fledgling.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, fledgling, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("A creature entering does not trigger landfall")
    void nonlandEntryDoesNotTrigger() {
        Permanent fledgling = harness.addToBattlefieldAndReturn(player1, new FearlessFledgling());

        Permanent other = harness.enterBattlefieldAndReturn(player1, new FearlessFledgling());

        assertThat(gd.stack).isEmpty();
        assertThat(fledgling.getCounters()).isEmpty();
        assertThat(other.getCounters()).isEmpty();
        assertThat(gqs.hasKeyword(gd, fledgling, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isFalse();
    }
}
