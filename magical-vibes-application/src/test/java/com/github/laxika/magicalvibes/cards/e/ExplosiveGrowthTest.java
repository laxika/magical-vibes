package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExplosiveGrowth.class, GrizzlyBears.class, FountainOfYouth.class})
class ExplosiveGrowthTest extends BaseCardTest {

    @Test
    void givesTargetCreaturePlusTwoPlusTwoWithoutKicker() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ExplosiveGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        Permanent resolvedBear = findPermanent(player1, "Grizzly Bears");
        assertThat(resolvedBear.getEffectivePower()).isEqualTo(4);
        assertThat(resolvedBear.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    void givesTargetCreaturePlusFivePlusFiveWithKicker() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ExplosiveGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castKickedInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        Permanent resolvedBear = findPermanent(player1, "Grizzly Bears");
        assertThat(resolvedBear.getEffectivePower()).isEqualTo(7);
        assertThat(resolvedBear.getEffectiveToughness()).isEqualTo(7);
    }

    @Test
    void canTargetCreatureAnOpponentControls() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ExplosiveGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        Permanent resolvedBear = findPermanent(player2, "Grizzly Bears");
        assertThat(resolvedBear.getEffectivePower()).isEqualTo(4);
        assertThat(resolvedBear.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    void boostWearsOffAtEndOfTurn() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ExplosiveGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent resolvedBear = findPermanent(player1, "Grizzly Bears");
        assertThat(resolvedBear.getEffectivePower()).isEqualTo(2);
        assertThat(resolvedBear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new ExplosiveGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, fountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
