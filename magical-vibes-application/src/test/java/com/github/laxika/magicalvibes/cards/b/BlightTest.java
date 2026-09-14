package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.ManaShort;
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

@CardUsed({Blight.class, Forest.class, GrizzlyBears.class, ManaShort.class})
class BlightTest extends BaseCardTest {

    // ===== Casting and targeting =====

    @Test
    @DisplayName("Can cast Blight targeting a land")
    void canTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
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
    @DisplayName("Can cast Blight targeting an opponent's land")
    void canTargetOpponentsLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Blight()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player1);

        harness.castEnchantment(player1, 0, land.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(land.getId());
    }

    @Test
    @DisplayName("Cannot cast Blight targeting a non-land permanent")
    void cannotTargetNonLand() {
        harness.addToBattlefield(player1, new Forest()); // valid target so spell is playable
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
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
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
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
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Tapping an un-enchanted land does not destroy it")
    void tappingUnenchantedLandDoesNotDestroy() {
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 0);
        resolveAllTriggers();

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Blight"));
        assertThat(gd.pendingManaAbilityTriggers)
                .noneMatch(entry -> entry.getCard().getName().equals("Blight"));
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Tapping an opponent's enchanted land with Mana Short destroys only that land")
    void tappingOpponentsLandWithManaShortDestroysOnlyThatLand() {
        Permanent land = addLandWithAura(player2);
        Permanent otherLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new ManaShort()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(land)
                .contains(otherLand);
    }

    @Test
    @DisplayName("Mana from the enchanted land can pay for a spell before Blight resolves")
    void enchantedLandManaCanPayForSpellBeforeTriggerResolves() {
        Permanent enchantedLand = addLandWithAura();
        Permanent otherLand = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.tapPermanent(player1, 0);
        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(otherLand));

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .doesNotContain(enchantedLand)
                .contains(otherLand)
                .anyMatch(permanent -> permanent.getCard() instanceof GrizzlyBears);
    }

    /**
     * Places a Forest on {@code landController}'s battlefield with a Blight controlled by player1
     * attached to it.
     *
     * @return the Forest permanent
     */
    private Permanent addLandWithAura() {
        return addLandWithAura(player1);
    }

    private Permanent addLandWithAura(Player landController) {
        Permanent land = harness.addToBattlefieldAndReturn(landController, new Forest());

        Blight auraCard = new Blight();
        Permanent aura = new Permanent(auraCard);
        aura.setAttachedTo(land.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        return land;
    }
}
