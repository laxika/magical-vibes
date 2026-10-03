package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.p.PhyrexianHulk;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlindZealot.class, PhyrexianHulk.class})
class BlindZealotTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage trigger presents may ability choice")
    void combatDamageTriggerPresentsMayChoice() {
        Permanent zealot = addCreatureReady(player1, new BlindZealot());
        zealot.setAttacking(true);
        Permanent victim = addCreatureReady(player2, new PhyrexianHulk());

        resolveCombat();
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting sacrifice destroys the previously chosen target")
    void sacrificeSelfAndDestroyTarget() {
        Permanent zealot = addCreatureReady(player1, new BlindZealot());
        zealot.setAttacking(true);
        Permanent bears = addCreatureReady(player2, new PhyrexianHulk());

        resolveCombat();

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        // Accept the may ability
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        // Blind Zealot should be sacrificed (removed from battlefield, in graveyard)
        harness.assertNotOnBattlefield(player1, "Blind Zealot");
        harness.assertInGraveyard(player1, "Blind Zealot");

        // Target creature should be destroyed
        harness.assertNotOnBattlefield(player2, "Phyrexian Hulk");
        harness.assertInGraveyard(player2, "Phyrexian Hulk");
    }

    @Test
    @DisplayName("Declining the may ability means no sacrifice - nothing happens")
    void declineSacrifice() {
        Permanent zealot = addCreatureReady(player1, new BlindZealot());
        zealot.setAttacking(true);
        Permanent bears = addCreatureReady(player2, new PhyrexianHulk());

        resolveCombat();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.handleMayAbilityChosen(player1, false);

        // Blind Zealot should still be on the battlefield
        harness.assertOnBattlefield(player1, "Blind Zealot");

        // Target creature should still be on the battlefield
        harness.assertOnBattlefield(player2, "Phyrexian Hulk");

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("declines"));
    }

    @Test
    @DisplayName("No ability remains on the stack when defender has no legal creature targets")
    void noTriggerWhenNoPermanents() {
        Permanent zealot = addCreatureReady(player1, new BlindZealot());
        zealot.setAttacking(true);
        // player2 has no creatures

        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("No trigger when Blind Zealot is blocked and deals no damage to player")
    void noTriggerWhenBlocked() {
        Permanent zealot = addCreatureReady(player1, new BlindZealot());
        zealot.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new PhyrexianHulk());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Defender takes combat damage even if sacrifice is declined")
    void defenderTakesCombatDamage() {
        harness.setLife(player2, 20);
        Permanent zealot = addCreatureReady(player1, new BlindZealot());
        zealot.setAttacking(true);
        Permanent victim = addCreatureReady(player2, new PhyrexianHulk());

        resolveCombat();
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        // Blind Zealot is 2/2, should deal 2 damage
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The target is chosen before resolution from the damaged player's creatures")
    void onlyDamagedPlayerCreatures() {
        Permanent zealot = addCreatureReady(player1, new BlindZealot());
        zealot.setAttacking(true);
        Permanent ownHulk = addCreatureReady(player1, new PhyrexianHulk());
        Permanent enemyBears = addCreatureReady(player2, new PhyrexianHulk());

        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .contains(enemyBears.getId())
                .doesNotContain(ownHulk.getId(), zealot.getId());
        harness.handlePermanentChosen(player1, enemyBears.getId());
        harness.assertOnBattlefield(player1, "Blind Zealot");
        harness.assertOnBattlefield(player1, "Phyrexian Hulk");
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Game advances after sacrifice choice is made")
    void gameAdvancesAfterChoice() {
        Permanent zealot = addCreatureReady(player1, new BlindZealot());
        zealot.setAttacking(true);
        Permanent bears = addCreatureReady(player2, new PhyrexianHulk());

        resolveCombat();

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        GameData gd = harness.getGameData();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }
    @Test
    @DisplayName("The target can regenerate while Blind Zealot is still sacrificed")
    void targetCanRegenerate() {
        Permanent zealot = addCreatureReady(player1, new BlindZealot());
        zealot.setAttacking(true);
        Permanent victim = addCreatureReady(player2, new PhyrexianHulk());
        victim.setRegenerationShield(1);

        resolveCombat();
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Blind Zealot");
        harness.assertOnBattlefield(player2, "Phyrexian Hulk");
        assertThat(victim.isTapped()).isTrue();
        assertThat(victim.getRegenerationShield()).isZero();
    }
}
