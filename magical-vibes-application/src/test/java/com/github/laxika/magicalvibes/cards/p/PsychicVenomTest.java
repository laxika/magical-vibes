package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PsychicVenomTest extends BaseCardTest {

    // ===== Casting and targeting =====

    @Test
    @DisplayName("Can cast Psychic Venom targeting a land")
    void canTargetLand() {
        harness.addToBattlefield(player1, new Mountain());
        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new PsychicVenom()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player1);

        harness.castEnchantment(player1, 0, land.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(land.getId());
    }

    @Test
    @DisplayName("Cannot cast Psychic Venom targeting a non-land permanent")
    void cannotTargetNonLand() {
        harness.addToBattlefield(player1, new Mountain()); // valid target so spell is playable
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent creature = findPermanent(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new PsychicVenom()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    // ===== Tap trigger: 2 damage to the land's controller =====

    @Test
    @DisplayName("Tapping the enchanted land queues the damage trigger (deferred as a mana-ability trigger)")
    void tappingLandQueuesTrigger() {
        addLandWithAura(player1);

        // Tapping a land for mana defers its triggers (CR 603.3) until a player next gets priority.
        harness.tapPermanent(player1, 0);

        assertThat(gd.pendingManaAbilityTriggers).anySatisfy(entry -> {
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getCard().getName()).isEqualTo("Psychic Venom");
        });
    }

    @Test
    @DisplayName("Tapping the enchanted land deals 2 damage to that land's controller")
    void tappingLandDamagesController() {
        addLandWithAura(player1);
        harness.setLife(player1, 20);

        harness.tapPermanent(player1, 0);
        resolveStackFully();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Damage goes to the enchanted land's controller, not the Aura's controller")
    void damagesLandControllerNotAuraController() {
        // Aura is controlled by player1 but attached to a land player2 controls.
        harness.addToBattlefield(player2, new Mountain());
        Permanent land = gd.playerBattlefields.get(player2.getId()).getFirst();

        PsychicVenom auraCard = new PsychicVenom();
        Permanent aura = new Permanent(auraCard);
        aura.setAttachedTo(land.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.tapPermanent(player2, 0);
        resolveStackFully();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Tapping an un-enchanted land does not deal damage")
    void tappingUnenchantedLandDoesNotDamage() {
        harness.addToBattlefield(player1, new Mountain());
        harness.setLife(player1, 20);

        harness.tapPermanent(player1, 0);
        resolveStackFully();

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Psychic Venom"));
        assertThat(gd.pendingManaAbilityTriggers)
                .noneMatch(entry -> entry.getCard().getName().equals("Psychic Venom"));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    // ===== Helpers =====

    /**
     * Places a land on {@code owner}'s battlefield (index 0) with a Psychic Venom attached (index 1).
     */
    private void addLandWithAura(Player owner) {
        harness.addToBattlefield(owner, new Mountain());
        Permanent land = gd.playerBattlefields.get(owner.getId()).getFirst();

        PsychicVenom auraCard = new PsychicVenom();
        Permanent aura = new Permanent(auraCard);
        aura.setAttachedTo(land.getId());
        gd.playerBattlefields.get(owner.getId()).add(aura);
    }

    /**
     * Drives priority until the stack and any deferred mana-ability triggers are fully resolved.
     */
    private void resolveStackFully() {
        for (int i = 0; i < 8 && (!gd.stack.isEmpty() || !gd.pendingManaAbilityTriggers.isEmpty()); i++) {
            harness.passBothPriorities();
        }
    }
}
