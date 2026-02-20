package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetAndGainLifeEffect;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EssenceDrainTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameService gs;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gs = harness.getGameService();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Essence Drain has correct card properties")
    void hasCorrectProperties() {
        EssenceDrain card = new EssenceDrain();

        assertThat(card.getName()).isEqualTo("Essence Drain");
        assertThat(card.getType()).isEqualTo(CardType.SORCERY);
        assertThat(card.getManaCost()).isEqualTo("{4}{B}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLACK);
        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(DealDamageToAnyTargetAndGainLifeEffect.class);
        DealDamageToAnyTargetAndGainLifeEffect effect = (DealDamageToAnyTargetAndGainLifeEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.damage()).isEqualTo(3);
        assertThat(effect.lifeGain()).isEqualTo(3);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Essence Drain targeting a player puts it on the stack")
    void castingTargetingPlayerPutsOnStack() {
        harness.setHand(player1, List.of(new EssenceDrain()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Essence Drain");
        assertThat(entry.getTargetPermanentId()).isEqualTo(player2.getId());
    }

    // ===== Damage to player and life gain =====

    @Test
    @DisplayName("Essence Drain deals 3 damage to target player and controller gains 3 life")
    void deals3DamageToPlayerAndGains3Life() {
        harness.setHand(player1, List.of(new EssenceDrain()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.setLife(player1, 15);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    // ===== Damage to creature =====

    @Test
    @DisplayName("Essence Drain deals 3 damage to target creature and kills it if toughness <= 3")
    void deals3DamageToCreatureAndKillsIt() {
        Permanent bear = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bear);

        harness.setHand(player1, List.of(new EssenceDrain()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.setLife(player1, 15);

        harness.castSorcery(player1, 0, bear.getId());
        harness.passBothPriorities();

        // 3 damage kills Grizzly Bears (2 toughness)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        // Controller gains 3 life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Essence Drain deals 3 damage to creature with toughness > 3 without killing it")
    void deals3DamageToCreatureWithoutKillingIt() {
        Permanent elemental = new Permanent(new AirElemental());
        gd.playerBattlefields.get(player2.getId()).add(elemental);

        harness.setHand(player1, List.of(new EssenceDrain()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.setLife(player1, 15);

        harness.castSorcery(player1, 0, elemental.getId());
        harness.passBothPriorities();

        // 3 damage does not kill Air Elemental (4/4)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Air Elemental"));
        // Controller still gains 3 life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Essence Drain fizzles when target creature is removed before resolution")
    void fizzlesWhenTargetCreatureRemoved() {
        Permanent bear = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bear);

        harness.setHand(player1, List.of(new EssenceDrain()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.setLife(player1, 15);

        harness.castSorcery(player1, 0, bear.getId());
        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        // Spell fizzles — no life gain
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(15);
    }
}

