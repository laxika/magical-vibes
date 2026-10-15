package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AinokArtillerist.class, AvenSunstriker.class})
class AinokArtilleristTest extends BaseCardTest {

    @Test
    @DisplayName("Does not have reach without a +1/+1 counter")
    void noReachWithoutCounter() {
        Permanent artillerist = addCreatureReady(player1, new AinokArtillerist());

        assertThat(gqs.hasKeyword(gd, artillerist, Keyword.REACH)).isFalse();
    }

    @Test
    @DisplayName("Has reach while it has a +1/+1 counter")
    void hasReachWithCounter() {
        Permanent artillerist = addCreatureReady(player1, new AinokArtillerist());
        artillerist.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, artillerist, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Loses reach when its +1/+1 counter is removed")
    void losesReachWhenCounterRemoved() {
        Permanent artillerist = addCreatureReady(player1, new AinokArtillerist());
        artillerist.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gqs.hasKeyword(gd, artillerist, Keyword.REACH)).isTrue();

        artillerist.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        assertThat(gqs.hasKeyword(gd, artillerist, Keyword.REACH)).isFalse();
    }

    @Test
    @DisplayName("Cannot block a flyer without a +1/+1 counter")
    void cannotBlockFlyingWithoutCounter() {
        addCreatureReady(player1, new AvenSunstriker());
        addCreatureReady(player2, new AinokArtillerist());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(flying)");
    }

    @Test
    @DisplayName("Can block a flyer with multiple +1/+1 counters")
    void canBlockFlyingWithCounters() {
        addCreatureReady(player1, new AvenSunstriker());
        Permanent artillerist = addCreatureReady(player2, new AinokArtillerist());
        artillerist.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Other counter types do not grant reach")
    void otherCounterTypesDoNotGrantReach() {
        Permanent artillerist = addCreatureReady(player1, new AinokArtillerist());
        artillerist.setCounterCount(CounterType.CHARGE, 2);

        assertThat(gqs.hasKeyword(gd, artillerist, Keyword.REACH)).isFalse();
    }

    @Test
    @DisplayName("Counters on another Artillerist do not grant this one reach")
    void countersOnAnotherArtilleristDoNotGrantReach() {
        Permanent withoutCounter = addCreatureReady(player1, new AinokArtillerist());
        Permanent withCounter = addCreatureReady(player1, new AinokArtillerist());
        withCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, withoutCounter, Keyword.REACH)).isFalse();
        assertThat(gqs.hasKeyword(gd, withCounter, Keyword.REACH)).isTrue();
    }
}
