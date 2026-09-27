package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.k.Karakas;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TuknirDeathlock.class, Karakas.class})
class TuknirDeathlockTest extends BaseCardTest {

    @Test
    @DisplayName("Gives any target creature +2/+2 until end of turn")
    void boostsTargetCreature() {
        Permanent tuknir = addCreatureReady(player1, new TuknirDeathlock());
        Permanent target = addCreatureReady(player2, new TuknirDeathlock());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(tuknir.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, tuknir)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, tuknir)).isEqualTo(2);
    }

    @Test
    @DisplayName("The creature boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new TuknirDeathlock());
        Permanent target = addCreatureReady(player2, new TuknirDeathlock());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new TuknirDeathlock());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Karakas());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires both red and green mana")
    void requiresBothRedAndGreenMana() {
        addCreatureReady(player1, new TuknirDeathlock());
        Permanent target = addCreatureReady(player2, new TuknirDeathlock());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate the tap ability while Tuknir Deathlock is tapped")
    void cannotActivateWhileTapped() {
        Permanent tuknir = addCreatureReady(player1, new TuknirDeathlock());
        Permanent firstTarget = addCreatureReady(player2, new TuknirDeathlock());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, firstTarget.getId());
        harness.passBothPriorities();

        Permanent secondTarget = addCreatureReady(player2, new TuknirDeathlock());
        addAbilityMana();

        assertThat(tuknir.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, secondTarget.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
