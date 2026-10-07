package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.HonorGuard;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpectralFlight.class, GrizzlyBears.class, HonorGuard.class,
        HillGiant.class, Mountain.class})
class SpectralFlightTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Spectral Flight puts it on the stack as enchantment spell")
    void castingPutsOnStack() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new SpectralFlight()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, bearsPerm.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Spectral Flight");
    }

    @Test
    @DisplayName("Resolving Spectral Flight attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new SpectralFlight()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, bearsPerm.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Spectral Flight")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    @Test
    @DisplayName("Enchanted creature gets +2/+2")
    void enchantedCreatureGetsBoost() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent flightPerm = harness.addToBattlefieldAndReturn(player1, new SpectralFlight());
        flightPerm.setAttachedTo(bearsPerm.getId());

        // Grizzly Bears 2/2 + 2/2 = 4/4
        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(4);
    }

    @Test
    @DisplayName("Enchanted creature has flying")
    void enchantedCreatureHasFlying() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent flightPerm = harness.addToBattlefieldAndReturn(player1, new SpectralFlight());
        flightPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Spectral Flight does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent otherBears = addCreatureReady(player1, new GrizzlyBears());

        Permanent flightPerm = harness.addToBattlefieldAndReturn(player1, new SpectralFlight());
        flightPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.getEffectivePower(gd, otherBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherBears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, otherBears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Creature loses boost and flying when Spectral Flight is removed")
    void creatureLosesBoostWhenRemoved() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent flightPerm = harness.addToBattlefieldAndReturn(player1, new SpectralFlight());
        flightPerm.setAttachedTo(bearsPerm.getId());

        // Verify effects are applied
        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FLYING)).isTrue();

        // Remove Spectral Flight
        gd.playerBattlefields.get(player1.getId()).remove(flightPerm);

        // Effects should be gone
        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Spectral Flight fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new SpectralFlight()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, bearsPerm.getId());

        // Remove the target before resolution
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Spectral Flight");
        harness.assertNotOnBattlefield(player1, "Spectral Flight");
    }

    @Test
    @DisplayName("Spectral Flight goes to graveyard when enchanted creature dies")
    void goesToGraveyardWhenCreatureDies() {
        // Player2 has a 1/1 Honor Guard enchanted with Spectral Flight (becomes 3/3 with flying)
        Permanent guardPerm = addCreatureReady(player2, new HonorGuard());

        Permanent flightPerm = harness.addToBattlefieldAndReturn(player2, new SpectralFlight());
        flightPerm.setAttachedTo(guardPerm.getId());

        // Player1 has a 3/3 Hill Giant attacker — enough to kill the enchanted 3/3
        addCreatureReady(player1, new HillGiant());

        declareAttackers(List.of(0));

        // Enchanted creature (3/3 with flying) blocks the 3/3 attacker — both die
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        // Spectral Flight should be in graveyard (orphaned aura cleanup)
        harness.assertNotOnBattlefield(player2, "Spectral Flight");
        harness.assertInGraveyard(player2, "Spectral Flight");
    }

    @Test
    @DisplayName("Cannot enchant a land")
    void cannotEnchantALand() {
        // A creature must exist so the spell is playable; targeting the land is then rejected.
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new SpectralFlight()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        Permanent mountain = findPermanent(player1, "Mountain");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can enchant an opponent's creature and grant both bonuses")
    void enchantsOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpectralFlight()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Spectral Flight");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Two copies stack their boosts and either remaining copy grants flying")
    void multipleCopiesStack() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpectralFlight(), new SpectralFlight()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spectral Flight")).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Spectral Flight"));

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }
}
