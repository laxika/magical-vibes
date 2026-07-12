package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DestroyPermanentAtEndOfCombat;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CinderWallTest extends BaseCardTest {

    @Test
    @DisplayName("When Cinder Wall blocks, it schedules itself for end-of-combat destruction")
    void blockingSchedulesSelfDestruction() {
        Permanent attacker = addReadySpider(player1); // green, 2/4
        attacker.setAttacking(true);
        Permanent cinderWall = addReadyCinderWall(player2);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // A block trigger from Cinder Wall fires (non-targeting, references itself)
        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Cinder Wall"));

        // Resolving it schedules Cinder Wall itself for destruction at end of combat
        harness.passBothPriorities();
        assertThat(gd.getDelayedActions(DestroyPermanentAtEndOfCombat.class))
                .anyMatch(a -> a.permanentId().equals(cinderWall.getId()));
    }

    @Test
    @DisplayName("Cinder Wall survives combat damage but is destroyed at end of combat")
    void survivesCombatDamageButDestroyedAtEndOfCombat() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent attacker = addReadySpider(player1); // 2/4: deals 2, Cinder Wall (3 toughness) survives damage
        attacker.setAttacking(true);
        addReadyCinderWall(player2);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // Resolve the trigger, then advance through end of combat
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Cinder Wall"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Cinder Wall"));
    }

    @Test
    @DisplayName("Cinder Wall that never blocks is not scheduled for destruction")
    void notDestroyedWhenItDoesNotBlock() {
        Permanent attacker = addReadySpider(player1);
        attacker.setAttacking(true);
        addReadyCinderWall(player2);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of()); // Cinder Wall stays back

        harness.passBothPriorities();
        assertThat(gd.hasDelayedAction(DestroyPermanentAtEndOfCombat.class)).isFalse();
    }

    // ===== Helpers =====

    private Permanent addReadyCinderWall(Player player) {
        Permanent perm = new Permanent(new CinderWall());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadySpider(Player player) {
        Permanent perm = new Permanent(new GiantSpider());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void setupDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
