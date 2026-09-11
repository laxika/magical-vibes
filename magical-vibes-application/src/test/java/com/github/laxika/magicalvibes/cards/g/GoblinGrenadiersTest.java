package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BenalishInfantry;
import com.github.laxika.magicalvibes.cards.d.DryadArbor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinGrenadiers.class, BenalishInfantry.class, GemstoneMine.class, DryadArbor.class})
class GoblinGrenadiersTest extends BaseCardTest {

    private Permanent addAttacker() {
        Permanent attacker = addCreatureReady(player1, new GoblinGrenadiers());
        attacker.setAttacking(true);
        return attacker;
    }

    private Permanent addDefenderCreature() {
        return addCreatureReady(player2, new BenalishInfantry());
    }

    private void declareNoBlocks() {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
    }

    @Test
    @DisplayName("Accepting the may sacrifices the Goblin and destroys the chosen creature and land")
    void acceptDestroysBothTargets() {
        Permanent bears = addDefenderCreature();
        Permanent gemstoneMine = harness.addToBattlefieldAndReturn(player2, new GemstoneMine());
        addAttacker();

        declareNoBlocks();

        // Both targets are chosen as the trigger goes on the stack (CR 603.3d).
        harness.handlePermanentChosen(player1, bears.getId());
        harness.handlePermanentChosen(player1, gemstoneMine.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Goblin Grenadiers");
        harness.assertInGraveyard(player1, "Goblin Grenadiers");
        harness.assertNotOnBattlefield(player2, "Benalish Infantry");
        harness.assertInGraveyard(player2, "Benalish Infantry");
        harness.assertNotOnBattlefield(player2, "Gemstone Mine");
        harness.assertInGraveyard(player2, "Gemstone Mine");
    }

    @Test
    @DisplayName("Declining the may leaves the Goblin and both targets alone")
    void declineKeepsEverything() {
        Permanent bears = addDefenderCreature();
        Permanent gemstoneMine = harness.addToBattlefieldAndReturn(player2, new GemstoneMine());
        addAttacker();

        declareNoBlocks();

        harness.handlePermanentChosen(player1, bears.getId());
        harness.handlePermanentChosen(player1, gemstoneMine.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Goblin Grenadiers");
        harness.assertOnBattlefield(player2, "Benalish Infantry");
        harness.assertOnBattlefield(player2, "Gemstone Mine");
    }

    @Test
    @DisplayName("A land creature may be chosen as both targets")
    void landCreatureCanBeBothTargets() {
        Permanent dryadArbor = addCreatureReady(player2, new DryadArbor());
        addAttacker();

        declareNoBlocks();

        harness.handlePermanentChosen(player1, dryadArbor.getId());
        harness.handlePermanentChosen(player1, dryadArbor.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Goblin Grenadiers");
        harness.assertInGraveyard(player1, "Goblin Grenadiers");
        harness.assertNotOnBattlefield(player2, "Dryad Arbor");
        harness.assertInGraveyard(player2, "Dryad Arbor");
    }

    @Test
    @DisplayName("A blocked attacker never triggers the ability")
    void blockedNoTrigger() {
        addDefenderCreature();
        harness.addToBattlefield(player2, new GemstoneMine());
        addAttacker();

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Goblin Grenadiers");
        harness.assertOnBattlefield(player2, "Gemstone Mine");
    }
}
