package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.cards.h.HapatraVizierOfPoisons;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SerratedBiskelion.class, BenalishKnight.class, MindStone.class, HapatraVizierOfPoisons.class})
class SerratedBiskelionTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a -1/-1 counter on itself and target creature")
    void putsCountersOnSourceAndTarget() {
        Permanent biskelion = addCreatureReady(player1, new SerratedBiskelion());
        Permanent knight = harness.addToBattlefieldAndReturn(player2, new BenalishKnight());

        harness.activateAbility(player1, 0, null, knight.getId());
        harness.passBothPriorities();

        assertThat(biskelion.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(knight.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(biskelion.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, biskelion)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, biskelion)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent biskelion = addCreatureReady(player1, new SerratedBiskelion());
        Permanent mindStone = harness.addToBattlefieldAndReturn(player2, new MindStone());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, mindStone.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(biskelion.isTapped()).isFalse();
        assertThat(biskelion.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Can target itself")
    void canTargetItself() {
        Permanent biskelion = addCreatureReady(player1, new SerratedBiskelion());

        harness.activateAbility(player1, 0, null, biskelion.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Serrated Biskelion");
        harness.assertNotOnBattlefield(player1, "Serrated Biskelion");
    }

    @Test
    @DisplayName("Counters are placed on resolution, not as an activation cost")
    void countersWaitForResolution() {
        Permanent biskelion = addCreatureReady(player1, new SerratedBiskelion());
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new BenalishKnight());

        harness.activateAbility(player1, 0, null, knight.getId());

        assertThat(biskelion.isTapped()).isTrue();
        assertThat(biskelion.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(knight.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(biskelion.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(knight.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Summoning sickness prevents activation of the tap ability")
    void cannotActivateWhileSummoningSick() {
        Permanent biskelion = harness.addToBattlefieldAndReturn(player1, new SerratedBiskelion());
        Permanent knight = harness.addToBattlefieldAndReturn(player2, new BenalishKnight());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, knight.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(biskelion.isTapped()).isFalse();
        assertThat(biskelion.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Biskelion cannot activate again")
    void cannotActivateAgainWhileTapped() {
        Permanent biskelion = addCreatureReady(player1, new SerratedBiskelion());
        Permanent knight = harness.addToBattlefieldAndReturn(player2, new BenalishKnight());

        harness.activateAbility(player1, 0, null, knight.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, knight.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(biskelion.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(knight.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An illegal target prevents both counters from being placed")
    void placesNoSourceCounterWhenTargetLeaves() {
        Permanent source = addCreatureReady(player1, new SerratedBiskelion());
        Permanent target = addCreatureReady(player2, new SerratedBiskelion());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.activateAbility(player2, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Serrated Biskelion");
        harness.assertNotOnBattlefield(player2, "Serrated Biskelion");

        harness.passBothPriorities();

        assertThat(source.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(source.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The target still gets its counter when the source leaves in response")
    void targetGetsCounterAfterSourceLeaves() {
        Permanent source = addCreatureReady(player1, new SerratedBiskelion());
        source.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        Permanent responder = addCreatureReady(player2, new SerratedBiskelion());
        Permanent knight = harness.addToBattlefieldAndReturn(player2, new BenalishKnight());

        harness.activateAbility(player1, 0, null, knight.getId());
        harness.activateAbility(player2, 0, null, source.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Serrated Biskelion");
        harness.assertNotOnBattlefield(player1, "Serrated Biskelion");
        assertThat(responder.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);

        harness.passBothPriorities();

        assertThat(knight.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The target gets its counter even when the source's counter is lethal")
    void lethalSourceCounterDoesNotPreventTargetCounter() {
        Permanent source = addCreatureReady(player1, new SerratedBiskelion());
        source.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        Permanent knight = harness.addToBattlefieldAndReturn(player2, new BenalishKnight());

        harness.activateAbility(player1, 0, null, knight.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Serrated Biskelion");
        harness.assertNotOnBattlefield(player1, "Serrated Biskelion");
        assertThat(knight.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Targeting itself places both counters together and triggers Hapatra once")
    void selfTargetingCreatesOnlyOneSnake() {
        Permanent biskelion = addCreatureReady(player1, new SerratedBiskelion());
        harness.addToBattlefield(player1, new HapatraVizierOfPoisons());

        harness.activateAbility(player1, 0, null, biskelion.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Serrated Biskelion");
        harness.assertNotOnBattlefield(player1, "Serrated Biskelion");
        assertThat(countPermanents(player1, "Snake")).isEqualTo(1);
    }

    @Test
    @DisplayName("Counters on two different creatures trigger Hapatra once for each creature")
    void differentCreaturesCreateTwoSnakes() {
        Permanent biskelion = addCreatureReady(player1, new SerratedBiskelion());
        Permanent knight = harness.addToBattlefieldAndReturn(player2, new BenalishKnight());
        harness.addToBattlefield(player1, new HapatraVizierOfPoisons());

        harness.activateAbility(player1, 0, null, knight.getId());
        resolveAllTriggers();

        assertThat(biskelion.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(knight.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(countPermanents(player1, "Snake")).isEqualTo(2);
    }
}
