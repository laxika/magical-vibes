package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.ManaShort;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PsychicVenom.class, Mountain.class, GrizzlyBears.class, ManaShort.class})
class PsychicVenomTest extends BaseCardTest {
    @Test
    @DisplayName("Can cast Psychic Venom targeting a land")
    void canTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
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
    @DisplayName("Can cast Psychic Venom targeting an opponent's land")
    void canTargetOpponentsLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new PsychicVenom()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player1);

        harness.castEnchantment(player1, 0, land.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof PsychicVenom
                        && land.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Resolving Psychic Venom attaches it to the target land")
    void resolvingAttachesToTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new PsychicVenom()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player1);

        harness.castEnchantment(player1, 0, land.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof PsychicVenom
                        && land.getId().equals(permanent.getAttachedTo()));
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

    @Test
    @DisplayName("Tapping the enchanted land queues the damage trigger (deferred as a mana-ability trigger)")
    void tappingLandQueuesTrigger() {
        addLandWithAura(player1);

        // Tapping a land for mana defers its triggers (CR 603.3) until a player next gets priority.
        harness.tapPermanent(player1, 0);

        assertThat(gd.pendingManaAbilityTriggers).anySatisfy(entry -> {
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        });
    }

    @Test
    @DisplayName("Tapping the enchanted land deals 2 damage to that land's controller")
    void tappingLandDamagesController() {
        addLandWithAura(player1);
        harness.setLife(player1, 20);

        harness.tapPermanent(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Tapping the enchanted land for a non-mana reason also deals damage")
    void tappingLandForNonManaReasonDamagesController() {
        addLandWithAura(player1);
        harness.setHand(player1, List.of(new ManaShort()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);

        harness.castAndResolveInstant(player1, 0, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Damage goes to the enchanted land's controller, not the Aura's controller")
    void damagesLandControllerNotAuraController() {
        // Aura is controlled by player1 but attached to a land player2 controls.
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());

        PsychicVenom auraCard = new PsychicVenom();
        Permanent aura = new Permanent(auraCard);
        aura.setAttachedTo(land.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.tapPermanent(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Damage uses the land's controller when the trigger resolves")
    void damagesCurrentLandControllerAfterControlChange() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        PsychicVenom auraCard = new PsychicVenom();
        Permanent aura = new Permanent(auraCard);
        aura.setAttachedTo(land.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.tapPermanent(player1, 0);
        gd.playerBattlefields.get(player1.getId()).remove(land);
        gd.playerBattlefields.get(player2.getId()).add(land);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Tapping an un-enchanted land does not deal damage")
    void tappingUnenchantedLandDoesNotDamage() {
        harness.addToBattlefield(player1, new Mountain());
        harness.setLife(player1, 20);

        harness.tapPermanent(player1, 0);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingManaAbilityTriggers).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    /**
     * Places a land on {@code owner}'s battlefield (index 0) with a Psychic Venom attached (index 1).
     */
    private void addLandWithAura(Player owner) {
        Permanent land = harness.addToBattlefieldAndReturn(owner, new Mountain());

        PsychicVenom auraCard = new PsychicVenom();
        Permanent aura = new Permanent(auraCard);
        aura.setAttachedTo(land.getId());
        gd.playerBattlefields.get(owner.getId()).add(aura);
    }
}
