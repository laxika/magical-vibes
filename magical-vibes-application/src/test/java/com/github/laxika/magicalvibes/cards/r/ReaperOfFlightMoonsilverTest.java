package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReaperOfFlightMoonsilverTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature gives it +2/+1 until end of turn with delirium")
    void sacrificeAnotherCreatureBoostsReaper() {
        setDelirium();
        Permanent reaper = addCreatureReady(player1, new ReaperOfFlightMoonsilver());
        addCreatureReady(player1, new GrizzlyBears());
        prepareActivation();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, reaper)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, reaper)).isEqualTo(4);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        setDelirium();
        Permanent reaper = addCreatureReady(player1, new ReaperOfFlightMoonsilver());
        addCreatureReady(player1, new GrizzlyBears());
        prepareActivation();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, reaper)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, reaper)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot activate without delirium")
    void cannotActivateWithoutDelirium() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new Shock()));
        addCreatureReady(player1, new ReaperOfFlightMoonsilver());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        prepareActivation();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
    }

    @Test
    @DisplayName("Cannot sacrifice Reaper of Flight Moonsilver itself")
    void cannotSacrificeItself() {
        setDelirium();
        addCreatureReady(player1, new ReaperOfFlightMoonsilver());
        prepareActivation();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void setDelirium() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Millstone()));
    }

    private void prepareActivation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
