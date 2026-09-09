package com.github.laxika.magicalvibes.cards.r;

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

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RocHatchling.class})
class RocHatchlingTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with four shell counters and no boost")
    void entersWithFourShellCounters() {
        harness.setHand(player1, List.of(new RocHatchling()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent hatchling = findPermanent(player1, "Roc Hatchling");
        assertThat(hatchling.getCounterCount(CounterType.SHELL)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, hatchling)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, hatchling)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, hatchling, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The upkeep trigger removes one shell counter")
    void upkeepRemovesOneShellCounter() {
        Permanent hatchling = addCreatureReady(player1, new RocHatchling());
        hatchling.setCounterCount(CounterType.SHELL, 4);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(hatchling.getCounterCount(CounterType.SHELL)).isEqualTo(3);
    }

    @Test
    @DisplayName("The upkeep that removes the last counter turns it into a 3/3 flier")
    void becomesFlierWhenLastCounterRemoved() {
        Permanent hatchling = addCreatureReady(player1, new RocHatchling());
        hatchling.setCounterCount(CounterType.SHELL, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(hatchling.getCounterCount(CounterType.SHELL)).isZero();
        assertThat(gqs.getEffectivePower(gd, hatchling)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hatchling)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, hatchling, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Putting a shell counter back takes the boost and flying away again")
    void boostTracksShellCounters() {
        Permanent hatchling = addCreatureReady(player1, new RocHatchling());

        assertThat(gqs.getEffectivePower(gd, hatchling)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, hatchling, Keyword.FLYING)).isTrue();

        hatchling.setCounterCount(CounterType.SHELL, 1);

        assertThat(gqs.getEffectivePower(gd, hatchling)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, hatchling)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, hatchling, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The upkeep trigger does not fire during an opponent's upkeep")
    void upkeepDoesNotFireForOpponent() {
        Permanent hatchling = addCreatureReady(player1, new RocHatchling());
        hatchling.setCounterCount(CounterType.SHELL, 4);

        advanceToUpkeep(player2);

        assertThat(hatchling.getCounterCount(CounterType.SHELL)).isEqualTo(4);
    }

    @Test
    @DisplayName("An unlocked hatchling stays unlocked through later upkeeps")
    void unlockedHatchlingStaysUnlocked() {
        Permanent hatchling = addCreatureReady(player1, new RocHatchling());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(hatchling.getCounterCount(CounterType.SHELL)).isZero();
        assertThat(gqs.getEffectivePower(gd, hatchling)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hatchling)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, hatchling, Keyword.FLYING)).isTrue();
    }
}
