package com.github.laxika.magicalvibes.cards.d;

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

@CardUsed({DeathPulse.class, GrizzlyBears.class})
class DeathPulseTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature -4/-4")
    void givesTargetCreatureMinusFourMinusFour() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DeathPulse()));
        addSpellMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cycling gives a target creature -1/-1 and draws a card")
    void cyclingDebuffsCreatureAndDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DeathPulse()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addCyclingMana();

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.getToughnessModifier()).isEqualTo(-1);
        harness.assertInGraveyard(player1, "Death Pulse");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cycling may be declined and still draws a card")
    void cyclingMayBeDeclined() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DeathPulse()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addCyclingMana();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cycling debuff wears off at end of turn")
    void cyclingDebuffWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DeathPulse()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addCyclingMana();

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();
        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.getToughnessModifier()).isEqualTo(-1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The main spell cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new DeathPulse()));
        addSpellMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addSpellMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void addCyclingMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
