package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
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

class BlightTest extends BaseCardTest {

    // ===== Casting and targeting =====

    @Test
    @DisplayName("Can cast Blight targeting a land")
    void canTargetLand() {
        harness.addToBattlefield(player1, new Forest());
        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new Blight()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player1);

        harness.castEnchantment(player1, 0, land.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(land.getId());
    }

    @Test
    @DisplayName("Cannot cast Blight targeting a non-land permanent")
    void cannotTargetNonLand() {
        harness.addToBattlefield(player1, new Forest()); // valid target so spell is playable
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent creature = findPermanent(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new Blight()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("Resolving Blight attaches it to the target land")
    void resolvingAttachesToLand() {
        harness.addToBattlefield(player1, new Forest());
        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new Blight()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player1);

        harness.castEnchantment(player1, 0, land.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Blight")
                        && land.getId().equals(p.getAttachedTo()));
    }

    // ===== Tap trigger: destroy the enchanted land =====

    @Test
    @DisplayName("Tapping the enchanted land triggers the destroy ability (deferred as a mana-ability trigger)")
    void tappingLandTriggersDestroy() {
        addLandWithAura();

        // Tapping a land for mana defers its triggers (CR 603.3) until a player next gets priority.
        harness.tapPermanent(player1, 0);

        assertThat(gd.pendingManaAbilityTriggers).anySatisfy(entry -> {
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getCard().getName()).isEqualTo("Blight");
        });
    }

    @Test
    @DisplayName("Tapping the enchanted land destroys it")
    void tappingLandDestroysIt() {
        addLandWithAura();

        harness.tapPermanent(player1, 0);
        resolveStackFully();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Forest"));
    }

    @Test
    @DisplayName("Tapping an un-enchanted land does not destroy it")
    void tappingUnenchantedLandDoesNotDestroy() {
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 0);
        resolveStackFully();

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Blight"));
        assertThat(gd.pendingManaAbilityTriggers)
                .noneMatch(entry -> entry.getCard().getName().equals("Blight"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Forest"));
    }

    // ===== Helpers =====

    /**
     * Places a Forest on player1's battlefield (index 0) with a Blight attached (index 1).
     *
     * @return the Forest permanent
     */
    private Permanent addLandWithAura() {
        harness.addToBattlefield(player1, new Forest());
        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();

        Blight auraCard = new Blight();
        Permanent aura = new Permanent(auraCard);
        aura.setAttachedTo(land.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        return land;
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
