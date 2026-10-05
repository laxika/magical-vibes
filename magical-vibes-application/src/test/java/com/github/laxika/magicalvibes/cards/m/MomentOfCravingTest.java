package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MomentOfCraving.class, GrizzlyBears.class, AirElemental.class, FountainOfYouth.class})
class MomentOfCravingTest extends BaseCardTest {

    @Test
    @DisplayName("Gives a creature -2/-2 and gains 2 life")
    void weakensCreatureAndGainsLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new MomentOfCraving()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("The -2/-2 wears off at end of turn when the creature survives")
    void boostWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new MomentOfCraving()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(-2);
        assertThat(target.getToughnessModifier()).isEqualTo(-2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new MomentOfCraving()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can weaken your own creature and gain life")
    void canTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new MomentOfCraving()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("No life is gained if the only target leaves before resolution")
    void noLifeGainWhenTargetLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new MomentOfCraving()));
        harness.setHand(player2, List.of(new MomentOfCraving()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 22);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Moment of Craving");
        assertThat(gd.stack).isEmpty();
    }
}
