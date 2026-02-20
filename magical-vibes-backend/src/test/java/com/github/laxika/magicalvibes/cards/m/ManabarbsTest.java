package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FurnaceOfRath;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageOnLandTapEffect;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ManabarbsTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;
    private GameService gs;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        gs = harness.getGameService();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Manabarbs has correct card properties")
    void hasCorrectProperties() {
        Manabarbs card = new Manabarbs();

        assertThat(card.getName()).isEqualTo("Manabarbs");
        assertThat(card.getType()).isEqualTo(CardType.ENCHANTMENT);
        assertThat(card.getManaCost()).isEqualTo("{3}{R}");
        assertThat(card.getColor()).isEqualTo(CardColor.RED);
        assertThat(card.isNeedsTarget()).isFalse();
        assertThat(card.getEffects(EffectSlot.ON_ANY_PLAYER_TAPS_LAND)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ANY_PLAYER_TAPS_LAND).getFirst())
                .isInstanceOf(DealDamageOnLandTapEffect.class);
        DealDamageOnLandTapEffect effect = (DealDamageOnLandTapEffect) card.getEffects(EffectSlot.ON_ANY_PLAYER_TAPS_LAND).getFirst();
        assertThat(effect.damage()).isEqualTo(1);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Manabarbs puts it on the stack as an enchantment spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new Manabarbs()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Manabarbs");
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Resolving Manabarbs puts it on the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new Manabarbs()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Manabarbs"));
    }

    // ===== Trigger: controller taps a land =====

    @Test
    @DisplayName("Controller tapping a land takes 1 damage from Manabarbs")
    void controllerTappingLandTakesDamage() {
        harness.addToBattlefield(player1, new Manabarbs());
        harness.addToBattlefield(player1, new Mountain());
        harness.setLife(player1, 20);

        // Mountain is at index 1 (Manabarbs at index 0)
        gs.tapPermanent(gd, player1, 1);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    // ===== Trigger: opponent taps a land =====

    @Test
    @DisplayName("Opponent tapping a land takes 1 damage from Manabarbs")
    void opponentTappingLandTakesDamage() {
        harness.addToBattlefield(player1, new Manabarbs());
        harness.addToBattlefield(player2, new Forest());
        harness.setLife(player2, 20);

        gs.tapPermanent(gd, player2, 0);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    // ===== Multiple land taps =====

    @Test
    @DisplayName("Tapping multiple lands triggers Manabarbs for each one")
    void tappingMultipleLandsTriggersMultipleTimes() {
        harness.addToBattlefield(player1, new Manabarbs());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.setLife(player1, 20);

        // Tap all three Mountains (indices 1, 2, 3)
        gs.tapPermanent(gd, player1, 1);
        gs.tapPermanent(gd, player1, 2);
        gs.tapPermanent(gd, player1, 3);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    // ===== Two Manabarbs =====

    @Test
    @DisplayName("Two Manabarbs on the battlefield each trigger for 2 total damage per land tap")
    void twoManabarbsStack() {
        harness.addToBattlefield(player1, new Manabarbs());
        harness.addToBattlefield(player1, new Manabarbs());
        harness.addToBattlefield(player2, new Forest());
        harness.setLife(player2, 20);

        gs.tapPermanent(gd, player2, 0);

        // Two Manabarbs each deal 1 damage = 2 total
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    // ===== Manabarbs on different sides =====

    @Test
    @DisplayName("Manabarbs from different players both trigger on the same land tap")
    void manabarbsFromDifferentPlayersBothTrigger() {
        harness.addToBattlefield(player1, new Manabarbs());
        harness.addToBattlefield(player2, new Manabarbs());
        harness.addToBattlefield(player1, new Mountain());
        harness.setLife(player1, 20);

        // Player1 taps Mountain (index 1, after their Manabarbs)
        gs.tapPermanent(gd, player1, 1);

        // Both Manabarbs trigger, dealing 2 total damage to player1
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    // ===== Removing Manabarbs stops the trigger =====

    @Test
    @DisplayName("Removing Manabarbs stops the land-tap damage")
    void removingManabarbsStopsDamage() {
        harness.addToBattlefield(player1, new Manabarbs());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.setLife(player1, 20);

        // Tap first Mountain (index 1) — takes 1 damage
        gs.tapPermanent(gd, player1, 1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);

        // Remove Manabarbs from battlefield
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Manabarbs"));

        // After removing Manabarbs (was index 0), Mountains are now at indices 0 (tapped) and 1 (untapped)
        // Tap second Mountain (index 1) — no damage since Manabarbs is gone
        gs.tapPermanent(gd, player1, 1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    // ===== Interaction with Furnace of Rath =====

    @Test
    @DisplayName("Furnace of Rath doubles Manabarbs damage to 2")
    void furnaceOfRathDoublesManabarbsDamage() {
        harness.addToBattlefield(player1, new Manabarbs());
        harness.addToBattlefield(player1, new FurnaceOfRath());
        harness.addToBattlefield(player2, new Forest());
        harness.setLife(player2, 20);

        gs.tapPermanent(gd, player2, 0);

        // 1 damage doubled to 2 by Furnace of Rath
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    // ===== Manabarbs can kill a player =====

    @Test
    @DisplayName("Manabarbs can reduce a player to 0 life")
    void manabarbsCanKillPlayer() {
        harness.addToBattlefield(player1, new Manabarbs());
        harness.addToBattlefield(player2, new Forest());
        harness.setLife(player2, 1);

        gs.tapPermanent(gd, player2, 0);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(0);
    }

    // ===== Only triggers for lands, not non-land permanents =====

    @Test
    @DisplayName("Tapping a non-land permanent does not trigger Manabarbs")
    void tappingNonLandDoesNotTrigger() {
        // Use MindStone - an artifact with a tap ability to produce mana
        harness.addToBattlefield(player1, new Manabarbs());
        harness.addToBattlefield(player1, new MindStone());
        harness.setLife(player1, 20);

        // Activate MindStone's mana ability (tap: add {1}) — it's an artifact, not a land
        harness.activateAbility(player1, 1, null, null);

        // Should not take damage — MindStone is an artifact, not a land
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }
}

