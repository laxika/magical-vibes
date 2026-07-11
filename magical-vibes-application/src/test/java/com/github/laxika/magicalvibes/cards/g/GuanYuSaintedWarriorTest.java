package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GuanYuSaintedWarriorTest extends BaseCardTest {

    // ===== Death trigger =====

    @Test
    @DisplayName("Accepting the death trigger shuffles Guan Yu from the graveyard into its owner's library")
    void diesThenAcceptShufflesIntoLibrary() {
        harness.setLibrary(player1, new ArrayList<>());
        Permanent guanYu = harness.addToBattlefieldAndReturn(player1, new GuanYuSaintedWarrior());
        guanYu.setMarkedDamage(5);

        harness.runStateBasedActions();

        // It first enters the graveyard, then its death trigger waits on the stack.
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Guan Yu, Sainted Warrior"));

        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotInGraveyard(player1, "Guan Yu, Sainted Warrior");
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Guan Yu, Sainted Warrior"));
    }

    @Test
    @DisplayName("Declining the death trigger leaves Guan Yu in the graveyard")
    void diesThenDeclineLeavesInGraveyard() {
        harness.setLibrary(player1, new ArrayList<>());
        Permanent guanYu = harness.addToBattlefieldAndReturn(player1, new GuanYuSaintedWarrior());
        guanYu.setMarkedDamage(5);

        harness.runStateBasedActions();

        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Guan Yu, Sainted Warrior"));
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Guan Yu, Sainted Warrior"));
    }

    // ===== Horsemanship =====

    @Test
    @DisplayName("Guan Yu can't be blocked by a creature without horsemanship")
    void cannotBeBlockedByCreatureWithoutHorsemanship() {
        Permanent blockerPerm = new Permanent(new GrizzlyBears());
        blockerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        Permanent atkPerm = new Permanent(new GuanYuSaintedWarrior());
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("horsemanship");
    }

    @Test
    @DisplayName("Guan Yu can be blocked by a creature with horsemanship")
    void canBeBlockedByCreatureWithHorsemanship() {
        Permanent blockerPerm = new Permanent(new GuanYuSaintedWarrior());
        blockerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        Permanent atkPerm = new Permanent(new GuanYuSaintedWarrior());
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }
}
