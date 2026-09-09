package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RobeOfMirrors.class, Boomerang.class, GrizzlyBears.class, IcyManipulator.class,
        Mountain.class, Pacifism.class})
class RobeOfMirrorsTest extends BaseCardTest {

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Robe of Mirrors puts it on the stack")
    void castingPutsOnStack() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        RobeOfMirrors robe = new RobeOfMirrors();
        harness.setHand(player1, List.of(robe));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, bearsPerm.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(robe);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(bearsPerm.getId());
    }

    @Test
    @DisplayName("Resolving Robe of Mirrors attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        RobeOfMirrors robe = new RobeOfMirrors();
        harness.setHand(player1, List.of(robe));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, bearsPerm.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() == robe
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    // ===== Shroud grants targeting protection =====

    @Test
    @DisplayName("Enchanted creature cannot be targeted by controller's spells (shroud blocks all targeting)")
    void enchantedCreatureCannotBeTargetedByController() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        // Attach Robe of Mirrors directly
        attachRobe(bearsPerm, player1);

        // Controller tries to target own creature with Boomerang
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bearsPerm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Enchanted creature cannot be targeted by opponent's spells")
    void enchantedCreatureCannotBeTargetedByOpponent() {
        // Player2 owns the creature with Robe, and is active player
        harness.forceActivePlayer(player2);
        Permanent bearsPerm = addCreatureReady(player2, new GrizzlyBears());

        // Attach Robe of Mirrors directly
        attachRobe(bearsPerm, player2);

        // Player2 passes priority so player1 gets it
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bearsPerm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Enchanted creature cannot be targeted by activated abilities")
    void enchantedCreatureCannotBeTargetedByActivatedAbility() {
        Permanent icyManipulator = harness.addToBattlefieldAndReturn(player1, new IcyManipulator());
        icyManipulator.setSummoningSick(false);
        Permanent bearsPerm = addCreatureReady(player2, new GrizzlyBears());
        attachRobe(bearsPerm, player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bearsPerm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Enchanted creature cannot be targeted by controller's auras")
    void enchantedCreatureCannotBeTargetedByAuras() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        // Attach Robe of Mirrors directly
        attachRobe(bearsPerm, player1);

        // Add another creature without shroud so aura is playable
        addCreatureReady(player1, new GrizzlyBears());

        // Controller tries to target the creature with Pacifism
        harness.setHand(player1, List.of(new Pacifism()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bearsPerm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    // ===== Shroud removed when Robe is removed =====

    @Test
    @DisplayName("Creature can be targeted again after Robe of Mirrors is removed")
    void creatureCanBeTargetedAfterRobeRemoved() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        // Attach Robe of Mirrors
        Permanent robePerm = attachRobe(bearsPerm, player1);

        // Verify creature has shroud
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.SHROUD)).isTrue();

        // Remove the Robe
        gd.playerBattlefields.get(player1.getId()).remove(robePerm);

        // Verify creature no longer has shroud
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.SHROUD)).isFalse();

        // Now creature can be targeted — cast Boomerang on it
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, bearsPerm.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(bearsPerm.getId());
    }

    // ===== Fizzles if target removed =====

    @Test
    @DisplayName("Robe of Mirrors fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new RobeOfMirrors()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, bearsPerm.getId());

        // Remove the target before resolution
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Robe of Mirrors");
        harness.assertNotOnBattlefield(player1, "Robe of Mirrors");
    }

    // ===== Can be cast on own creature =====

    @Test
    @DisplayName("Robe of Mirrors can be cast on own creature")
    void canCastOnOwnCreature() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        RobeOfMirrors robe = new RobeOfMirrors();
        harness.setHand(player1, List.of(robe));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, bearsPerm.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() == robe
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Cannot enchant a land")
    void cannotEnchantALand() {
        // A creature must exist so the spell is playable; targeting the land is then rejected.
        addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new RobeOfMirrors()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        Permanent mountain = findPermanent(player1, "Mountain");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent attachRobe(Permanent creature, Player controller) {
        Permanent robe = harness.addToBattlefieldAndReturn(controller, new RobeOfMirrors());
        robe.setAttachedTo(creature.getId());
        return robe;
    }
}

