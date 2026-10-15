package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.d.DawntreaderElk;
import com.github.laxika.magicalvibes.cards.g.GuardianOfTheGateless;
import com.github.laxika.magicalvibes.cards.h.HeavyMattock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FavorOfTheWoods.class, DawntreaderElk.class, HeavyMattock.class, GuardianOfTheGateless.class})
class FavorOfTheWoodsTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature blocking pushes Favor of the Woods trigger onto the stack")
    void blockTriggerPushesOntoStack() {
        Permanent creature = addCreatureReady(player2, new DawntreaderElk());

        // Add attacker before aura so attacker is at index 0 on player1's battlefield
        Permanent attacker = addCreatureReady(player1, new DawntreaderElk());
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

        Permanent creature = addCreatureReady(player2, new DawntreaderElk());

        Permanent attacker = addCreatureReady(player1, new DawntreaderElk());
        attacker.setAttacking(true);
        attachFavorOfTheWoods(player2, creature);

        declareBlockers(player2, List.of(new BlockerAssignment(0, 0)));
        // Resolve the triggered ability
        harness.passBothPriorities();

        // Player2 (aura's controller) gains 3 life: 20 + 3 = 23
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Aura's controller gains the life even when enchanting an opponent's creature")
    void auraControllerGainsLifeWhenOnOpponentCreature() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // player2's creature blocks, but player1 controls the aura
        Permanent creature = addCreatureReady(player2, new DawntreaderElk());

        Permanent attacker = addCreatureReady(player1, new DawntreaderElk());
        attacker.setAttacking(true);
        attachFavorOfTheWoods(player1, creature);

        declareBlockers(player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        // Player1 (aura's controller) gains 3 life — not player2 who controls the creature
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("No trigger when a different creature blocks")
    void noTriggerWhenEnchantedCreatureDoesNotBlock() {
        Permanent enchantedCreature = addCreatureReady(player2, new DawntreaderElk());
        addCreatureReady(player2, new DawntreaderElk());

        Permanent attacker = addCreatureReady(player1, new DawntreaderElk());
        attacker.setAttacking(true);
        attachFavorOfTheWoods(player2, enchantedCreature);

        // The non-enchanted creature (index 1) blocks
        declareBlockers(player2, List.of(new BlockerAssignment(1, 0)));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("No trigger when enchanted creature attacks")
    void noTriggerOnAttack() {
        Permanent creature = addCreatureReady(player1, new DawntreaderElk());
        attachFavorOfTheWoods(player1, creature);

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Creature without Favor of the Woods does not push any aura trigger")
    void creatureWithoutAuraDoesNotTrigger() {
        addCreatureReady(player2, new DawntreaderElk());

        Permanent attacker = addCreatureReady(player1, new DawntreaderElk());
        attacker.setAttacking(true);

        declareBlockers(player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Trigger still grants life even if enchanted creature leaves battlefield before resolution")
    void triggerStillResolvesIfCreatureRemoved() {
        harness.setLife(player2, 20);

        Permanent creature = addCreatureReady(player2, new DawntreaderElk());

        Permanent attacker = addCreatureReady(player1, new DawntreaderElk());
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
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("gains") && log.contains("3") && log.contains("life"));
    }

    @Test
    @DisplayName("Can target a creature with Favor of the Woods")
    void canTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new DawntreaderElk());
        harness.setHand(player1, List.of(new FavorOfTheWoods()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Favor of the Woods")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new DawntreaderElk());
        harness.addToBattlefield(player1, new HeavyMattock());
        harness.setHand(player1, List.of(new FavorOfTheWoods()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        Permanent artifact = findPermanent(player1, "Heavy Mattock");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Favor of the Woods trigger generates appropriate game log entries")
    void triggerGeneratesLogEntries() {
        harness.setLife(player2, 20);

        Permanent creature = addCreatureReady(player2, new DawntreaderElk());

        Permanent attacker = addCreatureReady(player1, new DawntreaderElk());
        attacker.setAttacking(true);
        attachFavorOfTheWoods(player2, creature);

        declareBlockers(player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("Favor of the Woods") && log.contains("triggers"));

        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("gain") && log.contains("3") && log.contains("life"));
    }

    @Test
    @CardUsed(GuardianOfTheGateless.class)
    @DisplayName("Blocking multiple attackers grants only 3 life")
    void gainsLifeOnlyOnceWhenBlockingMultipleAttackers() {
        harness.setLife(player2, 20);
        Permanent blocker = addCreatureReady(player2, new GuardianOfTheGateless());
        addCreatureReady(player1, new DawntreaderElk()).setAttacking(true);
        addCreatureReady(player1, new DawntreaderElk()).setAttacking(true);
        attachFavorOfTheWoods(player2, blocker);

        declareBlockers(player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(0, 1)));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Life gain resolves after the Aura leaves the battlefield")
    void triggerResolvesAfterAuraLeavesBattlefield() {
        harness.setLife(player2, 20);
        Permanent blocker = addCreatureReady(player2, new DawntreaderElk());
        addCreatureReady(player1, new DawntreaderElk()).setAttacking(true);
        Permanent aura = attachFavorOfTheWoods(player2, blocker);

        declareBlockers(player2, List.of(new BlockerAssignment(0, 0)));
        gd.playerBattlefields.get(player2.getId()).remove(aura);
        gd.playerGraveyards.get(player2.getId()).add(aura.getCard());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(23);
    }


    @Test
    @DisplayName("Resolved Aura enchants an opponent's creature and triggers for its controller")
    void resolvedAuraTriggersWhenOpponentsCreatureBlocks() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent blocker = addCreatureReady(player2, new DawntreaderElk());
        Permanent attacker = addCreatureReady(player1, new DawntreaderElk());
        harness.setHand(player1, List.of(new FavorOfTheWoods()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, blocker.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Favor of the Woods").getAttachedTo()).isEqualTo(blocker.getId());
        attacker.setAttacking(true);
        declareBlockers(player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private Permanent attachFavorOfTheWoods(Player controller, Permanent target) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new FavorOfTheWoods());
        aura.setAttachedTo(target.getId());
        return aura;
    }

    private void declareBlockers(Player player, List<BlockerAssignment> assignments) {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player, assignments);
    }
}
