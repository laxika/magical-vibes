package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.AlpineGrizzly;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HighSentinelsOfArashin.class, AlpineGrizzly.class, Forest.class})
class HighSentinelsOfArashinTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 for each other countered creature you control")
    void scalesWithOtherCounteredCreaturesYouControl() {
        Permanent sentinels = addCreatureReady(player1, new HighSentinelsOfArashin());
        Permanent counteredCreature = addCreatureReady(player1, new AlpineGrizzly());
        addCreatureReady(player1, new AlpineGrizzly());
        addCreatureReady(player2, new AlpineGrizzly()).setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        counteredCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.getEffectivePower(gd, sentinels)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, sentinels)).isEqualTo(5);
    }

    @Test
    @DisplayName("Puts a +1/+1 counter on the targeted creature")
    void putsCounterOnTargetCreature() {
        Permanent sentinels = addCreatureReady(player1, new HighSentinelsOfArashin());
        Permanent target = addCreatureReady(player1, new AlpineGrizzly());
        activateAbility(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, sentinels)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, sentinels)).isEqualTo(5);
    }

    @Test
    @DisplayName("Cannot target a land with its activated ability")
    void cannotTargetLand() {
        addCreatureReady(player1, new HighSentinelsOfArashin());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Counts creatures once regardless of counter quantity and updates as counters change")
    void countsCreaturesRatherThanCountersAndUpdatesContinuously() {
        Permanent sentinels = addCreatureReady(player1, new HighSentinelsOfArashin());
        Permanent first = addCreatureReady(player1, new AlpineGrizzly());
        Permanent second = addCreatureReady(player1, new AlpineGrizzly());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        second.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        land.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.getEffectivePower(gd, sentinels)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, sentinels)).isEqualTo(6);

        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        assertThat(gqs.getEffectivePower(gd, sentinels)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, sentinels)).isEqualTo(5);

        second.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        assertThat(gqs.getEffectivePower(gd, sentinels)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sentinels)).isEqualTo(4);
    }

    @Test
    @DisplayName("Can target itself without counting itself for its static bonus")
    void canTargetItselfWithoutDoubleCounting() {
        Permanent sentinels = harness.addToBattlefieldAndReturn(player1, new HighSentinelsOfArashin());
        sentinels.tap();

        activateAbility(sentinels);
        activateAbility(sentinels);

        assertThat(sentinels.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, sentinels)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, sentinels)).isEqualTo(6);
        assertThat(sentinels.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can put a counter on an opponent's creature without gaining a static bonus")
    void canTargetOpponentsCreature() {
        Permanent sentinels = addCreatureReady(player1, new HighSentinelsOfArashin());
        Permanent target = addCreatureReady(player2, new AlpineGrizzly());

        activateAbility(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, sentinels)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sentinels)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not count creatures with only a different kind of counter")
    void ignoresCreaturesWithOnlyMinusOneCounters() {
        Permanent sentinels = addCreatureReady(player1, new HighSentinelsOfArashin());
        Permanent other = addCreatureReady(player1, new AlpineGrizzly());
        other.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        assertThat(gqs.getEffectivePower(gd, sentinels)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sentinels)).isEqualTo(4);
    }

    private void activateAbility(Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        addManaForAbility();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
    }

    private void addManaForAbility() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
