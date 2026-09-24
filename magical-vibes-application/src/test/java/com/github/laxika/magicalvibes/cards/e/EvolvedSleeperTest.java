package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(EvolvedSleeper.class)
class EvolvedSleeperTest extends BaseCardTest {

    @Test
    void abilitiesAdvanceEvolvedSleeperAndFinalAbilityDrawsAndLosesLife() {
        Permanent sleeper = addSleeper();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new EvolvedSleeper()));

        assertThat(gqs.getEffectivePower(gd, sleeper)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, sleeper)).isEqualTo(1);

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, sleeper)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sleeper)).isEqualTo(2);
        assertThat(sleeper.getGrantedSubtypes()).contains(CardSubtype.CLERIC);

        resetPriority();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, sleeper)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sleeper)).isEqualTo(3);
        assertThat(sleeper.getCounterCount(CounterType.DEATHTOUCH)).isEqualTo(1);
        assertThat(sleeper.getGrantedSubtypes()).contains(CardSubtype.PHYREXIAN);

        resetPriority();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(sleeper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    void gatedAbilitiesDoNothingBeforeRequiredSubtype() {
        Permanent sleeper = addSleeper();

        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, sleeper)).isEqualTo(1);
        assertThat(sleeper.getCounterCount(CounterType.DEATHTOUCH)).isZero();

        resetPriority();
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(sleeper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addSleeper() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return harness.addToBattlefieldAndReturn(player1, new EvolvedSleeper());
    }

    private void resetPriority() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
