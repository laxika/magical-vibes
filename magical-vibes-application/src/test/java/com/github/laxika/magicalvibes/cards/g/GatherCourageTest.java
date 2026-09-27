package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BrambleElemental;
import com.github.laxika.magicalvibes.cards.b.BorosSignet;
import com.github.laxika.magicalvibes.cards.w.Watchwolf;
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

@CardUsed({GatherCourage.class, Watchwolf.class, BrambleElemental.class, BorosSignet.class})
class GatherCourageTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Gather Courage gives +2/+2 to target creature")
    void resolvesAndBoostsTarget() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new Watchwolf());
        harness.setHand(player1, List.of(new GatherCourage()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, wolf.getId());
        harness.passBothPriorities();

        assertThat(wolf.getEffectivePower()).isEqualTo(5);
        assertThat(wolf.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Boost from Gather Courage wears off at end of turn")
    void boostWearsOff() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new Watchwolf());
        harness.setHand(player1, List.of(new GatherCourage()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, wolf.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(wolf.getEffectivePower()).isEqualTo(3);
        assertThat(wolf.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Convoke lets a tapped creature pay for Gather Courage with no mana")
    void castsWithConvoke() {
        Permanent boost = harness.addToBattlefieldAndReturn(player1, new BrambleElemental());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Watchwolf());
        harness.setHand(player1, List.of(new GatherCourage()));

        harness.castInstantWithConvoke(player1, 0, List.of(target.getId()), List.of(boost.getId()));

        assertThat(boost.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Convoke can use the green color of a multicolored creature")
    void convokeUsesAnyColorOfMulticoloredCreature() {
        Permanent boost = harness.addToBattlefieldAndReturn(player1, new Watchwolf());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Watchwolf());
        harness.setHand(player1, List.of(new GatherCourage()));

        harness.castInstantWithConvoke(player1, 0, List.of(target.getId()), List.of(boost.getId()));

        assertThat(boost.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Gather Courage can target a creature an opponent controls")
    void targetsOpponentsCreature() {
        Permanent opponentWolf = harness.addToBattlefieldAndReturn(player2, new Watchwolf());
        harness.setHand(player1, List.of(new GatherCourage()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, opponentWolf.getId());
        harness.passBothPriorities();

        assertThat(opponentWolf.getEffectivePower()).isEqualTo(5);
        assertThat(opponentWolf.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Gather Courage")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new Watchwolf());
        Permanent signet = harness.addToBattlefieldAndReturn(player1, new BorosSignet());
        harness.setHand(player1, List.of(new GatherCourage()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, signet.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
