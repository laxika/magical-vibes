package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesXEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FlowstoneSlideTest {

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
    @DisplayName("Flowstone Slide has correct card properties")
    void hasCorrectProperties() {
        FlowstoneSlide card = new FlowstoneSlide();

        assertThat(card.getName()).isEqualTo("Flowstone Slide");
        assertThat(card.getType()).isEqualTo(CardType.SORCERY);
        assertThat(card.getManaCost()).isEqualTo("{X}{2}{R}{R}");
        assertThat(card.getColor()).isEqualTo(CardColor.RED);
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(BoostAllCreaturesXEffect.class);
        BoostAllCreaturesXEffect effect = (BoostAllCreaturesXEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.powerMultiplier()).isEqualTo(1);
        assertThat(effect.toughnessMultiplier()).isEqualTo(-1);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Flowstone Slide puts it on the stack with correct X value")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new FlowstoneSlide()));
        harness.addMana(player1, ManaColor.RED, 7); // X=3: {3}{2}{R}{R} = 7 total

        harness.castSorcery(player1, 0, 3);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Flowstone Slide");
        assertThat(entry.getXValue()).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Mana is fully consumed when casting")
    void manaIsConsumedWhenCasting() {
        harness.setHand(player1, List.of(new FlowstoneSlide()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castSorcery(player1, 0, 3);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    // ===== Resolution =====

    @Test
    @DisplayName("All creatures on both sides get +X/-X")
    void allCreaturesGetBoost() {
        // 2/2 on each side
        Permanent bear1 = addCreature(player1, new GrizzlyBears());
        Permanent bear2 = addCreature(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new FlowstoneSlide()));
        harness.addMana(player1, ManaColor.RED, 5); // X=1: {1}{2}{R}{R} = 5

        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();

        // Both bears should be 3/1 (2+1 / 2-1)
        assertThat(bear1.getPowerModifier()).isEqualTo(1);
        assertThat(bear1.getToughnessModifier()).isEqualTo(-1);
        assertThat(bear2.getPowerModifier()).isEqualTo(1);
        assertThat(bear2.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("X=2 kills 2-toughness creatures via state-based actions")
    void killsSmallCreatures() {
        addCreature(player1, new GrizzlyBears());
        addCreature(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new FlowstoneSlide()));
        harness.addMana(player1, ManaColor.RED, 6); // X=2: {2}{2}{R}{R} = 6

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Both 2/2 bears get +2/-2 → 4/0 → die to SBA
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Large X kills small creatures but not large ones")
    void largeXKillsSmallButNotLargeCreatures() {
        addCreature(player1, new GrizzlyBears()); // 2/2
        Permanent bigCreature = addCreature(player2, bigCreature()); // 4/5

        harness.setHand(player1, List.of(new FlowstoneSlide()));
        harness.addMana(player1, ManaColor.RED, 7); // X=3: {3}{2}{R}{R} = 7

        harness.castSorcery(player1, 0, 3);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // 2/2 bear gets +3/-3 → 5/-1 → dies
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        // 4/5 gets +3/-3 → 7/2 → survives
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Big Creature"));
        assertThat(bigCreature.getPowerModifier()).isEqualTo(3);
        assertThat(bigCreature.getToughnessModifier()).isEqualTo(-3);
    }

    @Test
    @DisplayName("X=0 resolves with no effect on creatures")
    void xZeroHasNoEffect() {
        Permanent bear = addCreature(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new FlowstoneSlide()));
        harness.addMana(player1, ManaColor.RED, 4); // X=0: {0}{2}{R}{R} = 4

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Bear is unchanged
        assertThat(bear.getPowerModifier()).isEqualTo(0);
        assertThat(bear.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Resolution logs the boost")
    void resolutionLogsBoost() {
        addCreature(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new FlowstoneSlide()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("Flowstone Slide") && log.contains("creature"));
    }

    // ===== Validation =====

    @Test
    @DisplayName("Cannot cast without enough mana for base cost plus X")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new FlowstoneSlide()));
        harness.addMana(player1, ManaColor.RED, 4); // Only enough for X=0

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 3))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    // ===== Helpers =====

    private Permanent addCreature(Player player, Card card) {
        Permanent perm = new Permanent(card);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private static Card bigCreature() {
        Card card = new Card();
        card.setName("Big Creature");
        card.setType(CardType.CREATURE);
        card.setManaCost("{3}{G}{G}");
        card.setColor(CardColor.GREEN);
        card.setPower(4);
        card.setToughness(5);
        return card;
    }
}

