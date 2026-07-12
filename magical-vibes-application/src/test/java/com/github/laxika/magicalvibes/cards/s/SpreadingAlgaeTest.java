package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpreadingAlgaeTest extends BaseCardTest {

    // ===== Casting and targeting =====

    @Test
    @DisplayName("Can cast Spreading Algae targeting a Swamp")
    void canTargetSwamp() {
        harness.addToBattlefield(player1, new Swamp());
        Permanent swamp = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new SpreadingAlgae()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);

        harness.castEnchantment(player1, 0, swamp.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(swamp.getId());
    }

    @Test
    @DisplayName("Cannot cast Spreading Algae targeting a non-Swamp permanent")
    void cannotTargetNonSwamp() {
        harness.addToBattlefield(player1, new Swamp()); // valid target so spell is playable
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent creature = findPermanent(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new SpreadingAlgae()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Swamp");
    }

    @Test
    @DisplayName("Resolving Spreading Algae attaches it to the target Swamp")
    void resolvingAttachesToSwamp() {
        harness.addToBattlefield(player1, new Swamp());
        Permanent swamp = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new SpreadingAlgae()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);

        harness.castEnchantment(player1, 0, swamp.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Spreading Algae")
                        && swamp.getId().equals(p.getAttachedTo()));
    }

    // ===== Tap trigger: destroy the enchanted land =====

    @Test
    @DisplayName("Tapping the enchanted Swamp triggers the destroy ability (deferred as a mana-ability trigger)")
    void tappingSwampTriggersDestroy() {
        addSwampWithAura();

        // Tapping a land for mana defers its triggers (CR 603.3) until a player next gets priority.
        harness.tapPermanent(player1, 0);

        assertThat(gd.pendingManaAbilityTriggers).anySatisfy(entry -> {
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getCard().getName()).isEqualTo("Spreading Algae");
        });
    }

    @Test
    @DisplayName("Tapping the enchanted Swamp destroys it")
    void tappingSwampDestroysIt() {
        addSwampWithAura();

        harness.tapPermanent(player1, 0);
        resolveStackFully();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Swamp"));
    }

    @Test
    @DisplayName("Tapping an un-enchanted Swamp does not destroy it")
    void tappingUnenchantedSwampDoesNotDestroy() {
        harness.addToBattlefield(player1, new Swamp());

        harness.tapPermanent(player1, 0);
        resolveStackFully();

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Spreading Algae"));
        assertThat(gd.pendingManaAbilityTriggers)
                .noneMatch(entry -> entry.getCard().getName().equals("Spreading Algae"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Swamp"));
    }

    // ===== Graveyard-from-battlefield trigger: return to hand =====

    @Test
    @DisplayName("When the enchanted Swamp is destroyed, Spreading Algae returns to its owner's hand")
    void auraReturnsToHandAfterDestroy() {
        addSwampWithAura();

        harness.tapPermanent(player1, 0);
        // Resolve the destroy trigger, then the return-to-hand trigger it spawns.
        resolveStackFully();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Spreading Algae"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Spreading Algae"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Spreading Algae"));
    }

    // ===== Helpers =====

    /**
     * Places a Swamp on player1's battlefield (index 0) with a Spreading Algae attached (index 1).
     *
     * @return the Swamp permanent
     */
    private Permanent addSwampWithAura() {
        harness.addToBattlefield(player1, new Swamp());
        Permanent swamp = gd.playerBattlefields.get(player1.getId()).getFirst();

        SpreadingAlgae auraCard = new SpreadingAlgae();
        Permanent aura = new Permanent(auraCard);
        aura.setAttachedTo(swamp.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        return swamp;
    }

    /**
     * Drives priority until the stack and any deferred mana-ability triggers are fully resolved.
     * Each passBothPriorities() call handles a single flush/resolve step, so several are needed to
     * work through a deferred trigger that spawns another trigger.
     */
    private void resolveStackFully() {
        for (int i = 0; i < 8 && (!gd.stack.isEmpty() || !gd.pendingManaAbilityTriggers.isEmpty()); i++) {
            harness.passBothPriorities();
        }
    }
}
