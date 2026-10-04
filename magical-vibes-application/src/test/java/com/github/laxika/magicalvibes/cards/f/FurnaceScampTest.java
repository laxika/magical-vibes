package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.r.RottedHystrix;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FurnaceScamp.class, RottedHystrix.class})
class FurnaceScampTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage trigger presents may ability choice")
    void combatDamageTriggerPresentsMayChoice() {
        Permanent scamp = addCreatureReady(player1, new FurnaceScamp());
        scamp.setAttacking(true);

        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting may sacrifices Furnace Scamp and deals 3 damage to damaged player")
    void sacrificeSelfAndDealDamage() {
        harness.setLife(player2, 20);
        Permanent scamp = addCreatureReady(player1, new FurnaceScamp());
        scamp.setAttacking(true);

        resolveCombat();

        GameData gd = harness.getGameData();
        // Accept the may ability
        harness.handleMayAbilityChosen(player1, true);
        // Resolve the triggered ability from the stack
        harness.passBothPriorities();

        // Furnace Scamp should be sacrificed
        harness.assertNotOnBattlefield(player1, "Furnace Scamp");
        harness.assertInGraveyard(player1, "Furnace Scamp");

        // Player 2 takes 1 combat damage + 3 trigger damage = 4 total
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Declining the may ability keeps Furnace Scamp alive and deals no extra damage")
    void declineSacrifice() {
        harness.setLife(player2, 20);
        Permanent scamp = addCreatureReady(player1, new FurnaceScamp());
        scamp.setAttacking(true);

        resolveCombat();

        GameData gd = harness.getGameData();
        harness.handleMayAbilityChosen(player1, false);

        // Furnace Scamp should still be on the battlefield
        harness.assertOnBattlefield(player1, "Furnace Scamp");

        // Player 2 takes only 1 combat damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("declines"));
    }

    @Test
    @DisplayName("No trigger when Furnace Scamp is blocked and deals no damage to player")
    void noTriggerWhenBlocked() {
        Permanent scamp = addCreatureReady(player1, new FurnaceScamp());
        scamp.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new RottedHystrix());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }
    @Test
    @DisplayName("No extra damage when the source leaves before its trigger resolves")
    void noExtraDamageWhenSourceLeaves() {
        harness.setLife(player2, 20);
        Permanent scamp = addCreatureReady(player1, new FurnaceScamp());
        scamp.setAttacking(true);
        resolveCombat();
        harness.handleMayAbilityChosen(player1, true);

        gd.playerBattlefields.get(player1.getId()).remove(scamp);
        gd.playerGraveyards.get(player1.getId()).add(scamp.getCard());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Furnace Scamp");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Cannot sacrifice the source after losing control of it")
    void noSacrificeAfterLosingControl() {
        harness.setLife(player2, 20);
        Permanent scamp = addCreatureReady(player1, new FurnaceScamp());
        scamp.setAttacking(true);
        resolveCombat();
        harness.handleMayAbilityChosen(player1, true);

        gd.playerBattlefields.get(player1.getId()).remove(scamp);
        gd.playerBattlefields.get(player2.getId()).add(scamp);
        scamp.setAttacking(false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Furnace Scamp");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }
}
