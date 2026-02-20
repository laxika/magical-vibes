package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IncinerateTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Incinerate has correct card properties")
    void hasCorrectProperties() {
        Incinerate card = new Incinerate();

        assertThat(card.getName()).isEqualTo("Incinerate");
        assertThat(card.getType()).isEqualTo(CardType.INSTANT);
        assertThat(card.getManaCost()).isEqualTo("{1}{R}");
        assertThat(card.getColor()).isEqualTo(CardColor.RED);
        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(DealDamageToAnyTargetEffect.class);
        DealDamageToAnyTargetEffect effect = (DealDamageToAnyTargetEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.damage()).isEqualTo(3);
        assertThat(effect.cantRegenerate()).isTrue();
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Incinerate targeting a player puts it on the stack")
    void castingTargetingPlayerPutsItOnStack() {
        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Incinerate");
        assertThat(entry.getTargetPermanentId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Casting Incinerate targeting a creature puts it on the stack")
    void castingTargetingCreaturePutsItOnStack() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Incinerate");
        assertThat(entry.getTargetPermanentId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Dealing damage to player =====

    @Test
    @DisplayName("Deals 3 damage to target player")
    void deals3DamageToPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    // ===== Dealing damage to creature =====

    @Test
    @DisplayName("Deals 3 damage to target creature, destroying a 2/2")
    void deals3DamageToCreatureDestroysIt() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Does not destroy a creature with toughness greater than 3")
    void doesNotDestroyHighToughnessCreature() {
        // Hill Giant is 3/3 — 3 damage equals toughness, so it IS destroyed
        // We need a creature with toughness > 3 to survive
        // Hill Giant 3/3 takes exactly 3, so it is destroyed. Let's boost a creature instead.
        Permanent hillGiant = new Permanent(new HillGiant());
        hillGiant.setSummoningSick(false);
        hillGiant.setToughnessModifier(1); // effectively 3/4
        harness.getGameData().playerBattlefields.get(player2.getId()).add(hillGiant);

        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, hillGiant.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Hill Giant"));
    }

    // ===== Can't be regenerated =====

    @Test
    @DisplayName("Creature dealt lethal damage by Incinerate cannot be regenerated")
    void creatureCannotRegenerateFromIncinerate() {
        // Drudge Skeletons (1/1) with regeneration shield
        DrudgeSkeletons card = new DrudgeSkeletons();
        Permanent skelePerm = new Permanent(card);
        skelePerm.setSummoningSick(false);
        skelePerm.setRegenerationShield(1);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(skelePerm);

        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, skelePerm.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Drudge Skeletons should be destroyed despite regeneration shield
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Drudge Skeletons"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Drudge Skeletons"));
    }

    @Test
    @DisplayName("Creature dealt lethal damage by Incinerate cannot be regenerated even with multiple shields")
    void creatureCannotRegenerateEvenWithMultipleShields() {
        DrudgeSkeletons card = new DrudgeSkeletons();
        Permanent skelePerm = new Permanent(card);
        skelePerm.setSummoningSick(false);
        skelePerm.setRegenerationShield(3);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(skelePerm);

        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, skelePerm.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Drudge Skeletons"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Drudge Skeletons"));
    }

    // ===== Incinerate goes to graveyard =====

    @Test
    @DisplayName("Incinerate goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Incinerate"));
    }
}

