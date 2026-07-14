package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LivingLandsTest extends BaseCardTest {

    private Permanent named(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("Forests of both players become 1/1 creatures that are still lands")
    void animatesForests() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player1, new LivingLands());

        Permanent forest1 = named(player1, "Forest");
        assertThat(gqs.isCreature(gd, forest1)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest1)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, forest1)).isEqualTo(1);
        assertThat(forest1.getCard().hasType(CardType.LAND)).isTrue();

        Permanent forest2 = named(player2, "Forest");
        assertThat(gqs.isCreature(gd, forest2)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest2)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, forest2)).isEqualTo(1);
    }

    @Test
    @DisplayName("Non-Forest lands are unaffected")
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
    @DisplayName("Forests revert to non-creatures when Living Lands leaves")
    void revertsWhenLeaves() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new LivingLands());

        Permanent forest = named(player1, "Forest");
        assertThat(gqs.isCreature(gd, forest)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Living Lands"));

        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(0);
    }
}
