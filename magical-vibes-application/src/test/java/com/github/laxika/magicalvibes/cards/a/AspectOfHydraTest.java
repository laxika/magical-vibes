package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.n.NyleasDisciple;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Aspect of Hydra")
class AspectOfHydraTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets +X/+X equal to green devotion")
    void targetCreatureGetsBoostEqualToGreenDevotion() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        harness.addToBattlefield(player1, new NyleasDisciple());
        harness.addToBattlefield(player1, new NyleasDisciple());
        castAspect(target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
    }

    @Test
    @DisplayName("Green devotion from an opponent is not counted")
    void doesNotCountOpponentsGreenDevotion() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        harness.addToBattlefield(player2, new NyleasDisciple());
        castAspect(target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("The boost expires at end of turn")
    void boostExpiresAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        harness.addToBattlefield(player1, new NyleasDisciple());
        castAspect(target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    private void castAspect(Permanent target) {
        harness.setHand(player1, List.of(new AspectOfHydra()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, List.of(target.getId()));
        harness.passBothPriorities();
    }
}
