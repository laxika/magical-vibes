package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfWood;
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

class VenomTest extends BaseCardTest {

    // ===== Enchanted creature blocks =====

    @Test
    @DisplayName("When enchanted creature blocks a non-Wall attacker, that attacker is scheduled for end-of-combat destruction")
    void enchantedCreatureBlocksNonWall_schedulesDestruction() {
        Permanent blocker = addReadyBears(player2);
        Permanent venom = addVenomAttachedTo(player2, blocker);

        Permanent attacker = addReadySpider(player1); // green, non-Wall
        attacker.setAttacking(true);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // The block trigger references the blocked attacker (the "other creature"), sourced from Venom
        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Venom")
                        && se.getTargetId().equals(attacker.getId())
                        && se.getSourcePermanentId().equals(venom.getId()));

        harness.passBothPriorities();
        assertThat(gd.getDelayedActions(DestroyPermanentAtEndOfCombat.class))
                .anyMatch(a -> a.permanentId().equals(attacker.getId()));
    }

    // ===== Enchanted creature becomes blocked =====

    @Test
    @DisplayName("When enchanted creature becomes blocked by a non-Wall creature, that blocker is scheduled for end-of-combat destruction")
    void enchantedCreatureBecomesBlockedByNonWall_schedulesDestruction() {
        Permanent attacker = addReadyBears(player1);
        Permanent venom = addVenomAttachedTo(player1, attacker);
        attacker.setAttacking(true);

        Permanent blocker = addReadySpider(player2); // green, non-Wall

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Venom")
                        && se.getTargetId().equals(blocker.getId())
                        && se.getSourcePermanentId().equals(venom.getId()));

        harness.passBothPriorities();
        assertThat(gd.getDelayedActions(DestroyPermanentAtEndOfCombat.class))
                .anyMatch(a -> a.permanentId().equals(blocker.getId()));
    }

    @Test
    @DisplayName("A non-Wall blocker survives combat damage but is destroyed at end of combat")
    void nonWallBlockerDestroyedAtEndOfCombat() {
        Permanent attacker = addReadyBears(player1);
        addVenomAttachedTo(player1, attacker);
        attacker.setAttacking(true);

        addReadySpider(player2); // 2/4 survives Grizzly Bears' 2 combat damage

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // Resolve the trigger, then advance through end of combat
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Giant Spider"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Giant Spider"));
    }

    // ===== Wall opponent is exempt =====

    @Test
    @DisplayName("When enchanted creature becomes blocked by a Wall, nothing is scheduled for destruction")
    void becomesBlockedByWall_schedulesNothing() {
        Permanent attacker = addReadyBears(player1);
        addVenomAttachedTo(player1, attacker);
        attacker.setAttacking(true);

        addReadyWall(player2); // Wall — exempt from Venom's filter

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // The trigger fires but the non-Wall filter fails at resolution
        harness.passBothPriorities();
        assertThat(gd.hasDelayedAction(DestroyPermanentAtEndOfCombat.class)).isFalse();
    }

    // ===== No trigger when not attached =====

    @Test
    @DisplayName("No trigger when Venom is not attached to any creature")
    void noTriggerWhenNotAttached() {
        addReadyBears(player2); // blocker without Venom
        addVenom(player2); // on battlefield but not attached

        Permanent attacker = addReadySpider(player1);
        attacker.setAttacking(true);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        long venomTriggers = gd.stack.stream()
                .filter(se -> se.getCard().getName().equals("Venom"))
                .count();
        assertThat(venomTriggers).isZero();
    }

    // ===== Helpers =====

    private Permanent addVenom(Player player) {
        Permanent perm = new Permanent(new Venom());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addVenomAttachedTo(Player player, Permanent creature) {
        Permanent perm = addVenom(player);
        perm.setAttachedTo(creature.getId());
        return perm;
    }

    private Permanent addReadyBears(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
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

    private Permanent addReadyWall(Player player) {
        Permanent perm = new Permanent(new WallOfWood());
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
