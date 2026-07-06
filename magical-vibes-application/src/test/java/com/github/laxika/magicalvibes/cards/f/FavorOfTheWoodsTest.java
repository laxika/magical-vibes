package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FavorOfTheWoodsTest extends BaseCardTest {

    // ===== Block trigger =====

    @Test
    @DisplayName("Enchanted creature blocking pushes Favor of the Woods trigger onto the stack")
    void blockTriggerPushesOntoStack() {
        Permanent creature = addReadyCreature(player2, new GrizzlyBears());

        // Add attacker before aura so attacker is at index 0 on player1's battlefield
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent aura = attachFavorOfTheWoods(player2, creature);

        declareBlockers(player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Favor of the Woods");
        assertThat(entry.getSourcePermanentId()).isEqualTo(aura.getId());
        // Not a targeted ability — targetId is null
        assertThat(entry.getTargetId()).isNull();
    }

    @Test
    @DisplayName("Aura's controller gains 3 life when enchanted creature blocks")
    void controllerGains3LifeOnBlock() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent creature = addReadyCreature(player2, new GrizzlyBears());

        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attachFavorOfTheWoods(player2, creature);

        declareBlockers(player2, List.of(new BlockerAssignment(0, 0)));
        // Resolve the triggered ability
        harness.passBothPriorities();

        // Player2 (aura's controller) gains 3 life: 20 + 3 = 23
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(23);
    }

    // ===== "You" is the aura's controller, not the creature's controller =====

    @Test
    @DisplayName("Aura's controller gains the life even when enchanting an opponent's creature")
    void auraControllerGainsLifeWhenOnOpponentCreature() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // player2's creature blocks, but player1 controls the aura
        Permanent creature = addReadyCreature(player2, new GrizzlyBears());

        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attachFavorOfTheWoods(player1, creature);

        declareBlockers(player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        // Player1 (aura's controller) gains 3 life — not player2 who controls the creature
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    // ===== No trigger when enchanted creature doesn't block =====

    @Test
    @DisplayName("No trigger when a different creature blocks")
    void noTriggerWhenEnchantedCreatureDoesNotBlock() {
        Permanent enchantedCreature = addReadyCreature(player2, new GrizzlyBears());
        Permanent otherCreature = addReadyCreature(player2, new GrizzlyBears());

        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attachFavorOfTheWoods(player2, enchantedCreature);

        // The non-enchanted creature (index 1) blocks
        declareBlockers(player2, List.of(new BlockerAssignment(1, 0)));

        assertThat(gd.stack).isEmpty();
    }

    // ===== No trigger on attack (block-only) =====

    @Test
    @DisplayName("No trigger when enchanted creature attacks")
    void noTriggerOnAttack() {
        Permanent creature = addReadyCreature(player1, new GrizzlyBears());
        attachFavorOfTheWoods(player1, creature);

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).isEmpty();
    }

    // ===== Creature with no aura does not trigger =====

    @Test
    @DisplayName("Creature without Favor of the Woods does not push any aura trigger")
    void creatureWithoutAuraDoesNotTrigger() {
        addReadyCreature(player2, new GrizzlyBears());

        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        declareBlockers(player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
    }

    // ===== Trigger still resolves even if creature leaves battlefield =====

    @Test
    @DisplayName("Trigger still grants life even if enchanted creature leaves battlefield before resolution")
    void triggerStillResolvesIfCreatureRemoved() {
        harness.setLife(player2, 20);

        Permanent creature = addReadyCreature(player2, new GrizzlyBears());

        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attachFavorOfTheWoods(player2, creature);

        declareBlockers(player2, List.of(new BlockerAssignment(0, 0)));

        // Trigger is on the stack
        assertThat(gd.stack).hasSize(1);

        // Remove creature before trigger resolves
        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getId().equals(creature.getId()));

        harness.passBothPriorities();

        // Life gain still applies — the triggered ability already went on the stack and
        // captured the aura's controller, independent of the creature leaving the battlefield.
        assertThat(gd.gameLog).anyMatch(log -> log.contains("gains") && log.contains("3") && log.contains("life"));
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Can target a creature with Favor of the Woods")
    void canTargetCreature() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        harness.setHand(player1, List.of(new FavorOfTheWoods()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Favor of the Woods")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new com.github.laxika.magicalvibes.cards.f.FountainOfYouth());
        harness.setHand(player1, List.of(new FavorOfTheWoods()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        Permanent artifact = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fountain of Youth"))
                .findFirst().orElseThrow();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    // ===== Game log =====

    @Test
    @DisplayName("Favor of the Woods trigger generates appropriate game log entries")
    void triggerGeneratesLogEntries() {
        harness.setLife(player2, 20);

        Permanent creature = addReadyCreature(player2, new GrizzlyBears());

        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attachFavorOfTheWoods(player2, creature);

        declareBlockers(player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.gameLog).anyMatch(log -> log.contains("Favor of the Woods") && log.contains("triggers"));

        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("gain") && log.contains("3") && log.contains("life"));
    }

    // ===== Helpers =====

    private Permanent addReadyCreature(Player player, GrizzlyBears card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent attachFavorOfTheWoods(Player controller, Permanent target) {
        FavorOfTheWoods card = new FavorOfTheWoods();
        Permanent aura = new Permanent(card);
        aura.setAttachedTo(target.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
        return aura;
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices);
    }

    private void declareBlockers(Player player, List<BlockerAssignment> assignments) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player, assignments);
    }
}
