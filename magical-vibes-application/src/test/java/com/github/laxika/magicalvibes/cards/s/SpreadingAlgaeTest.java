package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.ElvishLyrist;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpreadingAlgae.class, ElvishLyrist.class, Forest.class, Swamp.class})
class SpreadingAlgaeTest extends BaseCardTest {

    // ===== Casting and targeting =====

    @Test
    @DisplayName("Can cast Spreading Algae targeting a Swamp")
    void canTargetSwamp() {
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
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
    @DisplayName("Can cast Spreading Algae targeting an opponent's Swamp")
    void canTargetOpponentsSwamp() {
        Permanent swamp = harness.addToBattlefieldAndReturn(player2, new Swamp());
        harness.setHand(player1, List.of(new SpreadingAlgae()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);

        harness.castEnchantment(player1, 0, swamp.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Spreading Algae").getAttachedTo()).isEqualTo(swamp.getId());
    }

    @Test
    @DisplayName("Cannot cast Spreading Algae targeting a non-Swamp permanent")
    void cannotTargetNonSwamp() {
        harness.addToBattlefield(player1, new Swamp()); // valid target so spell is playable
        harness.addToBattlefield(player1, new Forest());
        Permanent forest = findPermanent(player1, "Forest");
        harness.setHand(player1, List.of(new SpreadingAlgae()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Swamp");
    }

    @Test
    @DisplayName("Resolving Spreading Algae attaches it to the target Swamp")
    void resolvingAttachesToSwamp() {
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
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
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Swamp");
    }

    @Test
    @DisplayName("Tapping an un-enchanted Swamp does not destroy it")
    void tappingUnenchantedSwampDoesNotDestroy() {
        harness.addToBattlefield(player1, new Swamp());

        harness.tapPermanent(player1, 0);
        resolveAllTriggers();

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Spreading Algae"));
        assertThat(gd.pendingManaAbilityTriggers)
                .noneMatch(entry -> entry.getCard().getName().equals("Spreading Algae"));
        harness.assertOnBattlefield(player1, "Swamp");
    }

    // ===== Graveyard-from-battlefield trigger: return to hand =====

    @Test
    @DisplayName("When the enchanted Swamp is destroyed, Spreading Algae returns to its owner's hand")
    void auraReturnsToHandAfterDestroy() {
        addSwampWithAura();

        harness.tapPermanent(player1, 0);
        // Resolve the destroy trigger, then the return-to-hand trigger it spawns.
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Spreading Algae");
        harness.assertNotInGraveyard(player1, "Spreading Algae");
        harness.assertInHand(player1, "Spreading Algae");
    }

    @Test
    @DisplayName("A pending tap trigger still destroys the Swamp if Spreading Algae leaves first")
    void pendingTapTriggerStillDestroysSwampAfterAuraLeaves() {
        Permanent swamp = addSwampWithAura();
        Permanent aura = findPermanent(player1, "Spreading Algae");
        addCreatureReady(player2, new ElvishLyrist());

        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.tapPermanent(player1, 0);
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, aura.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Spreading Algae");
        harness.assertNotOnBattlefield(player2, "Elvish Lyrist");
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(permanent -> permanent.getId().equals(swamp.getId()));
    }

    // ===== Helpers =====

    /**
     * Places a Swamp on player1's battlefield (index 0) with a Spreading Algae attached (index 1).
     *
     * @return the Swamp permanent
     */
    private Permanent addSwampWithAura() {
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SpreadingAlgae());
        aura.setAttachedTo(swamp.getId());

        return swamp;
    }
}
