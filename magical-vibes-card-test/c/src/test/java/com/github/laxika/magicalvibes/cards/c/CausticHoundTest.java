package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.f.FangrenMarauder;
import com.github.laxika.magicalvibes.cards.g.GoForTheThroat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CausticHound.class, FangrenMarauder.class, GoForTheThroat.class})
class CausticHoundTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Caustic Hound puts it on the battlefield")
    void castingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new CausticHound()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Caustic Hound");
    }

    @Test
    @DisplayName("When Caustic Hound dies in combat, death trigger goes on the stack")
    void deathTriggerGoesOnStack() {
        harness.addToBattlefield(player1, new CausticHound());
        harness.setLife(player2, 20);

        setupCombatWhereCausticHoundDies();
        resolveCombat(); // Combat damage — Caustic Hound dies

        // Caustic Hound should be in graveyard
        harness.assertInGraveyard(player1, "Caustic Hound");

        // Death trigger should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Caustic Hound");
    }

    @Test
    @DisplayName("Resolving death trigger causes each player to lose 4 life")
    void deathTriggerCausesEachPlayerLifeLoss() {
        harness.addToBattlefield(player1, new CausticHound());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        setupCombatWhereCausticHoundDies();
        resolveCombat(); // Combat damage — Caustic Hound dies

        // Resolve the death trigger
        harness.passBothPriorities();

        // Both players lose 4 life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Controller also loses life from Caustic Hound's death trigger")
    void controllerAlsoLosesLife() {
        harness.addToBattlefield(player1, new CausticHound());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        setupCombatWhereCausticHoundDies();
        resolveCombat(); // Combat damage — Caustic Hound dies

        // Resolve the death trigger
        harness.passBothPriorities();

        // Controller loses 4 life: 20 - 4 = 16
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Death trigger life loss is logged")
    void deathTriggerLifeLossIsLogged() {
        harness.addToBattlefield(player1, new CausticHound());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        setupCombatWhereCausticHoundDies();
        resolveCombat(); // Combat damage — Caustic Hound dies

        // Resolve the death trigger
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("loses") && log.contains("4") && log.contains("life"));
    }

    @Test
    @DisplayName("Destroying an opponent's Caustic Hound triggers life loss for both players")
    void destructionTriggersLifeLossForBothPlayers() {
        Permanent hound = harness.addToBattlefieldAndReturn(player2, new CausticHound());
        harness.setHand(player1, List.of(new GoForTheThroat()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, hound.getId());

        harness.assertInGraveyard(player2, "Caustic Hound");
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Two Caustic Hounds dying in combat each trigger independently")
    void simultaneousDeathsEachTrigger() {
        Permanent attacker = addCreatureReady(player1, new CausticHound());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new CausticHound());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveCombat();

        harness.assertInGraveyard(player1, "Caustic Hound");
        harness.assertInGraveyard(player2, "Caustic Hound");
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 12);
    }

    /**
     * Sets up combat where Caustic Hound (player1, 4/4) attacks and is blocked by a 5/5 creature (player2).
     * Caustic Hound will die from combat damage.
     */
    private void setupCombatWhereCausticHoundDies() {
        Permanent causticHoundPerm = findPermanent(player1, "Caustic Hound");
        causticHoundPerm.setSummoningSick(false);
        causticHoundPerm.setAttacking(true);

        Permanent blockerPerm = addCreatureReady(player2, new FangrenMarauder());
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);
    }
}
