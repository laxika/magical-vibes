package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VoiceOfTheBlessed.class, AngelOfMercy.class})
class VoiceOfTheBlessedTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter when controller gains life")
    void getsCounterOnLifeGain() {
        Permanent voice = harness.addToBattlefieldAndReturn(player1, new VoiceOfTheBlessed());
        assertThat(voice.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve Angel of Mercy
        harness.passBothPriorities(); // resolve GainLifeEffect
        harness.passBothPriorities(); // resolve Voice of the Blessed's counter trigger

        assertThat(voice.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("No flying, vigilance, or indestructible below four counters")
    void noKeywordsBelowFourCounters() {
        Permanent voice = harness.addToBattlefieldAndReturn(player1, new VoiceOfTheBlessed());
        voice.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        assertThat(gqs.hasKeyword(gd, voice, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, voice, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, voice, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Has flying and vigilance at four or more +1/+1 counters")
    void flyingAndVigilanceAtFourCounters() {
        Permanent voice = harness.addToBattlefieldAndReturn(player1, new VoiceOfTheBlessed());
        voice.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);

        assertThat(gqs.hasKeyword(gd, voice, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, voice, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, voice, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Has indestructible at ten or more +1/+1 counters")
    void indestructibleAtTenCounters() {
        Permanent voice = harness.addToBattlefieldAndReturn(player1, new VoiceOfTheBlessed());
        voice.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 10);

        assertThat(gqs.hasKeyword(gd, voice, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, voice, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, voice, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Keywords update dynamically as counters cross thresholds")
    void keywordsAreDynamic() {
        Permanent voice = harness.addToBattlefieldAndReturn(player1, new VoiceOfTheBlessed());

        voice.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        assertThat(gqs.hasKeyword(gd, voice, Keyword.FLYING)).isFalse();

        voice.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        assertThat(gqs.hasKeyword(gd, voice, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, voice, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, voice, Keyword.INDESTRUCTIBLE)).isFalse();

        voice.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 10);
        assertThat(gqs.hasKeyword(gd, voice, Keyword.INDESTRUCTIBLE)).isTrue();

        voice.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 9);
        assertThat(gqs.hasKeyword(gd, voice, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, voice, Keyword.FLYING)).isTrue();

        voice.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        assertThat(gqs.hasKeyword(gd, voice, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, voice, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Opponent gaining life does not add a counter")
    void opponentLifeGainDoesNotAddCounter() {
        Permanent voice = harness.addToBattlefieldAndReturn(player1, new VoiceOfTheBlessed());
        harness.enterBattlefieldAndReturn(player2, new AngelOfMercy());

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(23);
        assertThat(gd.stack).isEmpty();
        assertThat(voice.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Each life gain event adds one counter only after its trigger resolves")
    void separateLifeGainEventsEachAddOneCounter() {
        Permanent voice = harness.addToBattlefieldAndReturn(player1, new VoiceOfTheBlessed());

        for (int event = 0; event < 2; event++) {
            harness.enterBattlefieldAndReturn(player1, new AngelOfMercy());
            harness.passBothPriorities();

            assertThat(voice.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(event);
            assertThat(gd.stack).hasSize(1);

            harness.passBothPriorities();

            assertThat(voice.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(event + 1);
        }
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(26);
    }

    @Test
    @DisplayName("A life gain trigger does not put a counter on a different Voice")
    void departedSourceDoesNotPutCounterOnAnotherVoice() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new VoiceOfTheBlessed());
        harness.enterBattlefieldAndReturn(player1, new AngelOfMercy());
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(original);
        Permanent replacement = harness.addToBattlefieldAndReturn(player1, new VoiceOfTheBlessed());
        harness.passBothPriorities();

        assertThat(replacement.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Other counter types do not grant threshold keywords")
    void otherCounterTypesDoNotCountTowardThresholds() {
        Permanent voice = harness.addToBattlefieldAndReturn(player1, new VoiceOfTheBlessed());
        voice.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        voice.setCounterCount(CounterType.CHARGE, 10);

        assertThat(gqs.hasKeyword(gd, voice, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, voice, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, voice, Keyword.INDESTRUCTIBLE)).isFalse();
    }
}
