package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.f.Fleshtaker;
import com.github.laxika.magicalvibes.cards.s.SpireMonitor;
import com.github.laxika.magicalvibes.cards.n.NumbingDose;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ImpalerShrike.class, Fleshtaker.class, NumbingDose.class, SpireMonitor.class})
class ImpalerShrikeTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage trigger presents may ability choice")
    void combatDamageTriggerPresentsMayChoice() {
        Permanent shrike = addCreatureReady(player1, new ImpalerShrike());
        shrike.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting may sacrifices Impaler Shrike and draws 3 cards")
    void sacrificeSelfAndDrawCards() {
        Permanent shrike = addCreatureReady(player1, new ImpalerShrike());
        shrike.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        GameData gd = harness.getGameData();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        // Accept the may ability
        harness.handleMayAbilityChosen(player1, true);
        // Resolve the triggered ability from the stack
        harness.passBothPriorities();

        // Impaler Shrike should be sacrificed
        harness.assertNotOnBattlefield(player1, "Impaler Shrike");
        harness.assertInGraveyard(player1, "Impaler Shrike");

        // Controller should have drawn 3 cards
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 3);
    }

    @Test
    @DisplayName("Sacrificing Impaler Shrike fires ally-sacrifice triggers")
    void sacrificeFiresAllySacrificeTriggers() {
        Permanent shrike = addCreatureReady(player1, new ImpalerShrike());
        shrike.setAttacking(true);
        addCreatureReady(player1, new Fleshtaker());

        resolveCombat();
        resolveAllTriggers();

        GameData gd = harness.getGameData();
        int lifeBefore = gd.getLife(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Impaler Shrike");

        // Fleshtaker's "whenever you sacrifice another creature, you gain 1 life and scry 1".
        resolveAllTriggers();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Auras attached to Impaler Shrike go to the graveyard when it is sacrificed")
    void sacrificeCleansUpOrphanedAuras() {
        Permanent shrike = addCreatureReady(player1, new ImpalerShrike());
        shrike.setAttacking(true);

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new NumbingDose());
        aura.setAttachedTo(shrike.getId());

        resolveCombat();
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Impaler Shrike");
        harness.assertNotOnBattlefield(player1, "Numbing Dose");
        harness.assertInGraveyard(player1, "Numbing Dose");
    }

    @Test
    @DisplayName("Declining the may ability keeps Impaler Shrike alive and draws no cards")
    void declineSacrifice() {
        Permanent shrike = addCreatureReady(player1, new ImpalerShrike());
        shrike.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        GameData gd = harness.getGameData();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.handleMayAbilityChosen(player1, false);

        // Impaler Shrike should still be on the battlefield
        harness.assertOnBattlefield(player1, "Impaler Shrike");

        // No cards drawn
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("declines"));
    }

    @Test
    @DisplayName("\"If you do\" gate — Shrike killed in response means no sacrifice and no draw")
    void noDrawWhenShrikeLeavesBeforeResolution() {
        Permanent shrike = addCreatureReady(player1, new ImpalerShrike());
        shrike.setAttacking(true);

        resolveCombat();

        GameData gd = harness.getGameData();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        // The Shrike is removed while the trigger is still on the stack — it can no
        // longer be sacrificed, so the contingent draw must not happen either.
        gd.playerBattlefields.get(player1.getId()).remove(shrike);

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("fizzles") && log.contains("source no longer on the battlefield"));
    }

    @Test
    @DisplayName("A former controller cannot sacrifice Shrike after losing control")
    void noDrawWhenShrikeChangesControllerBeforeResolution() {
        Permanent shrike = addCreatureReady(player1, new ImpalerShrike());
        shrike.setAttacking(true);
        resolveCombat();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        gd.playerBattlefields.get(player1.getId()).remove(shrike);
        gd.playerBattlefields.get(player2.getId()).add(shrike);

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player2, "Impaler Shrike");
        harness.assertNotInGraveyard(player1, "Impaler Shrike");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("No trigger when Impaler Shrike is blocked and deals no damage to player")
    void noTriggerWhenBlocked() {
        Permanent shrike = addCreatureReady(player1, new ImpalerShrike());
        shrike.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new SpireMonitor());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Defender takes combat damage regardless of sacrifice choice")
    void defenderTakesCombatDamage() {
        harness.setLife(player2, 20);
        Permanent shrike = addCreatureReady(player1, new ImpalerShrike());
        shrike.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player1, false);

        // Impaler Shrike is 3/1, should deal 3 damage
        harness.assertLife(player2, 17);
    }
}
