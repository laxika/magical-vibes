package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
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

@CardUsed({PacksFavor.class, GrizzlyBears.class, RagingGoblin.class, FountainOfYouth.class})
class PacksFavorTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature +3/+3 until end of turn")
    void boostsTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new PacksFavor()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(5);
        assertThat(bears.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Convoke taps a creature to help cast Pack's Favor")
    void castsWithConvoke() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent convokeCreature = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        harness.setHand(player1, List.of(new PacksFavor()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstantWithConvoke(player1, 0, List.of(target.getId()), List.of(convokeCreature.getId()));
        harness.passBothPriorities();

        assertThat(convokeCreature.isTapped()).isTrue();
        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new PacksFavor()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new PacksFavor()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, fountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Convoke can pay the entire cost and tap the summoning-sick target")
    void castsWithoutManaUsingTargetForConvoke() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent firstGoblin = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        Permanent secondGoblin = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        target.setSummoningSick(true);
        harness.setHand(player1, List.of(new PacksFavor()));

        harness.castInstantWithConvoke(player1, 0, List.of(target.getId()),
                List.of(target.getId(), firstGoblin.getId(), secondGoblin.getId()));
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(firstGoblin.isTapped()).isTrue();
        assertThat(secondGoblin.isTapped()).isTrue();
        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Can boost an opponent's tapped creature")
    void boostsOpponentsTappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();
        harness.setHand(player1, List.of(new PacksFavor()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Cannot use an already tapped creature for convoke")
    void cannotConvokeWithTappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.tap();
        harness.setHand(player1, List.of(new PacksFavor()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0,
                List.of(target.getId()), List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Red creatures cannot pay the green part of the convoke cost")
    void redConvokeCannotPayGreenCost() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        harness.setHand(player1, List.of(new PacksFavor()));

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0,
                List.of(first.getId()), List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(third.isTapped()).isFalse();
    }
}
