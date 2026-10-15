package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BannerhideKrushok;
import com.github.laxika.magicalvibes.cards.s.SolTalisman;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({TragicFall.class, BannerhideKrushok.class, SolTalisman.class})
class TragicFallTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature -3/-3 when the controller has cards in hand")
    void givesMinusThreeMinusThreeWithCardsInHand() {
        Permanent target = addFourFourTarget();
        harness.setHand(player1, List.of(new TragicFall(), new BannerhideKrushok()));
        addMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(-3);
        assertThat(target.getToughnessModifier()).isEqualTo(-3);
        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Gives target creature -13/-13 with an empty hand")
    void givesMinusThirteenMinusThirteenWithEmptyHand() {
        Permanent target = addFourFourTarget();
        harness.setHand(player1, List.of(new TragicFall()));
        addMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Bannerhide Krushok");
        harness.assertInGraveyard(player2, "Bannerhide Krushok");
    }

    @Test
    @DisplayName("Checks hellbent when the spell resolves")
    void checksHellbentAtResolution() {
        Permanent target = addFourFourTarget();
        harness.setHand(player1, List.of(new TragicFall()));
        addMana();

        harness.castInstant(player1, 0, target.getId());
        harness.setHand(player1, List.of(new BannerhideKrushok()));
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-3);
        assertThat(target.getToughnessModifier()).isEqualTo(-3);
    }

    @Test
    @DisplayName("The debuff wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        Permanent target = addFourFourTarget();
        harness.setHand(player1, List.of(new TragicFall(), new BannerhideKrushok()));
        addMana();

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new SolTalisman());
        harness.setHand(player1, List.of(new TragicFall()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Hellbent replaces the base debuff with exactly -13/-13")
    void hellbentAppliesExactlyMinusThirteen() {
        Permanent target = addFourFourTarget();
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 10);
        harness.setHand(player1, List.of(new TragicFall()));
        harness.setHand(player2, List.of(new BannerhideKrushok()));
        addMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Bannerhide Krushok");
        assertThat(target.getPowerModifier()).isEqualTo(-13);
        assertThat(target.getToughnessModifier()).isEqualTo(-13);
        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.getEffectivePower()).isEqualTo(14);
        assertThat(target.getEffectiveToughness()).isEqualTo(14);
    }

    @Test
    @DisplayName("Hellbent turns on when the last card leaves hand before resolution")
    void hellbentTurnsOnBeforeResolution() {
        Permanent target = addFourFourTarget();
        harness.setHand(player1, List.of(new TragicFall(), new BannerhideKrushok()));
        addMana();

        harness.castInstant(player1, 0, target.getId());
        harness.setHand(player1, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Bannerhide Krushok");
        harness.assertInGraveyard(player2, "Bannerhide Krushok");
    }

    @Test
    @DisplayName("Can target a creature controlled by the spell controller")
    void canTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BannerhideKrushok());
        harness.setHand(player1, List.of(new TragicFall(), new BannerhideKrushok()));
        harness.setHand(player2, List.of());
        addMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player1, "Bannerhide Krushok");
        assertThat(target.getPowerModifier()).isEqualTo(-3);
        assertThat(target.getToughnessModifier()).isEqualTo(-3);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
    }

    private Permanent addFourFourTarget() {
        return harness.addToBattlefieldAndReturn(player2, new BannerhideKrushok());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
