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
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new ShimmeringWings()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, bearsPerm.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
    }

    @Test
    @DisplayName("Resolving Shimmering Wings attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new ShimmeringWings()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, bearsPerm.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof ShimmeringWings
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    @Test
    @DisplayName("Shimmering Wings can enchant a creature an opponent controls")
    void canEnchantOpponentCreature() {
        Permanent bearsPerm = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new ShimmeringWings()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, bearsPerm.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof ShimmeringWings
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FLYING)).isTrue();
    }

    // ===== Flying =====

    @Test
    @DisplayName("Enchanted creature has flying")
    void enchantedCreatureHasFlying() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent wingsPerm = harness.addToBattlefieldAndReturn(player1, new ShimmeringWings());
        wingsPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Shimmering Wings does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent otherBears = addCreatureReady(player1, new GrizzlyBears());

        Permanent wingsPerm = harness.addToBattlefieldAndReturn(player1, new ShimmeringWings());
        wingsPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.hasKeyword(gd, otherBears, Keyword.FLYING)).isFalse();
    }

    // ===== Activated ability: return to hand =====

    @Test
    @DisplayName("Activating {U} ability returns Shimmering Wings to owner's hand")
    void activateAbilityReturnsToHand() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent wingsPerm = harness.addToBattlefieldAndReturn(player1, new ShimmeringWings());
        wingsPerm.setAttachedTo(bearsPerm.getId());

        harness.addMana(player1, ManaColor.BLUE, 1);

        // Activate the return-to-hand ability (wings is at index 1)
        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card instanceof ShimmeringWings);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof ShimmeringWings);
    }

    @Test
    @DisplayName("Creature loses flying after Shimmering Wings returns to hand")
    void creatureLosesFlyingAfterBounce() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent wingsPerm = harness.addToBattlefieldAndReturn(player1, new ShimmeringWings());
        wingsPerm.setAttachedTo(bearsPerm.getId());

        // Verify flying is granted
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FLYING)).isTrue();

        harness.addMana(player1, ManaColor.BLUE, 1);

        // Activate the return-to-hand ability
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        // Creature no longer has flying
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FLYING)).isFalse();
    }

    // ===== Re-cast after bounce =====

    @Test
    @DisplayName("Shimmering Wings can be re-cast after returning to hand")
    void canRecastAfterBounce() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent wingsPerm = harness.addToBattlefieldAndReturn(player1, new ShimmeringWings());
        wingsPerm.setAttachedTo(bearsPerm.getId());

        harness.addMana(player1, ManaColor.BLUE, 1);

        // Bounce it
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card instanceof ShimmeringWings);

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
        harness.castEnchantment(player1, wingsIndex, bearsPerm.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof ShimmeringWings
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FLYING)).isTrue();
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Shimmering Wings fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new ShimmeringWings()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, bearsPerm.getId());

        // Remove the target before resolution
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof ShimmeringWings);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof ShimmeringWings);
    }

    // ===== Orphaned aura =====

    @Test
    @DisplayName("Shimmering Wings goes to graveyard when enchanted creature dies")
    void goesToGraveyardWhenCreatureDies() {
        Permanent bearsPerm = addCreatureReady(player2, new GrizzlyBears());

        Permanent wingsPerm = harness.addToBattlefieldAndReturn(player2, new ShimmeringWings());
        wingsPerm.setAttachedTo(bearsPerm.getId());

        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof ShimmeringWings);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card instanceof ShimmeringWings);
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

        Permanent mountain = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof Mountain)
                .findFirst()
                .orElseThrow();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}

