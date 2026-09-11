package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Blaze.class, GrizzlyBears.class, AirElemental.class, Mountain.class})
class BlazeTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting Blaze targeting a player puts it on the stack")
    void castingTargetingPlayerPutsOnStack() {
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, 3, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getXValue()).isEqualTo(3);
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Casting Blaze targeting a creature puts it on the stack")
    void castingTargetingCreaturePutsOnStack() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, 2, targetId);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getXValue()).isEqualTo(2);
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Cannot cast without enough mana for base cost")
    void cannotCastWithoutBaseMana() {
        harness.setHand(player1, List.of(new Blaze()));
        // No mana at all — need at least {R}
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can pay X with any color mana")
    void canPayXWithAnyColor() {
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());

        harness.assertLife(player2, 17);
    }

    // ===== Dealing damage to player =====

    @Test
    @DisplayName("Deals X damage to target player")
    void dealsXDamageToPlayer() {
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, 5, player2.getId());

        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Can target yourself")
    void canTargetSelf() {
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player1, 20);

        harness.castAndResolveSorcery(player1, 0, 3, player1.getId());

        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("X=0 deals no damage")
    void xZeroDealsNoDamage() {
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, 0, player2.getId());

        harness.assertLife(player2, 20);
    }

    @Test
    @CardUsed(ChandraNalaar.class)
    @DisplayName("Deals X damage to target planeswalker")
    void dealsXDamageToPlaneswalker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        target.setCounterCount(CounterType.LOYALTY, 6);
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, 3, target.getId());

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Dealing damage to creature =====

    @Test
    @DisplayName("Deals X damage to target creature, destroying it")
    void dealsXDamageToCreatureDestroysIt() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, 2, targetId);

        // Grizzly Bears (2/2) should be destroyed by 2 damage
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not destroy creature with toughness greater than X")
    void doesNotDestroyCreatureWithHigherToughness() {
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Air Elemental");
        harness.castAndResolveSorcery(player1, 0, 3, targetId);

        // Air Elemental (4/4) should survive 3 damage
        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    // ===== Graveyard and stack cleanup =====

    @Test
    @DisplayName("Blaze goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());

        harness.assertInGraveyard(player1, "Blaze");
    }

    @Test
    @DisplayName("Stack is empty after resolution")
    void stackIsEmptyAfterResolution() {
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());

        assertThat(gd.stack).isEmpty();
    }
}

