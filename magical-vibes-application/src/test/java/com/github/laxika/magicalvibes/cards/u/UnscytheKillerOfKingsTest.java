package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UnscytheKillerOfKingsTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the trigger exiles the dying creature and creates a 2/2 black Zombie")
    void acceptExilesDyingCreatureAndCreatesZombie() {
        Permanent blocker = setUpEquippedKill(player1, player2);

        runCombatUntilMayPrompt();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        // The dying creature's card is exiled (no longer in its owner's graveyard).
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(blocker.getCard().getId()));
        assertThat(gd.exiledCards)
                .anyMatch(e -> e.card().getId().equals(blocker.getCard().getId()));
        // A 2/2 black Zombie token is created under Unscythe's controller.
        assertThat(zombieTokens(player1)).hasSize(1);
        assertThat(zombieTokens(player1).getFirst().getCard().getPower()).isEqualTo(2);
        assertThat(zombieTokens(player1).getFirst().getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining the trigger leaves the creature in the graveyard and makes no token")
    void declineLeavesCreatureAndMakesNoZombie() {
        Permanent blocker = setUpEquippedKill(player1, player2);

        runCombatUntilMayPrompt();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(blocker.getCard().getId()));
        assertThat(gd.exiledCards)
                .noneMatch(e -> e.card().getId().equals(blocker.getCard().getId()));
        assertThat(zombieTokens(player1)).isEmpty();
    }

    @Test
    @DisplayName("No trigger when the killing creature has no Unscythe attached")
    void noTriggerWhenEquipmentNotAttached() {
        // A creature that survives combat (toughness 5) but kills the 2/2 blocker, with Unscythe on
        // the battlefield unattached — the killer carries no such ability, so nothing triggers.
        GrizzlyBears killerCard = new GrizzlyBears();
        killerCard.setToughness(5);
        Permanent killer = new Permanent(killerCard);
        killer.setSummoningSick(false);
        killer.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(killer);

        Permanent unscythe = new Permanent(new UnscytheKillerOfKings());
        gd.playerBattlefields.get(player1.getId()).add(unscythe); // not attached

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        for (int i = 0; i < 4; i++) {
            harness.passBothPriorities();
        }

        // The killer had no such ability, so no "you may exile" trigger was ever offered.
        assertThat(gd.pendingMayAbilities)
                .noneMatch(a -> a.sourceCard().getName().equals("Unscythe, Killer of Kings"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(blocker.getCard().getId()));
        assertThat(zombieTokens(player1)).isEmpty();
    }

    // ===== Helpers =====

    /**
     * Puts a Grizzly Bears equipped with Unscythe (a 5/5) on {@code attacker}'s battlefield attacking,
     * and a 2/2 Grizzly Bears on {@code defender}'s battlefield blocking it. Returns the blocker.
     */
    private Permanent setUpEquippedKill(Player attacker, Player defender) {
        Permanent equipped = new Permanent(new GrizzlyBears());
        equipped.setSummoningSick(false);
        equipped.setAttacking(true);
        gd.playerBattlefields.get(attacker.getId()).add(equipped);

        Permanent unscythe = new Permanent(new UnscytheKillerOfKings());
        unscythe.setAttachedTo(equipped.getId());
        gd.playerBattlefields.get(attacker.getId()).add(unscythe);

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(defender.getId()).add(blocker);

        harness.forceActivePlayer(attacker);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        return blocker;
    }

    private void runCombatUntilMayPrompt() {
        for (int i = 0; i < 10 && !gd.interaction.isAwaitingInput(); i++) {
            harness.passBothPriorities();
        }
    }

    private java.util.List<Permanent> zombieTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Zombie"))
                .toList();
    }
}
