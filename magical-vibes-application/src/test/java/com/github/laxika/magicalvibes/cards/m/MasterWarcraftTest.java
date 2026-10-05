package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MasterWarcraft.class, BorosRecruit.class, JaceBeleren.class})
class MasterWarcraftTest extends BaseCardTest {

    private Permanent addAttacker(Player player) {
        return addCreatureReady(player, new BorosRecruit());
    }

    private Permanent addBlocker(Player player) {
        return addCreatureReady(player, new BorosRecruit());
    }

    private void castMasterWarcraft(Player caster) {
        harness.setHand(caster, List.of(new MasterWarcraft()));
        harness.addMana(caster, ManaColor.RED, 4);
        harness.castAndResolveInstant(caster, 0);
    }

    private void enterPrecombatMain(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("The controller chooses attackers and blockers for the turn")
    void controllerChoosesCombatDeclarations() {
        enterPrecombatMain(player1);
        Permanent firstAttacker = addAttacker(player1);
        Permanent secondAttacker = addAttacker(player1);
        Permanent blocker = addBlocker(player2);
        castMasterWarcraft(player1);

        harness.passUntil(player1, TurnStep.DECLARE_ATTACKERS);
        PendingInteraction.AttackerDeclaration attackerPrompt =
                gd.interaction.activeInteraction(PendingInteraction.AttackerDeclaration.class);
        assertThat(attackerPrompt).isNotNull();
        assertThat(attackerPrompt.decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(attackerPrompt.choosingForOpponent()).isFalse();

        int firstAttackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(firstAttacker);
        int secondAttackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(secondAttacker);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of(firstAttackerIndex)))
                .isInstanceOf(IllegalStateException.class);
        gs.declareAttackers(gd, player1, List.of(firstAttackerIndex));

        assertThat(firstAttacker.isAttacking()).isTrue();
        assertThat(secondAttacker.isAttacking()).isFalse();

        harness.clearPriorityPassed();
        harness.passBothPriorities();

        PendingInteraction.BlockerDeclaration blockerPrompt =
                gd.interaction.activeInteraction(PendingInteraction.BlockerDeclaration.class);
        assertThat(blockerPrompt).isNotNull();
        assertThat(blockerPrompt.decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(blockerPrompt.defenderId()).isEqualTo(player2.getId());
        assertThat(blockerPrompt.choosingForOpponent()).isTrue();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(blockerIndex, firstAttackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(blocker.getBlockingTargetIds()).contains(firstAttacker.getId());
    }

    @Test
    @DisplayName("The controller may choose not to assign any blockers")
    void controllerMayChooseNoBlockers() {
        enterPrecombatMain(player1);
        Permanent attacker = addAttacker(player1);
        Permanent blocker = addBlocker(player2);
        castMasterWarcraft(player1);

        harness.passUntil(player1, TurnStep.DECLARE_ATTACKERS);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareAttackers(gd, player1, List.of(attackerIndex));

        harness.clearPriorityPassed();
        harness.passBothPriorities();

        PendingInteraction.BlockerDeclaration blockerPrompt =
                gd.interaction.activeInteraction(PendingInteraction.BlockerDeclaration.class);
        assertThat(blockerPrompt).isNotNull();
        assertThat(blockerPrompt.decidingPlayerId()).isEqualTo(player1.getId());
        gs.declareBlockers(gd, player1, List.of());

        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("A non-active controller chooses attackers from the active player's battlefield")
    void nonActiveControllerChoosesAttackers() {
        enterPrecombatMain(player2);
        Permanent attacker = addAttacker(player2);
        addBlocker(player1);
        castMasterWarcraft(player1);

        harness.passUntil(player2, TurnStep.DECLARE_ATTACKERS);
        PendingInteraction.AttackerDeclaration attackerPrompt =
                gd.interaction.activeInteraction(PendingInteraction.AttackerDeclaration.class);
        assertThat(attackerPrompt).isNotNull();
        assertThat(attackerPrompt.activePlayerId()).isEqualTo(player2.getId());
        assertThat(attackerPrompt.decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(attackerPrompt.choosingForOpponent()).isTrue();

        int attackerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of(attackerIndex)))
                .isInstanceOf(IllegalStateException.class);
        gs.declareAttackers(gd, player1, List.of(attackerIndex));

        assertThat(attacker.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("The controller chooses attackers in every combat phase of the turn")
    void controllerChoosesAttackersInEveryCombatPhaseOfTurn() {
        enterPrecombatMain(player2);
        Permanent firstAttacker = addAttacker(player2);
        Permanent secondAttacker = addAttacker(player2);
        addBlocker(player1);
        castMasterWarcraft(player1);

        harness.passUntil(player2, TurnStep.DECLARE_ATTACKERS);
        PendingInteraction.AttackerDeclaration firstPrompt =
                gd.interaction.activeInteraction(PendingInteraction.AttackerDeclaration.class);
        assertThat(firstPrompt.decidingPlayerId()).isEqualTo(player1.getId());

        int firstAttackerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(firstAttacker);
        gs.declareAttackers(gd, player1, List.of(firstAttackerIndex));

        assertThat(firstAttacker.isAttacking()).isTrue();
        assertThat(secondAttacker.isAttacking()).isFalse();

        gd.additionalCombatPhasesOnly = 1;
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);

        gs.advanceStep(gd);
        PendingInteraction.AttackerDeclaration secondPrompt =
                gd.interaction.activeInteraction(PendingInteraction.AttackerDeclaration.class);
        assertThat(secondPrompt).isNotNull();
        assertThat(secondPrompt.decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(secondPrompt.choosingForOpponent()).isTrue();
    }

    @Test
    @DisplayName("The controller chooses blockers in every combat phase of the turn")
    void controllerChoosesBlockersInEveryCombatPhaseOfTurn() {
        enterPrecombatMain(player1);
        Permanent firstAttacker = addAttacker(player1);
        Permanent secondAttacker = addAttacker(player1);
        addBlocker(player2);
        castMasterWarcraft(player1);

        harness.passUntil(player1, TurnStep.DECLARE_ATTACKERS);
        int firstAttackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(firstAttacker);
        gs.declareAttackers(gd, player1, List.of(firstAttackerIndex));

        gd.additionalCombatPhasesOnly = 1;
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);
        gs.advanceStep(gd);

        int secondAttackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(secondAttacker);
        gs.declareAttackers(gd, player1, List.of(secondAttackerIndex));

        PendingInteraction.BlockerDeclaration secondPrompt =
                gd.interaction.activeInteraction(PendingInteraction.BlockerDeclaration.class);
        assertThat(secondPrompt).isNotNull();
        assertThat(secondPrompt.decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(secondPrompt.choosingForOpponent()).isTrue();
        assertThat(secondPrompt.defenderId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Master Warcraft cannot be cast after attackers are declared")
    void cannotBeCastAfterAttackersAreDeclared() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new MasterWarcraft()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Master Warcraft cannot be cast before a later combat's attackers")
    void cannotBeCastBeforeLaterCombatAttackers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        gd.combatPhasesThisTurn = 2;
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new MasterWarcraft()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The non-active controller may choose no attackers")
    void nonActiveControllerMayChooseNoAttackers() {
        enterPrecombatMain(player2);
        Permanent attacker = addAttacker(player2);
        castMasterWarcraft(player1);

        harness.passUntil(player2, TurnStep.DECLARE_ATTACKERS);
        gs.declareAttackers(gd, player1, List.of());

        assertThat(attacker.isAttacking()).isFalse();
        assertThat(attacker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Choosing attackers does not allow a tapped creature to attack")
    void cannotChooseTappedAttacker() {
        enterPrecombatMain(player2);
        Permanent attacker = addAttacker(player2);
        Permanent tappedCreature = addAttacker(player2);
        tappedCreature.setTapped(true);
        castMasterWarcraft(player1);

        harness.passUntil(player2, TurnStep.DECLARE_ATTACKERS);
        int tappedIndex = gd.playerBattlefields.get(player2.getId()).indexOf(tappedCreature);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(tappedIndex)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(attacker.isAttacking()).isFalse();
        assertThat(tappedCreature.isAttacking()).isFalse();
    }

    @Test
    @CardUsed({MasterWarcraft.class, BorosRecruit.class, JaceBeleren.class})
    @DisplayName("Choosing the opponent's attackers does not grant choice of attack destinations")
    void nonActiveControllerCannotChooseAttackDestinations() {
        enterPrecombatMain(player2);
        Permanent attacker = addAttacker(player2);
        addBlocker(player1);
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        castMasterWarcraft(player1);

        harness.passUntil(player2, TurnStep.DECLARE_ATTACKERS);
        int attackerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(attacker);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(attackerIndex),
                Map.of(attackerIndex, planeswalker.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(attacker.isAttacking()).isFalse();
    }
}
