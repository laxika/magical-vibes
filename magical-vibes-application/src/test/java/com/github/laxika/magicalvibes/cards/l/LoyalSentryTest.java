package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TrollAscetic;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LoyalSentry.class, GrizzlyBears.class, TrollAscetic.class})
class LoyalSentryTest extends BaseCardTest {

    private Permanent addSentryBlocker() {
        return addCreatureReady(player2, new LoyalSentry());
    }

    private Permanent addAttacker(int power, int toughness) {
        GrizzlyBears card = new GrizzlyBears();
        card.setPower(power);
        card.setToughness(toughness);
        Permanent attacker = addCreatureReady(player1, card);
        attacker.setAttacking(true);
        return attacker;
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }

    @Test
    @DisplayName("Casting Loyal Sentry puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new LoyalSentry()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard()).isInstanceOf(LoyalSentry.class);
    }

    @Test
    @DisplayName("Resolving puts Loyal Sentry onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new LoyalSentry()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Loyal Sentry");
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new LoyalSentry()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Block trigger pushes onto stack =====

    @Test
    @DisplayName("Declaring Loyal Sentry as blocker pushes a triggered ability onto the stack")
    void blockTriggerPushesOntoStack() {
        Permanent sentryPerm = addSentryBlocker();
        Permanent atkPerm = addAttacker(2, 2);

        declareBlock(sentryPerm, atkPerm);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(atkPerm.getId());
        assertThat(entry.getSourcePermanentId()).isEqualTo(sentryPerm.getId());
        assertThat(entry.isNonTargeting()).isTrue();
    }

    // ===== Block trigger resolves — both creatures destroyed =====

    @Test
    @DisplayName("When block trigger resolves, both Loyal Sentry and blocked creature are destroyed")
    void blockTriggerDestroysBothCreatures() {
        declareBlock(addSentryBlocker(), addAttacker(2, 2));

        // Trigger is on the stack — resolve it
        harness.passBothPriorities();

        // Both creatures should be in their graveyards
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Loyal Sentry");

        // Neither should be on the battlefield
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Loyal Sentry");
    }

    @Test
    @DisplayName("Loyal Sentry destroys a large creature it blocks")
    void destroysLargeCreature() {
        declareBlock(addSentryBlocker(), addAttacker(10, 10));
        harness.passBothPriorities();

        // Even the 10/10 is destroyed by the trigger
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Loyal Sentry");
    }

    // ===== No damage to player when attacker is destroyed =====

    @Test
    @DisplayName("Blocked attacker destroyed by Loyal Sentry deals no damage to defending player")
    void destroyedAttackerDealsNoDamageToPlayer() {
        harness.setLife(player2, 20);

        declareBlock(addSentryBlocker(), addAttacker(5, 5));
        // Resolve the block trigger
        harness.passBothPriorities();

        // Player2 should take no damage — attacker was destroyed before combat damage
        harness.assertLife(player2, 20);
    }

    // ===== Trigger does not destroy if creatures are already gone =====

    @Test
    @DisplayName("Trigger does nothing if attacker is removed before resolution")
    void triggerDoesNothingIfAttackerAlreadyGone() {
        Permanent sentryPerm = addSentryBlocker();
        Permanent atkPerm = addAttacker(2, 2);

        declareBlock(sentryPerm, atkPerm);

        // Remove the attacker before trigger resolves
        gd.playerBattlefields.get(player1.getId()).remove(atkPerm);

        harness.passBothPriorities();

        // Loyal Sentry is still destroyed (self-destruct part still applies)
        harness.assertNotOnBattlefield(player2, "Loyal Sentry");
        harness.assertInGraveyard(player2, "Loyal Sentry");
    }

    @Test
    @DisplayName("Trigger does nothing to self if Loyal Sentry is removed before resolution")
    void triggerDoesNothingIfSentryAlreadyGone() {
        Permanent sentryPerm = addSentryBlocker();
        Permanent atkPerm = addAttacker(2, 2);

        declareBlock(sentryPerm, atkPerm);

        // Remove Loyal Sentry before trigger resolves
        gd.playerBattlefields.get(player2.getId()).remove(sentryPerm);

        harness.passBothPriorities();

        // Attacker is still destroyed by the trigger
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    // ===== Game log =====

    @Test
    @DisplayName("Block trigger generates appropriate game log entries")
    void blockTriggerGeneratesLogEntries() {
        declareBlock(addSentryBlocker(), addAttacker(2, 2));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("Loyal Sentry") && log.contains("block") && log.contains("trigger"));

        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("Grizzly Bears") && log.contains("destroyed"));
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("Loyal Sentry") && log.contains("destroyed"));
    }

    // ===== Normal creatures don't trigger on block =====

    @Test
    @DisplayName("Normal creature blocking does not push any trigger onto the stack")
    void normalCreatureDoesNotTriggerOnBlock() {
        Permanent blockerPerm = addCreatureReady(player2, new GrizzlyBears());
        Permanent atkPerm = addAttacker(2, 2);

        declareBlock(blockerPerm, atkPerm);

        assertThat(gd.stack).isEmpty();
    }

    // ===== Enters with summoning sickness =====

    @Test
    @DisplayName("Loyal Sentry enters battlefield with summoning sickness")
    void entersBattlefieldWithSummoningSickness() {
        harness.setHand(player1, List.of(new LoyalSentry()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent perm = findPermanent(player1, "Loyal Sentry");
        assertThat(perm.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("The block trigger destroys a hexproof attacker without targeting it")
    void destroysHexproofAttacker() {
        Permanent attacker = addCreatureReady(player1, new TrollAscetic());
        attacker.setAttacking(true);

        declareBlock(addSentryBlocker(), attacker);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Troll Ascetic");
        harness.assertInGraveyard(player2, "Loyal Sentry");
        harness.assertNotOnBattlefield(player1, "Troll Ascetic");
        harness.assertNotOnBattlefield(player2, "Loyal Sentry");
    }

    @Test
    @DisplayName("An attacker can regenerate while Loyal Sentry is still destroyed")
    void regeneratingAttackerSurvivesAndDealsNoCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new TrollAscetic());
        attacker.setAttacking(true);
        harness.setLife(player2, 20);

        declareBlock(addSentryBlocker(), attacker);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Troll Ascetic");
        harness.assertNotInGraveyard(player1, "Troll Ascetic");
        assertThat(attacker.isTapped()).isTrue();
        assertThat(attacker.isAttacking()).isFalse();
        harness.assertInGraveyard(player2, "Loyal Sentry");
        harness.assertNotOnBattlefield(player2, "Loyal Sentry");

        resolveCombat();
        harness.assertLife(player2, 20);
    }

}

