package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CityOfShadows;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Squire;
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

@CardUsed({GoblinCaves.class, CityOfShadows.class, GoblinHero.class, Mountain.class,
        Squire.class, StompingGround.class})
class GoblinCavesTest extends BaseCardTest {

    private void attachCaves(Permanent land) {
        Permanent caves = harness.addToBattlefieldAndReturn(player1, new GoblinCaves());
        caves.setAttachedTo(land.getId());
    }

    @Test
    @DisplayName("Goblin creatures get +0/+2 while enchanted land is a basic Mountain")
    void basicMountainBoostsGoblins() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent ownGoblin = harness.addToBattlefieldAndReturn(player1, new GoblinHero());
        Permanent opposingGoblin = harness.addToBattlefieldAndReturn(player2, new GoblinHero());
        Permanent nonGoblin = harness.addToBattlefieldAndReturn(player1, new Squire());
        attachCaves(mountain);

        assertThat(gqs.getEffectivePower(gd, ownGoblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownGoblin)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opposingGoblin)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, nonGoblin)).isEqualTo(2);
    }

    @Test
    @DisplayName("Goblin creatures are boosted when an opponent controls the enchanted basic Mountain")
    void opponentControlledBasicMountainBoostsGoblins() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent ownGoblin = harness.addToBattlefieldAndReturn(player1, new GoblinHero());
        Permanent opposingGoblin = harness.addToBattlefieldAndReturn(player2, new GoblinHero());
        attachCaves(mountain);

        assertThat(gqs.getEffectiveToughness(gd, ownGoblin)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opposingGoblin)).isEqualTo(4);
    }

    @Test
    @DisplayName("Goblin creatures are not boosted while a nonbasic Mountain is enchanted")
    void nonbasicMountainDoesNotBoostGoblins() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new StompingGround());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinHero());
        attachCaves(mountain);

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(2);
    }

    @Test
    @DisplayName("Goblin creatures are not boosted while a non-Mountain is enchanted")
    void nonMountainDoesNotBoostGoblins() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new CityOfShadows());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinHero());
        attachCaves(land);

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(2);
    }

    @Test
    @DisplayName("Goblin Caves cannot target a nonland permanent")
    void cannotTargetNonLand() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Squire());
        harness.setHand(player1, List.of(new GoblinCaves()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }
}
