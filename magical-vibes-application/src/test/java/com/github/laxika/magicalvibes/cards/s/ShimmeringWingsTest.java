package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShimmeringWings.class, GrizzlyBears.class, Mountain.class})
class ShimmeringWingsTest extends BaseCardTest {

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Shimmering Wings puts it on the stack as enchantment spell")
    void castingPutsOnStack() {
        Permanent knightPerm = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new ShimmeringWings()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, knightPerm.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
    }

    @Test
    @DisplayName("Resolving Shimmering Wings attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent knightPerm = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new ShimmeringWings()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, knightPerm.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof ShimmeringWings
                        && p.isAttached()
                        && p.getAttachedTo().equals(knightPerm.getId()));
    }

    @Test
    @DisplayName("Shimmering Wings can enchant a creature an opponent controls")
    void canEnchantOpponentCreature() {
        Permanent knightPerm = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new ShimmeringWings()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, knightPerm.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof ShimmeringWings
                        && p.isAttached()
                        && p.getAttachedTo().equals(knightPerm.getId()));
        assertThat(gqs.hasKeyword(gd, knightPerm, Keyword.FLYING)).isTrue();
    }

    // ===== Flying =====

    @Test
    @DisplayName("Enchanted creature has flying")
    void enchantedCreatureHasFlying() {
        Permanent knightPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent wingsPerm = harness.addToBattlefieldAndReturn(player1, new ShimmeringWings());
        wingsPerm.setAttachedTo(knightPerm.getId());

        assertThat(gqs.hasKeyword(gd, knightPerm, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Shimmering Wings does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent knightPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent otherKnight = addCreatureReady(player1, new GrizzlyBears());

        Permanent wingsPerm = harness.addToBattlefieldAndReturn(player1, new ShimmeringWings());
        wingsPerm.setAttachedTo(knightPerm.getId());

        assertThat(gqs.hasKeyword(gd, otherKnight, Keyword.FLYING)).isFalse();
    }

    // ===== Activated ability: return to hand =====

    @Test
    @DisplayName("Activating {U} ability returns Shimmering Wings to owner's hand")
    void activateAbilityReturnsToHand() {
        Permanent knightPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent wingsPerm = harness.addToBattlefieldAndReturn(player1, new ShimmeringWings());
        wingsPerm.setAttachedTo(knightPerm.getId());

        harness.addMana(player1, ManaColor.BLUE, 1);

        // Activate the return-to-hand ability (wings is at index 1)
        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Shimmering Wings");
        harness.assertNotOnBattlefield(player1, "Shimmering Wings");
    }

    @Test
    @DisplayName("Creature loses flying after Shimmering Wings returns to hand")
    void creatureLosesFlyingAfterBounce() {
        Permanent knightPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent wingsPerm = harness.addToBattlefieldAndReturn(player1, new ShimmeringWings());
        wingsPerm.setAttachedTo(knightPerm.getId());

        // Verify flying is granted
        assertThat(gqs.hasKeyword(gd, knightPerm, Keyword.FLYING)).isTrue();

        harness.addMana(player1, ManaColor.BLUE, 1);

        // Activate the return-to-hand ability
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        // Creature no longer has flying
        assertThat(gqs.hasKeyword(gd, knightPerm, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Returning a controlled Shimmering Wings puts it into its owner's hand")
    void returnsToOwnersHandWhenControlledByOpponent() {
        Permanent knight = addCreatureReady(player2, new GrizzlyBears());

        ShimmeringWings card = new ShimmeringWings();
        card.setOwnerId(player1.getId());
        Permanent wingsPerm = harness.addToBattlefieldAndReturn(player2, card);
        wingsPerm.setAttachedTo(knight.getId());

        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.activateAbility(player2, 1, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Shimmering Wings");
        harness.assertNotInHand(player2, "Shimmering Wings");
        harness.assertNotOnBattlefield(player2, "Shimmering Wings");
    }

    @Test
    @DisplayName("Aura controller can return Shimmering Wings while it enchants an opponent's creature")
    void auraControllerCanActivateWhenEnchantedCreatureIsOpponent() {
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        ShimmeringWings card = new ShimmeringWings();
        card.setOwnerId(player1.getId());
        Permanent wingsPerm = harness.addToBattlefieldAndReturn(player1, card);
        wingsPerm.setAttachedTo(opponentCreature.getId());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Shimmering Wings");
        harness.assertNotOnBattlefield(player1, "Shimmering Wings");
    }

    // ===== Re-cast after bounce =====

    @Test
    @DisplayName("Shimmering Wings can be re-cast after returning to hand")
    void canRecastAfterBounce() {
        Permanent knightPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent wingsPerm = harness.addToBattlefieldAndReturn(player1, new ShimmeringWings());
        wingsPerm.setAttachedTo(knightPerm.getId());

        harness.addMana(player1, ManaColor.BLUE, 1);

        // Bounce it
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Shimmering Wings");

        // Re-cast it
        harness.addMana(player1, ManaColor.BLUE, 1);
        int wingsIndex = -1;
        List<Card> hand = gd.playerHands.get(player1.getId());
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i) instanceof ShimmeringWings) {
                wingsIndex = i;
                break;
            }
        }
        harness.castEnchantment(player1, wingsIndex, knightPerm.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof ShimmeringWings
                        && p.isAttached()
                        && p.getAttachedTo().equals(knightPerm.getId()));
        assertThat(gqs.hasKeyword(gd, knightPerm, Keyword.FLYING)).isTrue();
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Shimmering Wings fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent knightPerm = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new ShimmeringWings()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, knightPerm.getId());

        // Remove the target before resolution
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shimmering Wings");
        harness.assertNotOnBattlefield(player1, "Shimmering Wings");
    }

    // ===== Orphaned aura =====

    @Test
    @DisplayName("Shimmering Wings goes to graveyard when enchanted creature dies")
    void goesToGraveyardWhenCreatureDies() {
        Permanent knightPerm = addCreatureReady(player2, new GrizzlyBears());

        Permanent wingsPerm = harness.addToBattlefieldAndReturn(player2, new ShimmeringWings());
        wingsPerm.setAttachedTo(knightPerm.getId());

        addCreatureReady(player1, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Shimmering Wings");
        harness.assertInGraveyard(player2, "Shimmering Wings");
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Cannot enchant a land")
    void cannotEnchantALand() {
        // A creature must exist so the spell is playable; targeting the land is then rejected.
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new ShimmeringWings()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        Permanent mountain = findPermanent(player1, "Mountain");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Returning Shimmering Wings requires blue mana")
    void returnAbilityRequiresBlueMana() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent wings = harness.addToBattlefieldAndReturn(player1, new ShimmeringWings());
        wings.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Shimmering Wings");
        harness.assertNotInHand(player1, "Shimmering Wings");
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Multiple return activations return the Aura only once")
    void multipleActivationsReturnAuraOnlyOnce() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent wings = harness.addToBattlefieldAndReturn(player1, new ShimmeringWings());
        wings.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 1, null, null);
        harness.activateAbility(player1, 1, null, null);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card instanceof ShimmeringWings).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Shimmering Wings");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }
}

