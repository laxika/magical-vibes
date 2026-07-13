package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NaturesRevoltTest extends BaseCardTest {

    private Permanent named(com.github.laxika.magicalvibes.model.Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("Lands of both players become 2/2 creatures that are still lands")
    void animatesAllLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player1, new NaturesRevolt());

        Permanent forest = named(player1, "Forest");
        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(2);
        assertThat(forest.getCard().hasType(CardType.LAND)).isTrue();

        Permanent mountain = named(player2, "Mountain");
        assertThat(gqs.isCreature(gd, mountain)).isTrue();
        assertThat(gqs.getEffectivePower(gd, mountain)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mountain)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not animate non-land permanents or change existing creatures")
    void doesNotAnimateNonLands() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new NaturesRevolt());

        Permanent bears = named(player1, "Grizzly Bears");
        assertThat(gqs.isCreature(gd, bears)).isTrue();
        // Grizzly Bears is a natural 2/2 — Nature's Revolt does not touch its P/T.
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Animated lands benefit from a creature anthem")
    void animatedLandsBenefitFromAnthem() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new NaturesRevolt());
        harness.addToBattlefield(player1, new GloriousAnthem());

        Permanent forest = named(player1, "Forest");
        // 2/2 from Nature's Revolt + 1/1 from Glorious Anthem = 3/3.
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(3);
    }

    @Test
    @DisplayName("Lands revert to non-creatures when Nature's Revolt leaves")
    void revertsWhenLeaves() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new NaturesRevolt());

        Permanent forest = named(player1, "Forest");
        assertThat(gqs.isCreature(gd, forest)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Nature's Revolt"));

        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(0);
    }
}
