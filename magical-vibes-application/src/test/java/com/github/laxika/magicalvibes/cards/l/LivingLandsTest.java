package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.Bayou;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.PrismaticOmen;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LivingLands.class, Forest.class})
class LivingLandsTest extends BaseCardTest {

    private Permanent named(Player player, String name) {
        return findPermanent(player, name);
    }

    @Test
    @CardUsed({Mountain.class, PrismaticOmen.class})
    void animatesLandsThatGainForestSubtype() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new PrismaticOmen());
        harness.addToBattlefield(player1, new LivingLands());

        Permanent mountain = gd.playerBattlefields.get(player1.getId()).get(0);
        assertThat(gqs.effectiveBasicLandTypes(gd, mountain)).contains(CardSubtype.FOREST);
        assertThat(gqs.isCreature(gd, mountain)).isTrue();
        assertThat(gqs.getEffectivePower(gd, mountain)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, mountain)).isEqualTo(1);
        assertThat(gqs.isLand(gd, mountain)).isTrue();
    }

    @Test
    @DisplayName("Forests of both players become 1/1 creatures that are still lands")
    @CardUsed(Bayou.class)
    void animatesForests() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player1, new Bayou());
        harness.addToBattlefield(player1, new LivingLands());

        Permanent forest1 = named(player1, "Forest");
        assertThat(gqs.isCreature(gd, forest1)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest1)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, forest1)).isEqualTo(1);
        assertThat(gqs.isLand(gd, forest1)).isTrue();

        Permanent forest2 = named(player2, "Forest");
        assertThat(gqs.isCreature(gd, forest2)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest2)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, forest2)).isEqualTo(1);
        assertThat(gqs.isLand(gd, forest2)).isTrue();

        Permanent bayou = named(player1, Bayou.class.getSimpleName());
        assertThat(gqs.isCreature(gd, bayou)).isTrue();
        assertThat(gqs.getEffectivePower(gd, bayou)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bayou)).isEqualTo(1);
        assertThat(gqs.isLand(gd, bayou)).isTrue();
    }

    @Test
    @DisplayName("Non-Forest lands are unaffected")
    @CardUsed(Mountain.class)
    void doesNotAnimateNonForestLands() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new LivingLands());

        Permanent mountain = named(player1, "Mountain");
        assertThat(gqs.isCreature(gd, mountain)).isFalse();
        assertThat(gqs.getEffectivePower(gd, mountain)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, mountain)).isEqualTo(0);
    }

    @Test
    @DisplayName("Animated Forests benefit from a creature anthem")
    @CardUsed(GloriousAnthem.class)
    void animatedForestsBenefitFromAnthem() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new LivingLands());
        harness.addToBattlefield(player1, new GloriousAnthem());

        Permanent forest = named(player1, "Forest");
        // 1/1 from Living Lands + 1/1 from Glorious Anthem = 2/2.
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(2);
    }

    @Test
    @DisplayName("Animated Forests stay colorless — Living Lands names no color")
    void animatedForestsStayColorless() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new LivingLands());

        Permanent forest = named(player1, "Forest");
        // "All Forests are 1/1 creatures that are still lands" — no color, and a land has no mana
        // cost to take one from (CR 202.2). Contrast Kormus Bell's "1/1 black creatures".
        assertThat(gqs.getEffectiveColors(gd, forest)).isEmpty();
    }

    @Test
    @DisplayName("Forests revert to non-creatures when Living Lands leaves")
    void revertsWhenLeaves() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new LivingLands());

        Permanent forest = named(player1, "Forest");
        Permanent livingLands = named(player1, "Living Lands");
        assertThat(gqs.isCreature(gd, forest)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, livingLands));

        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(0);
    }
}
