package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.e.ExplosiveApparatus;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MagmaticChasm;
import com.github.laxika.magicalvibes.cards.t.ThrabenInspector;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReaperOfFlightMoonsilver.class, ThrabenInspector.class, Forest.class, MagmaticChasm.class, ExplosiveApparatus.class})
class ReaperOfFlightMoonsilverTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature gives it +2/+1 until end of turn with delirium")
    void sacrificeAnotherCreatureBoostsReaper() {
        setDelirium();
        Permanent reaper = addCreatureReady(player1, new ReaperOfFlightMoonsilver());
        addCreatureReady(player1, new ThrabenInspector());
        prepareActivation();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, reaper)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, reaper)).isEqualTo(4);
        harness.assertInGraveyard(player1, "Thraben Inspector");
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        setDelirium();
        Permanent reaper = addCreatureReady(player1, new ReaperOfFlightMoonsilver());
        addCreatureReady(player1, new ThrabenInspector());
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
        harness.setGraveyard(player1, List.of(new ThrabenInspector(), new Forest(), new MagmaticChasm()));
        addCreatureReady(player1, new ReaperOfFlightMoonsilver());
        Permanent inspector = addCreatureReady(player1, new ThrabenInspector());
        prepareActivation();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(inspector);
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

    @Test
    @DisplayName("Sacrifice is paid before resolution, and losing delirium does not stop the boost")
    void sacrificeIsUpfrontAndDeliriumIsNotRecheckedAtResolution() {
        setDelirium();
        Permanent reaper = addCreatureReady(player1, new ReaperOfFlightMoonsilver());
        Permanent fodder = addCreatureReady(player1, new ThrabenInspector());
        prepareActivation();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(fodder);
        harness.assertInGraveyard(player1, "Thraben Inspector");
        assertThat(gqs.getEffectivePower(gd, reaper)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, reaper)).isEqualTo(3);
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, reaper)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, reaper)).isEqualTo(4);
    }

    @Test
    @DisplayName("Repeated activations stack and boost only the source")
    void repeatedActivationsBoostOnlySource() {
        setDelirium();
        Permanent reaper = addCreatureReady(player1, new ReaperOfFlightMoonsilver());
        Permanent otherReaper = addCreatureReady(player1, new ReaperOfFlightMoonsilver());
        Permanent firstFodder = addCreatureReady(player1, new ThrabenInspector());
        Permanent secondFodder = addCreatureReady(player1, new ThrabenInspector());
        prepareActivation();

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, firstFodder.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, secondFodder.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, reaper)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, reaper)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, otherReaper)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, otherReaper)).isEqualTo(3);
    }

    @Test
    @DisplayName("Sacrificing a creature cannot supply the fourth card type needed to activate")
    void cannotEnableDeliriumByPayingSacrificeCost() {
        harness.setGraveyard(player1, List.of(new Forest(), new MagmaticChasm(), new ExplosiveApparatus()));
        addCreatureReady(player1, new ReaperOfFlightMoonsilver());
        Permanent fodder = addCreatureReady(player1, new ThrabenInspector());
        prepareActivation();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fodder);
        harness.assertNotInGraveyard(player1, "Thraben Inspector");
    }

    @Test
    @DisplayName("An opponent's graveyard cannot enable delirium")
    void cannotUseOpponentsGraveyard() {
        harness.setGraveyard(player2, List.of(
                new ThrabenInspector(), new Forest(), new MagmaticChasm(), new ExplosiveApparatus()));
        addCreatureReady(player1, new ReaperOfFlightMoonsilver());
        Permanent fodder = addCreatureReady(player1, new ThrabenInspector());
        prepareActivation();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fodder);
    }

    @Test
    @DisplayName("An opponent's creature cannot be sacrificed to pay the cost")
    void cannotSacrificeOpponentsCreature() {
        setDelirium();
        addCreatureReady(player1, new ReaperOfFlightMoonsilver());
        Permanent opponentsCreature = addCreatureReady(player2, new ThrabenInspector());
        prepareActivation();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentsCreature);
    }

    @Test
    @DisplayName("Four cards with only three distinct card types do not enable delirium")
    void duplicateTypesDoNotEnableDelirium() {
        harness.setGraveyard(player1, List.of(
                new ThrabenInspector(), new ReaperOfFlightMoonsilver(), new Forest(), new MagmaticChasm()));
        addCreatureReady(player1, new ReaperOfFlightMoonsilver());
        Permanent fodder = addCreatureReady(player1, new ThrabenInspector());
        prepareActivation();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fodder);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Reaper can activate on the opponent's turn")
    void canActivateWhileTappedAndSummoningSickOnOpponentsTurn() {
        setDelirium();
        Permanent reaper = harness.addToBattlefieldAndReturn(player1, new ReaperOfFlightMoonsilver());
        reaper.setTapped(true);
        reaper.setSummoningSick(true);
        addCreatureReady(player1, new ThrabenInspector());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, reaper)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, reaper)).isEqualTo(4);
        assertThat(reaper.isTapped()).isTrue();
    }

    private void setDelirium() {
        harness.setGraveyard(player1, List.of(
                new ThrabenInspector(), new Forest(), new MagmaticChasm(), new ExplosiveApparatus()));
    }

    private void prepareActivation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
