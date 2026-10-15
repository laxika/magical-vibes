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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SlickshotShowOff.class, Shock.class, GrizzlyBears.class})
class SlickshotShowOffTest extends BaseCardTest {

    private Permanent addShowOff() {
        Permanent showOff = harness.addToBattlefieldAndReturn(player1, new SlickshotShowOff());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return showOff;
    }

    @Test
    @DisplayName("Casting a noncreature spell gives +2/+0 until end of turn")
    void noncreatureSpellPumps() {
        Permanent showOff = addShowOff();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

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

    @Test
    void multipleNoncreatureSpellsGiveCumulativeBoosts() {
        Permanent showOff = addShowOff();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, showOff)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, showOff)).isEqualTo(2);
    }

    @Test
    void opponentsNoncreatureSpellDoesNotPump() {
        Permanent showOff = addShowOff();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, showOff)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, showOff)).isEqualTo(2);
    }

    @Test
    void boostResolvesBeforeTheSpellThatTriggeredIt() {
        Permanent showOff = addShowOff();
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, showOff)).isEqualTo(3);
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    void plottedCardCanBeCastForFreeOnlyOnALaterTurn() {
        SlickshotShowOff showOff = new SlickshotShowOff();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(showOff));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, List.of());

        harness.assertNotInHand(player1, "Slickshot Show-Off");
        harness.assertNotOnBattlefield(player1, "Slickshot Show-Off");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThatThrownBy(() -> harness.castFromExile(player1, showOff.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.setHand(player2, List.of());
        assertThatThrownBy(() -> harness.castFromExile(player1, showOff.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.castFromExile(player1, showOff.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Slickshot Show-Off");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(showOff);
    }

    @Test
    void plottingIsUnavailableOutsideAMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new SlickshotShowOff()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Slickshot Show-Off");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void plottingDoesNotTriggerAnotherShowOff() {
        Permanent battlefieldShowOff = addShowOff();
        harness.setHand(player1, List.of(new SlickshotShowOff()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, battlefieldShowOff)).isEqualTo(1);
    }

    @Test
    void plottingIsUnavailableWhileASpellIsOnTheStack() {
        addShowOff();
        harness.setHand(player1, List.of(new Shock(), new SlickshotShowOff()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Slickshot Show-Off");
        resolveAllTriggers();
    }
}
