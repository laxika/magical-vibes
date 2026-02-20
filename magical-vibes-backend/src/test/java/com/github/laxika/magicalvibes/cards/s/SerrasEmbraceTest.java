package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.service.GameQueryService;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SerrasEmbraceTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameService gs;
    private GameQueryService gqs;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gs = harness.getGameService();
        gqs = harness.getGameQueryService();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Serra's Embrace has correct card properties")
    void hasCorrectProperties() {
        SerrasEmbrace card = new SerrasEmbrace();

        assertThat(card.getName()).isEqualTo("Serra's Embrace");
        assertThat(card.getType()).isEqualTo(CardType.ENCHANTMENT);
        assertThat(card.getManaCost()).isEqualTo("{2}{W}{W}");
        assertThat(card.getColor()).isEqualTo(CardColor.WHITE);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.AURA);
        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.isAura()).isTrue();
        assertThat(card.getCardText()).isEqualTo("Enchant creature\nEnchanted creature gets +2/+2 and has flying and vigilance.");
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(3);
        assertThat(card.getEffects(EffectSlot.STATIC).get(0)).isInstanceOf(BoostEnchantedCreatureEffect.class);
        assertThat(card.getEffects(EffectSlot.STATIC).get(1)).isInstanceOf(GrantKeywordToEnchantedCreatureEffect.class);
        assertThat(card.getEffects(EffectSlot.STATIC).get(2)).isInstanceOf(GrantKeywordToEnchantedCreatureEffect.class);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Serra's Embrace puts it on the stack")
    void castingPutsOnStack() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        harness.setHand(player1, List.of(new SerrasEmbrace()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Serra's Embrace");
    }

    @Test
    @DisplayName("Resolving Serra's Embrace attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        harness.setHand(player1, List.of(new SerrasEmbrace()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Serra's Embrace")
                        && p.getAttachedTo() != null
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    // ===== +2/+2 boost =====

    @Test
    @DisplayName("Enchanted creature gets +2/+2")
    void enchantedCreatureGetsBoost() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        Permanent embracePerm = new Permanent(new SerrasEmbrace());
        embracePerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(embracePerm);

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(4);
    }

    // ===== Flying =====

    @Test
    @DisplayName("Enchanted creature has flying")
    void enchantedCreatureHasFlying() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        Permanent embracePerm = new Permanent(new SerrasEmbrace());
        embracePerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(embracePerm);

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FLYING)).isTrue();
    }

    // ===== Vigilance =====

    @Test
    @DisplayName("Enchanted creature has vigilance")
    void enchantedCreatureHasVigilance() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        Permanent embracePerm = new Permanent(new SerrasEmbrace());
        embracePerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(embracePerm);

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.VIGILANCE)).isTrue();
    }

    // ===== Effects stop when removed =====

    @Test
    @DisplayName("Creature loses boost and keywords when Serra's Embrace is removed")
    void effectsStopWhenRemoved() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        Permanent embracePerm = new Permanent(new SerrasEmbrace());
        embracePerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(embracePerm);

        // Verify effects are active
        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.VIGILANCE)).isTrue();

        // Remove Serra's Embrace
        gd.playerBattlefields.get(player1.getId()).remove(embracePerm);

        // Verify effects are gone
        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.VIGILANCE)).isFalse();
    }

    // ===== Does not affect other creatures =====

    @Test
    @DisplayName("Serra's Embrace does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        Permanent otherBears = new Permanent(new GrizzlyBears());
        otherBears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(otherBears);

        Permanent embracePerm = new Permanent(new SerrasEmbrace());
        embracePerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(embracePerm);

        // Other creature should not be affected
        assertThat(gqs.getEffectivePower(gd, otherBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherBears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, otherBears, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherBears, Keyword.VIGILANCE)).isFalse();
    }
}

