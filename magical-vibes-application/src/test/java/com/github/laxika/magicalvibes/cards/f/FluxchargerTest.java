package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FluxchargerTest extends BaseCardTest {

    private Permanent addFluxcharger(Player player) {
        harness.addToBattlefield(player, new Fluxcharger());
        return gd.playerBattlefields.get(player.getId()).getLast();
    }

    private void setUpMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Accepting the trigger after an instant switches power and toughness")
    void acceptingSwitchesPowerAndToughness() {
        Permanent fluxcharger = addFluxcharger(player1);
        setUpMainPhase(player1);

        assertThat(gqs.getEffectivePower(gd, fluxcharger)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, fluxcharger)).isEqualTo(5);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve Shock, trigger goes on stack
        harness.passBothPriorities(); // resolve trigger -> may prompt

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, fluxcharger)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, fluxcharger)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the trigger leaves power and toughness unchanged")
    void decliningLeavesStatsUnchanged() {
        Permanent fluxcharger = addFluxcharger(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, fluxcharger)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, fluxcharger)).isEqualTo(5);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger Fluxcharger")
    void creatureSpellDoesNotTrigger() {
        Permanent fluxcharger = addFluxcharger(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, fluxcharger)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, fluxcharger)).isEqualTo(5);
    }

    @Test
    @DisplayName("The switch wears off at end of turn")
    void switchWearsOffAtEndOfTurn() {
        Permanent fluxcharger = addFluxcharger(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, fluxcharger)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, fluxcharger)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, fluxcharger)).isEqualTo(5);
    }
}
