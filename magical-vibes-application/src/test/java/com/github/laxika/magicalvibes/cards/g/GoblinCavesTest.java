package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.StompingGround;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinCaves.class, Forest.class, GrizzlyBears.class, GoblinEliteInfantry.class,
        Mountain.class, StompingGround.class})
class GoblinCavesTest extends BaseCardTest {

    private void attachCaves(Permanent land) {
        Permanent caves = new Permanent(new GoblinCaves());
        caves.setAttachedTo(land.getId());
        gd.playerBattlefields.get(player1.getId()).add(caves);
    }

    @Test
    @DisplayName("Goblin creatures get +0/+2 while enchanted land is a basic Mountain")
    void basicMountainBoostsGoblins() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.addToBattlefield(player1, new GoblinEliteInfantry());
        harness.addToBattlefield(player2, new GoblinEliteInfantry());
        harness.addToBattlefield(player1, new GrizzlyBears());
        attachCaves(mountain);

        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Goblin Elite Infantry"))).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, findPermanent(player1, "Goblin Elite Infantry"))).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, findPermanent(player2, "Goblin Elite Infantry"))).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, findPermanent(player1, "Grizzly Bears"))).isEqualTo(2);
    }

    @Test
    @DisplayName("Goblin creatures are not boosted while a nonbasic Mountain is enchanted")
    void nonbasicMountainDoesNotBoostGoblins() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new StompingGround());
        harness.addToBattlefield(player1, new GoblinEliteInfantry());
        attachCaves(mountain);

        Permanent goblin = findPermanent(player1, "Goblin Elite Infantry");
        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(2);
    }

    @Test
    @DisplayName("Goblin creatures are not boosted while a non-Mountain is enchanted")
    void nonMountainDoesNotBoostGoblins() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new GoblinEliteInfantry());
        attachCaves(forest);

        Permanent goblin = findPermanent(player1, "Goblin Elite Infantry");
        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(2);
    }

    @Test
    @DisplayName("Goblin Caves cannot target a nonland permanent")
    void cannotTargetNonLand() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GoblinCaves()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }
}
