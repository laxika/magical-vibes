package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.BoostEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.service.GameQueryService;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UnholyStrengthTest {

    private GameTestHarness harness;
    private Player player1;
    private GameService gs;
    private GameQueryService gqs;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        gs = harness.getGameService();
        gqs = harness.getGameQueryService();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    @Test
    @DisplayName("Unholy Strength has correct card properties")
    void hasCorrectProperties() {
        UnholyStrength card = new UnholyStrength();

        assertThat(card.getName()).isEqualTo("Unholy Strength");
        assertThat(card.getType()).isEqualTo(CardType.ENCHANTMENT);
        assertThat(card.getManaCost()).isEqualTo("{B}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLACK);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.AURA);
        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.isAura()).isTrue();
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(BoostEnchantedCreatureEffect.class);
    }

    @Test
    @DisplayName("Casting Unholy Strength puts it on the stack")
    void castingPutsOnStack() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.setHand(player1, List.of(new UnholyStrength()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Unholy Strength");
    }

    @Test
    @DisplayName("Resolving Unholy Strength attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.setHand(player1, List.of(new UnholyStrength()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Unholy Strength")
                        && bears.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Enchanted creature gets +2/+1")
    void enchantedCreatureGetsBoost() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent aura = new Permanent(new UnholyStrength());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Creature loses +2/+1 when Unholy Strength is removed")
    void effectsStopWhenRemoved() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent aura = new Permanent(new UnholyStrength());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Unholy Strength fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.setHand(player1, List.of(new UnholyStrength()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        gs.playCard(gd, player1, 0, 0, bears.getId(), null);
        gd.playerBattlefields.get(player1.getId()).remove(bears);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Unholy Strength"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Unholy Strength"));
    }
}
