package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VoraciousHydra.class, GreenwoodSentinel.class})
class VoraciousHydraTest extends BaseCardTest {

    @Test
    void doubleCountersModeDoublesTheCountersItEnteredWith() {
        castHydra(0, 3, null);
        harness.passBothPriorities();

        assertThat(findHydra().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    void fightModeFightsTheChosenCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());

        castHydra(1, 3, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Greenwood Sentinel");
        assertThat(findHydra().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(findHydra().getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void fightModeCannotTargetACreatureYouControl() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        castHydraSpell(3);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "This creature fights target creature you don't control");

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, opponent.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Greenwood Sentinel");
        harness.assertInGraveyard(player2, "Greenwood Sentinel");
    }

    private void castHydra(int modeIndex, int xValue, UUID targetId) {
        castHydraSpell(xValue);
        harness.passBothPriorities();
        harness.handleListChoice(player1, modeIndex == 0
                ? "Double the number of +1/+1 counters on this creature"
                : "This creature fights target creature you don't control");
        if (targetId != null) {
            harness.handlePermanentChosen(player1, targetId);
        }
    }

    private void castHydraSpell(int xValue) {
        harness.setHand(player1, List.of(new VoraciousHydra()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
        gs.playCard(gd, player1, 0, xValue, null, null);
    }

    @Test
    void zeroXCanDoubleZeroCountersAndSurvive() {
        castHydra(0, 0, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Voracious Hydra");
        assertThat(findHydra().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void fightCanKillHydraWhileItsOpponentSurvives() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        castHydra(1, 1, sentinel.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Voracious Hydra");
        harness.assertOnBattlefield(player2, "Greenwood Sentinel");
        assertThat(sentinel.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void doubleCountsCountersAtResolution() {
        castHydra(0, 3, null);
        findHydra().getCounters().put(CounterType.PLUS_ONE_PLUS_ONE, 4);
        harness.passBothPriorities();

        assertThat(findHydra().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(8);
    }

    @Test
    void entryWithoutCastingStillAllowsChoosingFight() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        harness.enterBattlefieldAndReturn(player1, new VoraciousHydra());
        harness.handleListChoice(player1, "This creature fights target creature you don't control");
        harness.handlePermanentChosen(player1, sentinel.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Voracious Hydra");
        harness.assertOnBattlefield(player2, "Greenwood Sentinel");
        assertThat(sentinel.getMarkedDamage()).isZero();
    }

    private Permanent findHydra() {
        return findPermanent(player1, "Voracious Hydra");
    }
}
