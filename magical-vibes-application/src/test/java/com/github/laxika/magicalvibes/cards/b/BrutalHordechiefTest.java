package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.ArashinCleric;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrutalHordechief.class, ArashinCleric.class})
class BrutalHordechiefTest extends BaseCardTest {

    @Test
    @DisplayName("Each attacking creature makes the defending player lose 1 life and its controller gain 1 life")
    void attackTriggerDrainsDefendingPlayer() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);
        addCreatureReady(player1, new BrutalHordechief());
        addCreatureReady(player1, new ArashinCleric());
        addCreatureReady(player2, new ArashinCleric());
        addCreatureReady(player2, new ArashinCleric());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(8);
    }

    @Test
    @DisplayName("Activated ability makes the controller choose how opposing creatures block")
    void activatedAbilityGivesControllerBlockChoice() {
        addCreatureReady(player1, new BrutalHordechief());
        addCreatureReady(player1, new ArashinCleric());
        Permanent blocker = addCreatureReady(player2, new ArashinCleric());

        activateBlockControl(ManaColor.RED, ManaColor.RED);

        declareAttackersToBlockers(player1, List.of(1));

        PendingInteraction.BlockerDeclaration pending =
                gd.interaction.activeInteraction(PendingInteraction.BlockerDeclaration.class);
        assertThat(pending).isNotNull();
        assertThat(pending.decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(pending.defenderId()).isEqualTo(player2.getId());
        assertThat(pending.choosingForOpponent()).isTrue();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class);

        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A creature attacking without Hordechief still triggers the drain")
    void nonattackingHordechiefTriggersForAnotherAttacker() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);
        addCreatureReady(player1, new BrutalHordechief());
        addCreatureReady(player1, new ArashinCleric());
        addCreatureReady(player2, new ArashinCleric());

        declareAttackersToBlockers(player1, List.of(1));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(11);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(9);
    }

    @Test
    @DisplayName("An opponent's attacking creature does not trigger Hordechief")
    void opposingAttackerDoesNotTriggerDrain() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);
        addCreatureReady(player1, new BrutalHordechief());
        addCreatureReady(player2, new ArashinCleric());

        declareAttackersToBlockers(player2, List.of(0));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("The attack trigger resolves after Hordechief leaves the battlefield")
    void attackTriggerSurvivesSourceLeaving() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);
        Permanent hordechief = addCreatureReady(player1, new BrutalHordechief());
        addCreatureReady(player1, new ArashinCleric());
        addCreatureReady(player2, new ArashinCleric());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(1)));
        gd.playerBattlefields.get(player1.getId()).remove(hordechief);
        gd.playerGraveyards.get(player1.getId()).add(hordechief.getCard());
        harness.passUntil(player1, TurnStep.DECLARE_BLOCKERS);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(11);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(9);
    }

    @Test
    @DisplayName("Every able opposing creature must block, while tapped creatures need not")
    void activatedAbilityRequiresAllAbleBlockers() {
        addCreatureReady(player1, new BrutalHordechief());
        addCreatureReady(player1, new ArashinCleric());
        addCreatureReady(player2, new ArashinCleric());
        addCreatureReady(player2, new ArashinCleric());
        Permanent tappedBlocker = addCreatureReady(player2, new ArashinCleric());
        tappedBlocker.setTapped(true);
        activateBlockControl(ManaColor.WHITE, ManaColor.WHITE);

        declareAttackersToBlockers(player1, List.of(1));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class);
        gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(0, 1), new BlockerAssignment(1, 1)));
        assertThat(tappedBlocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Creatures entering after the activated ability resolves must also block")
    void laterEnteringCreatureMustBlock() {
        addCreatureReady(player1, new BrutalHordechief());
        addCreatureReady(player1, new ArashinCleric());
        addCreatureReady(player2, new ArashinCleric());
        activateBlockControl(ManaColor.RED, ManaColor.WHITE);
        harness.enterBattlefieldAndReturn(player2, new ArashinCleric());
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, this::resolveAllTriggers);

        declareAttackersToBlockers(player1, List.of(1));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Blocker choice persists into a second combat in the same turn")
    void blockerChoicePersistsForLaterCombat() {
        addCreatureReady(player1, new BrutalHordechief());
        Permanent attacker = addCreatureReady(player1, new ArashinCleric());
        addCreatureReady(player2, new ArashinCleric());
        activateBlockControl(ManaColor.RED, ManaColor.RED);
        declareAttackersToBlockers(player1, List.of(1));
        gd.additionalCombatPhasesOnly = 1;
        harness.withAutoStop(TurnStep.END_OF_COMBAT,
                () -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 1))));
        harness.passUntil(player1, TurnStep.END_OF_COMBAT);
        attacker.setTapped(false);
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);

        declareAttackersToBlockers(player1, List.of(1));

        PendingInteraction.BlockerDeclaration pending =
                gd.interaction.activeInteraction(PendingInteraction.BlockerDeclaration.class);
        assertThat(pending).isNotNull();
        assertThat(pending.decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(pending.choosingForOpponent()).isTrue();
    }

    @Test
    @DisplayName("A summoning-sick tapped Hordechief can activate its ability")
    void abilityDoesNotRequireTappingOrHaste() {
        Permanent hordechief = harness.addToBattlefieldAndReturn(player1, new BrutalHordechief());
        hordechief.setTapped(true);
        addCreatureReady(player1, new ArashinCleric());
        addCreatureReady(player2, new ArashinCleric());
        activateBlockControl(ManaColor.WHITE, ManaColor.WHITE);

        declareAttackersToBlockers(player1, List.of(1));

        PendingInteraction.BlockerDeclaration pending =
                gd.interaction.activeInteraction(PendingInteraction.BlockerDeclaration.class);
        assertThat(pending).isNotNull();
        assertThat(pending.decidingPlayerId()).isEqualTo(player1.getId());
    }

    private void activateBlockControl(ManaColor firstHybrid, ManaColor secondHybrid) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, firstHybrid, 1);
        harness.addMana(player1, secondHybrid, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);
    }

    private void declareAttackersToBlockers(Player attacker, List<Integer> indices) {
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(attacker, indices));
        harness.passUntil(attacker, TurnStep.DECLARE_BLOCKERS);
    }
}
