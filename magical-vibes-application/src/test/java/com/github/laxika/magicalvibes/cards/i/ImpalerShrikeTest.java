package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.cards.f.Fleshtaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ImpalerShrikeTest extends BaseCardTest {

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        GameData gd = harness.getGameData();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    

    @Test
    @DisplayName("Combat damage trigger presents may ability choice")
    void combatDamageTriggerPresentsMayChoice() {
        Permanent shrike = addReadyCreature(player1, new ImpalerShrike());
        shrike.setAttacking(true);

        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting may sacrifices Impaler Shrike and draws 3 cards")
    void sacrificeSelfAndDrawCards() {
        Permanent shrike = addReadyCreature(player1, new ImpalerShrike());
        shrike.setAttacking(true);

        resolveCombat();

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
        Permanent shrike = addReadyCreature(player1, new ImpalerShrike());
        shrike.setAttacking(true);
        addReadyCreature(player1, new Fleshtaker());

        resolveCombat();

        GameData gd = harness.getGameData();
        int lifeBefore = gd.getLife(player1.getId());

        // Resolve the combat-damage trigger so the "you may sacrifice it" choice opens.
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Impaler Shrike");

        // Fleshtaker's "whenever you sacrifice another creature, you gain 1 life and scry 1".
        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Auras attached to Impaler Shrike go to the graveyard when it is sacrificed")
    void sacrificeCleansUpOrphanedAuras() {
        Permanent shrike = addReadyCreature(player1, new ImpalerShrike());
        shrike.setAttacking(true);

        GameData gd = harness.getGameData();
        Permanent aura = new Permanent(new HolyStrength());
        aura.setAttachedTo(shrike.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        resolveCombat();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Impaler Shrike");
        harness.assertNotOnBattlefield(player1, "Holy Strength");
        harness.assertInGraveyard(player1, "Holy Strength");
    }

    @Test
    @DisplayName("Declining the may ability keeps Impaler Shrike alive and draws no cards")
    void declineSacrifice() {
        Permanent shrike = addReadyCreature(player1, new ImpalerShrike());
        shrike.setAttacking(true);

        resolveCombat();

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
    @DisplayName("No trigger when Impaler Shrike is blocked and deals no damage to player")
    void noTriggerWhenBlocked() {
        Permanent shrike = addReadyCreature(player1, new ImpalerShrike());
        shrike.setAttacking(true);
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Defender takes combat damage regardless of sacrifice choice")
    void defenderTakesCombatDamage() {
        harness.setLife(player2, 20);
        Permanent shrike = addReadyCreature(player1, new ImpalerShrike());
        shrike.setAttacking(true);

        resolveCombat();

        GameData gd = harness.getGameData();
        harness.handleMayAbilityChosen(player1, false);

        // Impaler Shrike is 3/1, should deal 3 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }
}
