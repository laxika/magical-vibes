package com.github.laxika.magicalvibes.cards.c;

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
import com.github.laxika.magicalvibes.model.effect.DealXDamageToAnyTargetAndGainXLifeEffect;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConsumeSpiritTest {

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
    @DisplayName("Consume Spirit has correct card properties")
    void hasCorrectProperties() {
        ConsumeSpirit card = new ConsumeSpirit();

        assertThat(card.getName()).isEqualTo("Consume Spirit");
        assertThat(card.getType()).isEqualTo(CardType.SORCERY);
        assertThat(card.getManaCost()).isEqualTo("{X}{1}{B}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLACK);
        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getXColorRestriction()).isEqualTo(ManaColor.BLACK);
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(DealXDamageToAnyTargetAndGainXLifeEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Consume Spirit targeting a player puts it on the stack")
    void castingTargetingPlayerPutsOnStack() {
        harness.setHand(player1, List.of(new ConsumeSpirit()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 3, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Consume Spirit");
        assertThat(entry.getXValue()).isEqualTo(3);
        assertThat(entry.getTargetPermanentId()).isEqualTo(player2.getId());
    }

    // ===== Damage to player and life gain =====

    @Test
    @DisplayName("Consume Spirit deals X damage to target player and gains X life")
    void dealsXDamageToPlayerAndGainsXLife() {
        harness.setHand(player1, List.of(new ConsumeSpirit()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.setLife(player1, 15);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    // ===== Damage to creature =====

    @Test
    @DisplayName("Consume Spirit deals X damage to target creature and gains X life")
    void dealsXDamageToCreatureAndGainsXLife() {
        Permanent bear = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bear);

        harness.setHand(player1, List.of(new ConsumeSpirit()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.setLife(player1, 15);

        harness.castSorcery(player1, 0, 2, bear.getId());
        harness.passBothPriorities();

        // 2 damage kills Grizzly Bears (2 toughness)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        // Controller gains 2 life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    // ===== Mana restriction: spend only black on X =====

    @Test
    @DisplayName("Cannot pay X with non-black mana")
    void cannotPayXWithNonBlackMana() {
        harness.setHand(player1, List.of(new ConsumeSpirit()));
        // 2 black (1 for {B}, 1 for X) + 1 blue (for {1}) = X should be at most 1
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        // X=2 requires 2 black for X + 1 black for {B} + 1 generic = 4 total, 3 black
        // We only have 2 black, so X=2 should fail
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Can pay X with only black mana and generic with any color")
    void canPayXWithBlackAndGenericWithAny() {
        harness.setHand(player1, List.of(new ConsumeSpirit()));
        // 3 black + 1 blue = {B} takes 1 black, {1} takes 1 blue, X can be 2 (from 2 remaining black)
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Can cast with X=0 for minimum cost")
    void canCastWithXZero() {
        harness.setHand(player1, List.of(new ConsumeSpirit()));
        // {X}{1}{B} with X=0 costs just {1}{B}
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        // X=0: 0 damage and 0 life gain
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot cast without enough mana for base cost")
    void cannotCastWithoutBaseMana() {
        harness.setHand(player1, List.of(new ConsumeSpirit()));
        // Only 1 mana, need at least 2 for {1}{B}
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}

