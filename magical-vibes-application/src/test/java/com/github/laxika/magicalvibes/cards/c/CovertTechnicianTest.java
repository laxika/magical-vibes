package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.s.SolemnSimulacrum;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CovertTechnician.class, GrizzlyBears.class, MindStone.class, SolemnSimulacrum.class})
class CovertTechnicianTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage offers an artifact card with mana value at most the damage dealt")
    void combatDamageFiltersArtifactsByDamage() {
        harness.setHand(player1, List.of(new MindStone(), new SolemnSimulacrum(), new GrizzlyBears()));
        attackAndResolveTrigger();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.HandCardChoice choice = gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(0);
    }

    @Test
    @DisplayName("Accepting the trigger puts the chosen artifact onto the battlefield")
    void acceptingTriggerPutsArtifactOntoBattlefield() {
        harness.setHand(player1, List.of(new MindStone(), new SolemnSimulacrum()));
        attackAndResolveTrigger();
        harness.handleMayAbilityChosen(player1, true);

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Mind Stone");
        harness.assertInHand(player1, "Solemn Simulacrum");
    }

    @Test
    @DisplayName("Declining the trigger leaves the hand unchanged")
    void decliningTriggerLeavesHandUnchanged() {
        harness.setHand(player1, List.of(new MindStone()));
        attackAndResolveTrigger();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Mind Stone");
        harness.assertNotOnBattlefield(player1, "Mind Stone");
    }

    @Test
    @DisplayName("Ninjutsu puts Covert Technician onto the battlefield tapped and attacking")
    void ninjutsuSwapsTheUnblockedAttacker() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CovertTechnician()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, attacker.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        Permanent technician = findPermanent(player1, "Covert Technician");
        assertThat(technician.isTapped()).isTrue();
        assertThat(technician.isAttacking()).isTrue();
        assertThat(technician.getAttackTarget()).isEqualTo(player2.getId());
    }

    private void attackAndResolveTrigger() {
        addCreatureReady(player1, new CovertTechnician());
        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();
    }
}
