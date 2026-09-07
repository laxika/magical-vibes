package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LivingPlane.class, Forest.class, Mountain.class, GrizzlyBears.class, GloriousAnthem.class})
class LivingPlaneTest extends BaseCardTest {

    private Permanent named(Player player, String name) {
        return findPermanent(player, name);
    }

    @Test
    @DisplayName("Lands of both players become 1/1 creatures that are still lands")
    void animatesAllLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player1, new LivingPlane());

        Permanent forest = named(player1, "Forest");
        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(1);
        assertThat(forest.getCard().hasType(CardType.LAND)).isTrue();

        Permanent mountain = named(player2, "Mountain");
        assertThat(gqs.isCreature(gd, mountain)).isTrue();
        assertThat(gqs.getEffectivePower(gd, mountain)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, mountain)).isEqualTo(1);
    }

    @Test
    @DisplayName("Non-land permanents are unaffected")
    void doesNotAnimateNonLands() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LivingPlane());

        Permanent bears = named(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Animated lands benefit from a creature anthem")
    void animatedLandsBenefitFromAnthem() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new LivingPlane());
        harness.addToBattlefield(player1, new GloriousAnthem());

        Permanent forest = named(player1, "Forest");
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(2);
    }

    @Test
    @DisplayName("Lands revert to non-creatures when Living Plane leaves")
    void revertsWhenLeaves() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new LivingPlane());

        Permanent forest = named(player1, "Forest");
        assertThat(gqs.isCreature(gd, forest)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Living Plane"));

        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(0);
    }
}
