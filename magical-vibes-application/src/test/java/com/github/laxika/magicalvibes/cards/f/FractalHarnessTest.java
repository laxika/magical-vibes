package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FractalHarness.class, GrizzlyBears.class})
class FractalHarnessTest extends BaseCardTest {

    @Test
    void createsAndAttachesFractalWithXPlusOneCounters() {
        castHarness(3);

        Permanent harnessPermanent = findPermanent(player1, "Fractal Harness");
        Permanent fractal = findPermanent(player1, "Fractal");

        assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(harnessPermanent.getAttachedTo()).isEqualTo(fractal.getId());
    }

    @Test
    void doublesEquippedFractalsCountersWhenTheyAttack() {
        castHarness(2);
        Permanent fractal = findPermanent(player1, "Fractal");
        fractal.setSummoningSick(false);
        int fractalIndex = gd.playerBattlefields.get(player1.getId()).indexOf(fractal);

        declareAttackers(List.of(fractalIndex));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void equipAbilityCanMoveHarnessToAnotherCreature() {
        castHarness(1);
        Permanent harnessPermanent = findPermanent(player1, "Fractal Harness");
        Permanent fractal = findPermanent(player1, "Fractal");
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(harnessPermanent.getAttachedTo()).isEqualTo(bear.getId());
        assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void castHarness(int xValue) {
        harness.setHand(player1, List.of(new FractalHarness()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue + 2);
        harness.castArtifact(player1, 0, xValue);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
