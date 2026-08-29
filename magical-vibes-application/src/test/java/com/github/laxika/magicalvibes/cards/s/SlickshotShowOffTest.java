package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed({SlickshotShowOff.class, Shock.class, GrizzlyBears.class})
class SlickshotShowOffTest extends BaseCardTest {

    private Permanent addShowOff() {
        harness.addToBattlefield(player1, new SlickshotShowOff());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }

    @Test
    @DisplayName("Casting a noncreature spell gives +2/+0 until end of turn")
    void noncreatureSpellPumps() {
        Permanent showOff = addShowOff();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, showOff)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, showOff)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, showOff)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger the boost")
    void creatureSpellDoesNotPump() {
        Permanent showOff = addShowOff();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        assertThat(gqs.getEffectivePower(gd, showOff)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, showOff)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can be plotted for its plot cost")
    void canBePlottedForItsPlotCost() {
        SlickshotShowOff showOff = new SlickshotShowOff();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(showOff));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, List.of());

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(showOff);
        assertThat(gd.plottedCardIds).contains(showOff.getId());
    }
}
