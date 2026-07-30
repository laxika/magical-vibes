package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GoblinArsonistTest extends BaseCardTest {

    /**
     * Sets up combat where Goblin Arsonist (player1) attacks and is blocked by a 3/3,
     * so the 1/1 Arsonist dies from combat damage.
     */
    private void setupCombatWhereArsonistDies() {
        Permanent arsonist = findPermanent(player1, "Goblin Arsonist");
        arsonist.setSummoningSick(false);
        arsonist.setAttacking(true);

        GrizzlyBears bigBear = new GrizzlyBears();
        bigBear.setPower(3);
        bigBear.setToughness(3);
        Permanent blocker = new Permanent(bigBear);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Death trigger deals 1 damage to the chosen player when accepted")
    void deathTriggerDealsDamageToPlayer() {
        harness.addToBattlefield(player1, new GoblinArsonist());
        harness.setLife(player2, 20);

        setupCombatWhereArsonistDies();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Goblin Arsonist");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Death trigger kills a 1/1 creature when the may choice is accepted")
    void deathTriggerKillsSmallCreature() {
        harness.addToBattlefield(player1, new GoblinArsonist());
        harness.addToBattlefield(player2, new SavannahLions());

        UUID lionsId = harness.getPermanentId(player2, "Savannah Lions");

        setupCombatWhereArsonistDies();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.handlePermanentChosen(player1, lionsId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(lionsId));
        harness.assertInGraveyard(player2, "Savannah Lions");
    }

    @Test
    @DisplayName("Declining the may choice deals no damage")
    void decliningDealsNoDamage() {
        harness.addToBattlefield(player1, new GoblinArsonist());
        harness.setLife(player2, 20);

        setupCombatWhereArsonistDies();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
