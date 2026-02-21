package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.UnholyStrength;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.BoostSelfPerEnchantmentOnBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameQueryService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class YavimayaEnchantressTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameQueryService gqs;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gqs = harness.getGameQueryService();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Yavimaya Enchantress has correct card properties")
    void hasCorrectProperties() {
        YavimayaEnchantress card = new YavimayaEnchantress();

        assertThat(card.getName()).isEqualTo("Yavimaya Enchantress");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{2}{G}");
        assertThat(card.getColor()).isEqualTo(CardColor.GREEN);
        assertThat(card.getPower()).isEqualTo(2);
        assertThat(card.getToughness()).isEqualTo(2);
        assertThat(card.getSubtypes()).contains(CardSubtype.HUMAN, CardSubtype.DRUID);
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(BoostSelfPerEnchantmentOnBattlefieldEffect.class);
    }

    // ===== Static boost =====

    @Test
    @DisplayName("Base stats are 2/2 with no enchantments on battlefield")
    void baseStatsWithNoEnchantments() {
        harness.addToBattlefield(player1, new YavimayaEnchantress());

        Permanent enchantress = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, enchantress)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enchantress)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gets +1/+1 for own enchantment on battlefield")
    void boostedByOwnEnchantment() {
        harness.addToBattlefield(player1, new YavimayaEnchantress());
        harness.addToBattlefield(player1, new AngelicChorus());

        Permanent enchantress = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Yavimaya Enchantress"))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, enchantress)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enchantress)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gets +1/+1 for opponent's enchantment on battlefield")
    void boostedByOpponentEnchantment() {
        harness.addToBattlefield(player1, new YavimayaEnchantress());
        harness.addToBattlefield(player2, new AngelicChorus());

        Permanent enchantress = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, enchantress)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enchantress)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gets +1/+1 for each enchantment, stacks with multiple")
    void boostedByMultipleEnchantments() {
        harness.addToBattlefield(player1, new YavimayaEnchantress());
        harness.addToBattlefield(player1, new AngelicChorus());
        harness.addToBattlefield(player2, new AngelicChorus());

        Permanent enchantress = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Yavimaya Enchantress"))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, enchantress)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, enchantress)).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost updates when enchantment is removed")
    void boostUpdatesWhenEnchantmentRemoved() {
        harness.addToBattlefield(player1, new YavimayaEnchantress());
        harness.addToBattlefield(player1, new AngelicChorus());

        Permanent enchantress = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Yavimaya Enchantress"))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, enchantress)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enchantress)).isEqualTo(3);

        // Remove the enchantment
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Angelic Chorus"));

        assertThat(gqs.getEffectivePower(gd, enchantress)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enchantress)).isEqualTo(2);
    }

    @Test
    @DisplayName("Non-enchantment permanents do not boost")
    void nonEnchantmentDoesNotBoost() {
        harness.addToBattlefield(player1, new YavimayaEnchantress());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent enchantress = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Yavimaya Enchantress"))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, enchantress)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enchantress)).isEqualTo(2);
    }

    @Test
    @DisplayName("Auras on the battlefield also count as enchantments")
    void aurasCountAsEnchantments() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new YavimayaEnchantress());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        // Attach an aura to Grizzly Bears
        Permanent aura = new Permanent(new UnholyStrength());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        Permanent enchantress = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Yavimaya Enchantress"))
                .findFirst().orElseThrow();
        // Unholy Strength is an enchantment, so +1/+1
        assertThat(gqs.getEffectivePower(gd, enchantress)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enchantress)).isEqualTo(3);
    }
}
