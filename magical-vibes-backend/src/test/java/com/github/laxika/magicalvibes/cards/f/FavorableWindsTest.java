package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CloudElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FavorableWindsTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has static boost for own creatures with flying")
    void hasCorrectEffects() {
        FavorableWinds card = new FavorableWinds();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(StaticBoostEffect.class);
        StaticBoostEffect boost = (StaticBoostEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(boost.powerBoost()).isEqualTo(1);
        assertThat(boost.toughnessBoost()).isEqualTo(1);
        assertThat(boost.scope()).isEqualTo(GrantScope.OWN_CREATURES);
        assertThat(boost.filter()).isInstanceOf(PermanentHasKeywordPredicate.class);
        PermanentHasKeywordPredicate filter = (PermanentHasKeywordPredicate) boost.filter();
        assertThat(filter.keyword()).isEqualTo(Keyword.FLYING);
    }

    // ===== Static boost: creatures with flying get +1/+1 =====

    @Test
    @DisplayName("Own creature with flying gets +1/+1")
    void ownFlyingCreatureGetsBoosted() {
        harness.addToBattlefield(player1, new FavorableWinds());
        harness.addToBattlefield(player1, new CloudElemental());

        Permanent elemental = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Cloud Elemental"))
                .findFirst().orElseThrow();

        // Cloud Elemental is 2/3; with +1/+1 boost = 3/4
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not boost creature without flying")
    void doesNotBoostNonFlyingCreature() {
        harness.addToBattlefield(player1, new FavorableWinds());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        // Grizzly Bears is 2/2, no flying, should not be boosted
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost opponent's flying creature")
    void doesNotBoostOpponentFlyingCreature() {
        harness.addToBattlefield(player1, new FavorableWinds());
        harness.addToBattlefield(player2, new CloudElemental());

        Permanent elemental = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Cloud Elemental"))
                .findFirst().orElseThrow();

        // Opponent's Cloud Elemental should remain 2/3
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost is lost when Favorable Winds leaves the battlefield")
    void boostLostWhenEnchantmentRemoved() {
        harness.addToBattlefield(player1, new FavorableWinds());
        harness.addToBattlefield(player1, new CloudElemental());

        Permanent elemental = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Cloud Elemental"))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(3);

        // Remove the enchantment
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Favorable Winds"));

        // Elemental should revert to 2/3
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(3);
    }

    @Test
    @DisplayName("Multiple Favorable Winds stack")
    void multipleWindsStack() {
        harness.addToBattlefield(player1, new FavorableWinds());
        harness.addToBattlefield(player1, new FavorableWinds());
        harness.addToBattlefield(player1, new CloudElemental());

        Permanent elemental = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Cloud Elemental"))
                .findFirst().orElseThrow();

        // Cloud Elemental is 2/3; with two +1/+1 boosts = 4/5
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(5);
    }
}
