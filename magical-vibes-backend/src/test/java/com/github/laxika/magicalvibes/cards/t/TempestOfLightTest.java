package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DestroyAllEnchantmentsEffect;
import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TempestOfLightTest {

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
    @DisplayName("Tempest of Light has correct card properties")
    void hasCorrectProperties() {
        TempestOfLight card = new TempestOfLight();

        assertThat(card.getName()).isEqualTo("Tempest of Light");
        assertThat(card.getType()).isEqualTo(CardType.INSTANT);
        assertThat(card.getManaCost()).isEqualTo("{2}{W}");
        assertThat(card.getColor()).isEqualTo(CardColor.WHITE);
        assertThat(card.isNeedsTarget()).isFalse();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(DestroyAllEnchantmentsEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new TempestOfLight()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Tempest of Light");
    }

    // ===== Resolution =====

    @Test
    @DisplayName("Destroys a single enchantment")
    void destroysSingleEnchantment() {
        harness.addToBattlefield(player1, new RuleOfLaw());
        harness.setHand(player1, List.of(new TempestOfLight()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Rule of Law"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Rule of Law"));
    }

    @Test
    @DisplayName("Destroys enchantments controlled by both players")
    void destroysEnchantmentsFromBothPlayers() {
        harness.addToBattlefield(player1, new RuleOfLaw());
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.setHand(player1, List.of(new TempestOfLight()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Rule of Law"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Angelic Chorus"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Rule of Law"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Angelic Chorus"));
    }

    @Test
    @DisplayName("Destroys auras attached to creatures")
    void destroysAurasAttachedToCreatures() {
        GrizzlyBears bearsCard = new GrizzlyBears();
        Permanent bears = new Permanent(bearsCard);
        bears.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(bears);

        HolyStrength aura = new HolyStrength();
        Permanent auraPerm = new Permanent(aura);
        auraPerm.setAttachedTo(bears.getId());
        harness.getGameData().playerBattlefields.get(player1.getId()).add(auraPerm);

        harness.setHand(player1, List.of(new TempestOfLight()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Aura is destroyed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Holy Strength"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Holy Strength"));
        // Creature survives
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Does not destroy creatures")
    void doesNotDestroyCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TempestOfLight()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Does nothing when no enchantments on battlefield")
    void doesNothingWhenNoEnchantments() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TempestOfLight()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Tempest of Light goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new TempestOfLight()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Tempest of Light"));
    }

    @Test
    @DisplayName("Resolving logs destroyed enchantments")
    void resolvingLogsDestroyedEnchantments() {
        harness.addToBattlefield(player1, new RuleOfLaw());
        harness.setHand(player1, List.of(new TempestOfLight()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("Rule of Law") && log.contains("destroyed"));
    }
}
