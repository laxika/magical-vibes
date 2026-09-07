package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StormTheSeedcore.class, GrizzlyBears.class})
class StormTheSeedcoreTest extends BaseCardTest {

    @Test
    @DisplayName("Distributes four counters and grants vigilance and trample to all own creatures")
    void distributesCountersAndGrantsKeywords() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareCast();

        harness.castSorcery(player1, 0, Map.of(
                first.getId(), 2,
                second.getId(), 1,
                third.getId(), 1));
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(third.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, first, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, first, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, third, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, third, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Choosing no targets still grants keywords to creatures you control")
    void noTargetsStillGrantKeywords() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareCast();

        harness.castSorcery(player1, 0, Map.of());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Granted keywords wear off at end of turn while counters remain")
    void keywordsWearOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareCast();

        harness.castSorcery(player1, 0, Map.of(creature.getId(), 4));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Only creatures you control may receive the counters")
    void rejectsOpponentCreatureTarget() {
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, Map.of(opponent.getId(), 4)))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new StormTheSeedcore()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
