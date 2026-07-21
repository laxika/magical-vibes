package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VoiceOfTheBlessedTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter when controller gains life")
    void getsCounterOnLifeGain() {
        harness.addToBattlefield(player1, new VoiceOfTheBlessed());
        Permanent voice = findPermanent(player1, "Voice of the Blessed");
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
        harness.addToBattlefield(player1, new VoiceOfTheBlessed());
        Permanent voice = findPermanent(player1, "Voice of the Blessed");
        voice.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        assertThat(gqs.hasKeyword(gd, voice, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, voice, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, voice, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Has flying and vigilance at four or more +1/+1 counters")
    void flyingAndVigilanceAtFourCounters() {
        harness.addToBattlefield(player1, new VoiceOfTheBlessed());
        Permanent voice = findPermanent(player1, "Voice of the Blessed");
        voice.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);

        assertThat(gqs.hasKeyword(gd, voice, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, voice, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, voice, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Has indestructible at ten or more +1/+1 counters")
    void indestructibleAtTenCounters() {
        harness.addToBattlefield(player1, new VoiceOfTheBlessed());
        Permanent voice = findPermanent(player1, "Voice of the Blessed");
        voice.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 10);

        assertThat(gqs.hasKeyword(gd, voice, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, voice, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, voice, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Keywords update dynamically as counters cross thresholds")
    void keywordsAreDynamic() {
        harness.addToBattlefield(player1, new VoiceOfTheBlessed());
        Permanent voice = findPermanent(player1, "Voice of the Blessed");

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
}
