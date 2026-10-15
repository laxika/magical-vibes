package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.Lifelink;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinArsonist.class, GorehornMinotaurs.class, GoblinFireslinger.class, GideonJura.class,
        Lifelink.class})
class GoblinArsonistTest extends BaseCardTest {

    /**
     * Sets up combat where Goblin Arsonist (player1) attacks and is blocked by a 3/3,
     * so the 1/1 Arsonist dies from combat damage.
     */
    private void setupCombatWhereArsonistDies() {
        Permanent arsonist = findPermanent(player1, "Goblin Arsonist");
        arsonist.setSummoningSick(false);
        arsonist.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GorehornMinotaurs());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

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

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Death trigger kills a 1/1 creature when the may choice is accepted")
    void deathTriggerKillsSmallCreature() {
        harness.addToBattlefield(player1, new GoblinArsonist());
        harness.addToBattlefield(player2, new GoblinFireslinger());

        UUID fireslingerId = harness.getPermanentId(player2, "Goblin Fireslinger");

        setupCombatWhereArsonistDies();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, fireslingerId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Goblin Fireslinger");
        harness.assertInGraveyard(player2, "Goblin Fireslinger");
    }

    @Test
    @DisplayName("Declining the may choice deals no damage")
    void decliningDealsNoDamage() {
        harness.addToBattlefield(player1, new GoblinArsonist());
        harness.setLife(player2, 20);

        setupCombatWhereArsonistDies();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Death trigger can damage its controller")
    void deathTriggerCanDamageController() {
        harness.addToBattlefield(player1, new GoblinArsonist());
        harness.setLife(player1, 20);
        setupCombatWhereArsonistDies();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Death trigger removes one loyalty from a targeted planeswalker")
    void deathTriggerDamagesPlaneswalker() {
        harness.addToBattlefield(player1, new GoblinArsonist());
        Permanent gideon = harness.addToBattlefieldAndReturn(player2, new GideonJura());
        gideon.setCounterCount(CounterType.LOYALTY, 6);
        setupCombatWhereArsonistDies();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, gideon.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        harness.assertOnBattlefield(player2, "Gideon Jura");
    }

    @Test
    @DisplayName("Death-trigger damage retains lifelink from before Arsonist died")
    void deathTriggerRetainsLifelink() {
        Permanent arsonist = harness.addToBattlefieldAndReturn(player1, new GoblinArsonist());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Lifelink());
        aura.setAttachedTo(arsonist.getId());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        setupCombatWhereArsonistDies();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Goblin Arsonist");
        harness.assertLife(player1, 21);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 22);
    }
}
