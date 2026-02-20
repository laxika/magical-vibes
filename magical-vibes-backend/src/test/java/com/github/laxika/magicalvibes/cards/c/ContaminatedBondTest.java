package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureControllerLosesLifeEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ContaminatedBondTest {

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
    @DisplayName("Contaminated Bond has correct card properties")
    void hasCorrectProperties() {
        ContaminatedBond card = new ContaminatedBond();

        assertThat(card.getName()).isEqualTo("Contaminated Bond");
        assertThat(card.getType()).isEqualTo(CardType.ENCHANTMENT);
        assertThat(card.getManaCost()).isEqualTo("{1}{B}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLACK);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.AURA);
        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.ON_ATTACK)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ATTACK).getFirst()).isInstanceOf(EnchantedCreatureControllerLosesLifeEffect.class);
        assertThat(card.getEffects(EffectSlot.ON_BLOCK)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_BLOCK).getFirst()).isInstanceOf(EnchantedCreatureControllerLosesLifeEffect.class);
    }

    // ===== Attack trigger =====

    @Test
    @DisplayName("Enchanted creature attacking pushes Contaminated Bond trigger onto the stack")
    void attackTriggerPushesOntoStack() {
        Permanent creature = addReadyCreature(player1, new GrizzlyBears());
        Permanent aura = attachContaminatedBond(player2, creature);

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Contaminated Bond");
        assertThat(entry.getSourcePermanentId()).isEqualTo(aura.getId());
        // Not a targeted ability — targetPermanentId is null
        assertThat(entry.getTargetPermanentId()).isNull();
    }

    @Test
    @DisplayName("Enchanted creature's controller loses 3 life when it attacks")
    void creatureControllerLoses3LifeOnAttack() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent creature = addReadyCreature(player1, new GrizzlyBears());
        attachContaminatedBond(player2, creature);

        declareAttackers(player1, List.of(0));
        // Resolve the triggered ability
        harness.passBothPriorities();

        // Player1 (creature's controller) loses 3 life from Contaminated Bond: 20 - 3 = 17
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        // Player2 takes 2 combat damage from the unblocked 2/2 attacker: 20 - 2 = 18
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    // ===== Block trigger =====

    @Test
    @DisplayName("Enchanted creature blocking pushes Contaminated Bond trigger onto the stack")
    void blockTriggerPushesOntoStack() {
        Permanent creature = addReadyCreature(player2, new GrizzlyBears());

        // Add attacker before aura so attacker is at index 0 on player1's battlefield
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent aura = attachContaminatedBond(player1, creature);

        declareBlockers(player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Contaminated Bond");
        assertThat(entry.getSourcePermanentId()).isEqualTo(aura.getId());
        // Not a targeted ability — targetPermanentId is null
        assertThat(entry.getTargetPermanentId()).isNull();
    }

    @Test
    @DisplayName("Enchanted creature's controller loses 3 life when it blocks")
    void creatureControllerLoses3LifeOnBlock() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent creature = addReadyCreature(player2, new GrizzlyBears());

        // Add attacker before aura so attacker is at index 0 on player1's battlefield
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attachContaminatedBond(player1, creature);

        declareBlockers(player2, List.of(new BlockerAssignment(0, 0)));
        // Resolve the triggered ability
        harness.passBothPriorities();

        // Player2 (creature's controller) loses 3 life from Contaminated Bond
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        // Player1 (aura's controller) is unaffected by the trigger
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    // ===== Aura on own creature =====

    @Test
    @DisplayName("Controller loses life even when aura is on their own creature")
    void ownCreatureAttackingStillCausesLifeLoss() {
        harness.setLife(player1, 20);

        Permanent creature = addReadyCreature(player1, new GrizzlyBears());
        attachContaminatedBond(player1, creature);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        // Player1 controls both the aura and the creature — still loses 3 life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    // ===== No trigger when creature doesn't attack or block =====

    @Test
    @DisplayName("No trigger when enchanted creature does not attack")
    void noTriggerWhenCreatureDoesNotAttack() {
        Permanent enchantedCreature = addReadyCreature(player1, new GrizzlyBears());
        attachContaminatedBond(player2, enchantedCreature);

        // A different creature attacks
        Permanent otherCreature = addReadyCreature(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));

        // No Contaminated Bond trigger — only the non-enchanted creature attacked
        assertThat(gd.stack).isEmpty();
    }

    // ===== Creature with no aura does not trigger =====

    @Test
    @DisplayName("Creature without Contaminated Bond does not push any aura trigger")
    void creatureWithoutAuraDoesNotTrigger() {
        addReadyCreature(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).isEmpty();
    }

    // ===== Trigger still resolves even if creature leaves battlefield =====

    @Test
    @DisplayName("Trigger still causes life loss even if enchanted creature leaves battlefield before resolution")
    void triggerStillResolvesIfCreatureRemoved() {
        harness.setLife(player1, 20);

        Permanent creature = addReadyCreature(player1, new GrizzlyBears());
        attachContaminatedBond(player2, creature);

        declareAttackers(player1, List.of(0));

        // Remove creature before trigger resolves
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        // Life loss still applies — the triggered ability already captured the creature's controller
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    // ===== Game log =====

    @Test
    @DisplayName("Contaminated Bond trigger generates appropriate game log entries")
    void triggerGeneratesLogEntries() {
        harness.setLife(player1, 20);

        Permanent creature = addReadyCreature(player1, new GrizzlyBears());
        attachContaminatedBond(player2, creature);

        declareAttackers(player1, List.of(0));

        assertThat(gd.gameLog).anyMatch(log -> log.contains("Contaminated Bond") && log.contains("triggers"));

        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("loses") && log.contains("3") && log.contains("life"));
    }

    // ===== Helpers =====

    private Permanent addReadyCreature(Player player, GrizzlyBears card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent attachContaminatedBond(Player controller, Permanent target) {
        ContaminatedBond card = new ContaminatedBond();
        Permanent aura = new Permanent(card);
        aura.setAttachedTo(target.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
        return aura;
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.awaitingInput = AwaitingInput.ATTACKER_DECLARATION;
        gs.declareAttackers(gd, player, attackerIndices);
    }

    private void declareBlockers(Player player, List<BlockerAssignment> assignments) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.awaitingInput = AwaitingInput.BLOCKER_DECLARATION;
        gs.declareBlockers(gd, player, assignments);
    }
}

