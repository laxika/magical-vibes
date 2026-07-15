package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BlindZealotTest extends BaseCardTest {

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        GameData gd = harness.getGameData();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    

    @Test
    @DisplayName("Combat damage trigger presents may ability choice")
    void combatDamageTriggerPresentsMayChoice() {
        Permanent zealot = addReadyCreature(player1, new BlindZealot());
        zealot.setAttacking(true);
        addReadyCreature(player2, new GrizzlyBears());

        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting may and choosing a creature sacrifices Blind Zealot and destroys the target")
    void sacrificeSelfAndDestroyTarget() {
        Permanent zealot = addReadyCreature(player1, new BlindZealot());
        zealot.setAttacking(true);
        Permanent bears = addReadyCreature(player2, new GrizzlyBears());

        resolveCombat();

        GameData gd = harness.getGameData();
        // Accept the may ability
        harness.handleMayAbilityChosen(player1, true);
        // Resolve the inner effect from the stack
        harness.passBothPriorities();
        // Choose the target creature
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        // Blind Zealot should be sacrificed (removed from battlefield, in graveyard)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Blind Zealot"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Blind Zealot"));

        // Target creature should be destroyed
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Declining the may ability means no sacrifice - nothing happens")
    void declineSacrifice() {
        Permanent zealot = addReadyCreature(player1, new BlindZealot());
        zealot.setAttacking(true);
        Permanent bears = addReadyCreature(player2, new GrizzlyBears());

        resolveCombat();

        GameData gd = harness.getGameData();
        harness.handleMayAbilityChosen(player1, false);

        // Blind Zealot should still be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Blind Zealot"));

        // Target creature should still be on the battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("declines"));
    }

    @Test
    @DisplayName("No trigger when defender has no creatures")
    void noTriggerWhenNoPermanents() {
        Permanent zealot = addReadyCreature(player1, new BlindZealot());
        zealot.setAttacking(true);
        // player2 has no creatures

        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("No trigger when Blind Zealot is blocked and deals no damage to player")
    void noTriggerWhenBlocked() {
        Permanent zealot = addReadyCreature(player1, new BlindZealot());
        zealot.setAttacking(true);
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
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
        Permanent zealot = addReadyCreature(player1, new BlindZealot());
        zealot.setAttacking(true);
        addReadyCreature(player2, new GrizzlyBears());

        resolveCombat();

        GameData gd = harness.getGameData();
        harness.handleMayAbilityChosen(player1, false);

        // Blind Zealot is 2/2, should deal 2 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Accepting may presents multi-permanent choice with defender's creatures only")
    void onlyDamagedPlayerCreatures() {
        Permanent zealot = addReadyCreature(player1, new BlindZealot());
        zealot.setAttacking(true);
        Permanent ownBears = addReadyCreature(player1, new GrizzlyBears());
        Permanent enemyBears = addReadyCreature(player2, new GrizzlyBears());

        resolveCombat();

        GameData gd = harness.getGameData();
        // Accept may ability
        harness.handleMayAbilityChosen(player1, true);
        // Resolve inner effect
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);

        // The valid IDs should only contain the enemy creature, not our own
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).validIds())
                .contains(enemyBears.getId())
                .doesNotContain(ownBears.getId());
    }

    @Test
    @DisplayName("Game advances after sacrifice choice is made")
    void gameAdvancesAfterChoice() {
        Permanent zealot = addReadyCreature(player1, new BlindZealot());
        zealot.setAttacking(true);
        Permanent bears = addReadyCreature(player2, new GrizzlyBears());

        resolveCombat();

        GameData gd = harness.getGameData();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }
}
