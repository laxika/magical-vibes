package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BadMoon;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KormusBellTest extends BaseCardTest {

    private Permanent named(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("Swamps of both players become 1/1 creatures that are still lands")
    void animatesSwamps() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player1, new KormusBell());

        Permanent swamp1 = named(player1, "Swamp");
        assertThat(gqs.isCreature(gd, swamp1)).isTrue();
        assertThat(gqs.getEffectivePower(gd, swamp1)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, swamp1)).isEqualTo(1);
        assertThat(swamp1.getCard().hasType(CardType.LAND)).isTrue();

        Permanent swamp2 = named(player2, "Swamp");
        assertThat(gqs.isCreature(gd, swamp2)).isTrue();
        assertThat(gqs.getEffectivePower(gd, swamp2)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, swamp2)).isEqualTo(1);
    }

    @Test
    @DisplayName("Non-Swamp lands are unaffected")
    void doesNotAnimateNonSwampLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new KormusBell());

        Permanent forest = named(player1, "Forest");
        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(0);
    }

    @Test
    @DisplayName("Animated Swamps are black creatures, so Bad Moon pumps them to 2/2")
    void animatedSwampsAreBlackCreaturesForBadMoon() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new KormusBell());
        harness.addToBattlefield(player1, new BadMoon());

        Permanent swamp = named(player1, "Swamp");
        // The Swamp is only a creature because of Kormus Bell; being a black creature, Bad Moon's
        // +1/+1 applies: 1/1 (Kormus Bell) + 1/1 (Bad Moon) = 2/2.
        assertThat(gqs.getEffectivePower(gd, swamp)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, swamp)).isEqualTo(2);
    }

    @Test
    @DisplayName("Swamps revert to non-creatures when Kormus Bell leaves")
    void revertsWhenLeaves() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new KormusBell());

        Permanent swamp = named(player1, "Swamp");
        assertThat(gqs.isCreature(gd, swamp)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Kormus Bell"));

        assertThat(gqs.isCreature(gd, swamp)).isFalse();
        assertThat(gqs.getEffectivePower(gd, swamp)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, swamp)).isEqualTo(0);
    }
}
