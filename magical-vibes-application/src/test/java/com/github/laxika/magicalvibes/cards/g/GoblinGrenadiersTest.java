package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GoblinGrenadiersTest extends BaseCardTest {

    private Permanent addAttacker() {
        Permanent attacker = new Permanent(new GoblinGrenadiers());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);
        return attacker;
    }

    private Permanent addDefenderCreature() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);
        return bears;
    }

    private void declareNoBlocks() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of());
    }

    @Test
    @DisplayName("Accepting the may sacrifices the Goblin and destroys the chosen creature and land")
    void acceptDestroysBothTargets() {
        Permanent bears = addDefenderCreature();
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        addAttacker();

        declareNoBlocks();

        // Both targets are chosen as the trigger goes on the stack (CR 603.3d).
        harness.handlePermanentChosen(player1, bears.getId());
        harness.handlePermanentChosen(player1, mountain.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Goblin Grenadiers");
        harness.assertInGraveyard(player1, "Goblin Grenadiers");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertInGraveyard(player2, "Mountain");
    }

    @Test
    @DisplayName("Declining the may leaves the Goblin and both targets alone")
    void declineKeepsEverything() {
        Permanent bears = addDefenderCreature();
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        addAttacker();

        declareNoBlocks();

        harness.handlePermanentChosen(player1, bears.getId());
        harness.handlePermanentChosen(player1, mountain.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Goblin Grenadiers");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Mountain");
    }

    @Test
    @DisplayName("A blocked attacker never triggers the ability")
    void blockedNoTrigger() {
        addDefenderCreature();
        harness.addToBattlefield(player2, new Mountain());
        addAttacker();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Goblin Grenadiers");
        harness.assertOnBattlefield(player2, "Mountain");
    }
}
