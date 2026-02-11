package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DestroyBlockedCreatureAndSelfEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoyalSentryTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameService gs;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gs = harness.getGameService();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Loyal Sentry has correct card properties")
    void hasCorrectProperties() {
        LoyalSentry card = new LoyalSentry();

        assertThat(card.getName()).isEqualTo("Loyal Sentry");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{W}");
        assertThat(card.getColor()).isEqualTo(CardColor.WHITE);
        assertThat(card.getPower()).isEqualTo(1);
        assertThat(card.getToughness()).isEqualTo(1);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.HUMAN, CardSubtype.SOLDIER);
        assertThat(card.getOnBlockEffects()).hasSize(1);
        assertThat(card.getOnBlockEffects().getFirst()).isInstanceOf(DestroyBlockedCreatureAndSelfEffect.class);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Loyal Sentry puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new LoyalSentry()));
        harness.addMana(player1, "W", 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Loyal Sentry");
    }

    @Test
    @DisplayName("Resolving puts Loyal Sentry onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new LoyalSentry()));
        harness.addMana(player1, "W", 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Loyal Sentry"));
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
        // Player2 has Loyal Sentry as blocker
        Permanent sentryPerm = new Permanent(new LoyalSentry());
        sentryPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(sentryPerm);

        // Player1 has an attacking Grizzly Bears
        Permanent atkPerm = new Permanent(new GrizzlyBears());
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.awaitingBlockerDeclaration = true;

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Loyal Sentry");
        assertThat(entry.getTargetPermanentId()).isEqualTo(atkPerm.getId());
        assertThat(entry.getSourcePermanentId()).isEqualTo(sentryPerm.getId());
    }

    // ===== Block trigger resolves — both creatures destroyed =====

    @Test
    @DisplayName("When block trigger resolves, both Loyal Sentry and blocked creature are destroyed")
    void blockTriggerDestroysBothCreatures() {
        Permanent sentryPerm = new Permanent(new LoyalSentry());
        sentryPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(sentryPerm);

        Permanent atkPerm = new Permanent(new GrizzlyBears());
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.awaitingBlockerDeclaration = true;

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // Trigger is on the stack — resolve it
        harness.passBothPriorities();

        // Both creatures should be in their graveyards
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Loyal Sentry"));

        // Neither should be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Loyal Sentry"));
    }

    @Test
    @DisplayName("Loyal Sentry destroys a large creature it blocks")
    void destroysLargeCreature() {
        Permanent sentryPerm = new Permanent(new LoyalSentry());
        sentryPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(sentryPerm);

        // 10/10 attacker
        GrizzlyBears bigCreature = new GrizzlyBears();
        bigCreature.setPower(10);
        bigCreature.setToughness(10);
        Permanent atkPerm = new Permanent(bigCreature);
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.awaitingBlockerDeclaration = true;

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        // Even the 10/10 is destroyed by the trigger
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Loyal Sentry"));
    }

    // ===== No damage to player when attacker is destroyed =====

    @Test
    @DisplayName("Blocked attacker destroyed by Loyal Sentry deals no damage to defending player")
    void destroyedAttackerDealsNoDamageToPlayer() {
        harness.setLife(player2, 20);

        Permanent sentryPerm = new Permanent(new LoyalSentry());
        sentryPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(sentryPerm);

        GrizzlyBears bigCreature = new GrizzlyBears();
        bigCreature.setPower(5);
        bigCreature.setToughness(5);
        Permanent atkPerm = new Permanent(bigCreature);
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.awaitingBlockerDeclaration = true;

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        // Resolve the block trigger
        harness.passBothPriorities();

        // Player2 should take no damage — attacker was destroyed before combat damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    // ===== Trigger does not destroy if creatures are already gone =====

    @Test
    @DisplayName("Trigger does nothing if attacker is removed before resolution")
    void triggerDoesNothingIfAttackerAlreadyGone() {
        Permanent sentryPerm = new Permanent(new LoyalSentry());
        sentryPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(sentryPerm);

        Permanent atkPerm = new Permanent(new GrizzlyBears());
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.awaitingBlockerDeclaration = true;

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // Remove the attacker before trigger resolves
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        // Loyal Sentry is still destroyed (self-destruct part still applies)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Loyal Sentry"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Loyal Sentry"));
    }

    @Test
    @DisplayName("Trigger does nothing to self if Loyal Sentry is removed before resolution")
    void triggerDoesNothingIfSentryAlreadyGone() {
        Permanent sentryPerm = new Permanent(new LoyalSentry());
        sentryPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(sentryPerm);

        Permanent atkPerm = new Permanent(new GrizzlyBears());
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.awaitingBlockerDeclaration = true;

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // Remove Loyal Sentry before trigger resolves
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        // Attacker is still destroyed by the trigger
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Game log =====

    @Test
    @DisplayName("Block trigger generates appropriate game log entries")
    void blockTriggerGeneratesLogEntries() {
        Permanent sentryPerm = new Permanent(new LoyalSentry());
        sentryPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(sentryPerm);

        Permanent atkPerm = new Permanent(new GrizzlyBears());
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.awaitingBlockerDeclaration = true;

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.gameLog).anyMatch(log -> log.contains("Loyal Sentry") && log.contains("block") && log.contains("trigger"));

        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("Grizzly Bears") && log.contains("destroyed"));
        assertThat(gd.gameLog).anyMatch(log -> log.contains("Loyal Sentry") && log.contains("destroyed"));
    }

    // ===== Normal creatures don't trigger on block =====

    @Test
    @DisplayName("Normal creature blocking does not push any trigger onto the stack")
    void normalCreatureDoesNotTriggerOnBlock() {
        Permanent blockerPerm = new Permanent(new GrizzlyBears());
        blockerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        Permanent atkPerm = new Permanent(new GrizzlyBears());
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.awaitingBlockerDeclaration = true;

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
    }

    // ===== Enters with summoning sickness =====

    @Test
    @DisplayName("Loyal Sentry enters battlefield with summoning sickness")
    void entersBattlefieldWithSummoningSickness() {
        harness.setHand(player1, List.of(new LoyalSentry()));
        harness.addMana(player1, "W", 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent perm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Loyal Sentry"))
                .findFirst().orElseThrow();
        assertThat(perm.isSummoningSick()).isTrue();
    }
}
