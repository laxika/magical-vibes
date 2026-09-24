package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.a.AncientDen;
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

@CardUsed({ContaminatedBond.class, AlphaMyr.class, AncientDen.class})
class ContaminatedBondTest extends BaseCardTest {

    // ===== Attack trigger =====

    @Test
    @DisplayName("Enchanted creature attacking pushes Contaminated Bond trigger onto the stack")
    void attackTriggerPushesOntoStack() {
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        Permanent aura = attachContaminatedBond(player2, creature);

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Contaminated Bond");
        assertThat(entry.getSourcePermanentId()).isEqualTo(aura.getId());
        // Not a targeted ability — targetId is null
        assertThat(entry.getTargetId()).isNull();
    }

    @Test
    @DisplayName("Enchanted creature's controller loses 3 life when it attacks")
    void creatureControllerLoses3LifeOnAttack() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        attachContaminatedBond(player2, creature);

        declareAttackers(player1, List.of(0));
        // Resolve the triggered ability
        harness.passBothPriorities();

        // Player1 (creature's controller) loses 3 life from Contaminated Bond: 20 - 3 = 17
        harness.assertLife(player1, 17);
        // Player2 takes 2 combat damage from the unblocked 2/1 attacker: 20 - 2 = 18
        harness.assertLife(player2, 18);
    }

    // ===== Block trigger =====

    @Test
    @DisplayName("Enchanted creature blocking pushes Contaminated Bond trigger onto the stack")
    void blockTriggerPushesOntoStack() {
        Permanent creature = addCreatureReady(player2, new AlphaMyr());

        addCreatureReady(player1, new AlphaMyr());
        Permanent aura = attachContaminatedBond(player1, creature);

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Contaminated Bond");
        assertThat(entry.getSourcePermanentId()).isEqualTo(aura.getId());
        // Not a targeted ability — targetId is null
        assertThat(entry.getTargetId()).isNull();
    }

    @Test
    @DisplayName("Enchanted creature's controller loses 3 life when it blocks")
    void creatureControllerLoses3LifeOnBlock() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent creature = addCreatureReady(player2, new AlphaMyr());

        addCreatureReady(player1, new AlphaMyr());
        attachContaminatedBond(player1, creature);

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        // Resolve the triggered ability
        harness.passBothPriorities();

        // Player2 (creature's controller) loses 3 life from Contaminated Bond
        harness.assertLife(player2, 17);
        // Player1 (aura's controller) is unaffected by the trigger
        harness.assertLife(player1, 20);
    }

    // ===== Aura on own creature =====

    @Test
    @DisplayName("Controller loses life even when aura is on their own creature")
    void ownCreatureAttackingStillCausesLifeLoss() {
        harness.setLife(player1, 20);

        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        attachContaminatedBond(player1, creature);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        // Player1 controls both the aura and the creature — still loses 3 life
        harness.assertLife(player1, 17);
    }

    // ===== No trigger when creature doesn't attack or block =====

    @Test
    @DisplayName("No trigger when enchanted creature does not attack")
    void noTriggerWhenCreatureDoesNotAttack() {
        Permanent enchantedCreature = addCreatureReady(player1, new AlphaMyr());
        attachContaminatedBond(player2, enchantedCreature);

        // A different creature attacks
        addCreatureReady(player1, new AlphaMyr());

        declareAttackers(player1, List.of(1));

        // No Contaminated Bond trigger — only the non-enchanted creature attacked
        assertThat(gd.stack).isEmpty();
    }

    // ===== Creature with no aura does not trigger =====

    @Test
    @DisplayName("Creature without Contaminated Bond does not push any aura trigger")
    void creatureWithoutAuraDoesNotTrigger() {
        addCreatureReady(player1, new AlphaMyr());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).isEmpty();
    }

    // ===== Trigger still resolves even if creature leaves battlefield =====

    @Test
    @DisplayName("Trigger still causes life loss even if enchanted creature leaves battlefield before resolution")
    void triggerStillResolvesIfCreatureRemoved() {
        harness.setLife(player1, 20);

        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        Permanent aura = attachContaminatedBond(player2, creature);

        declareAttackers(player1, List.of(0));

        // Remove both the enchanted creature and its now-orphaned Aura before the trigger resolves.
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura);
        });

        harness.passBothPriorities();

        // Life loss still applies — the triggered ability already captured the creature's controller
        harness.assertLife(player1, 17);
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Can target a creature with Contaminated Bond")
    void canTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AlphaMyr());
        harness.setHand(player1, List.of(new ContaminatedBond()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Resolving Contaminated Bond attaches it to the targeted creature")
    void resolvingAttachesToTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());
        harness.setHand(player1, List.of(new ContaminatedBond()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Contaminated Bond").getAttachedTo())
                .isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Contaminated Bond")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new AlphaMyr());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AncientDen());
        harness.setHand(player1, List.of(new ContaminatedBond()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    // ===== Game log =====

    @Test
    @DisplayName("Contaminated Bond trigger generates appropriate game log entries")
    void triggerGeneratesLogEntries() {
        harness.setLife(player1, 20);

        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        attachContaminatedBond(player2, creature);

        declareAttackers(player1, List.of(0));

        assertThat(gameLogContains("Contaminated Bond's ability triggers.")).isTrue();

        harness.passBothPriorities();

        assertThat(gameLogContains("loses 3 life")).isTrue();
    }

    private Permanent attachContaminatedBond(Player controller, Permanent target) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new ContaminatedBond());
        aura.setAttachedTo(target.getId());
        return aura;
    }
}

