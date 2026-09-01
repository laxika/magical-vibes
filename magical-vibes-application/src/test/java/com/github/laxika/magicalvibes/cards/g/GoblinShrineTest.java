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

@CardUsed({GoblinShrine.class, GoblinEliteInfantry.class, GrizzlyBears.class,
        Forest.class, Mountain.class, StompingGround.class})
class GoblinShrineTest extends BaseCardTest {

    private Permanent attachShrine(Permanent land) {
        Permanent shrine = harness.addToBattlefieldAndReturn(player1, new GoblinShrine());
        shrine.setAttachedTo(land.getId());
        return shrine;
    }

    @Test
    @DisplayName("Goblin creatures get +1/+0 while enchanted land is a basic Mountain")
    void basicMountainBoostsGoblins() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.addToBattlefield(player1, new GoblinEliteInfantry());
        harness.addToBattlefield(player2, new GoblinEliteInfantry());
        harness.addToBattlefield(player1, new GrizzlyBears());
        attachShrine(mountain);

        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Goblin Elite Infantry"))).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, findPermanent(player2, "Goblin Elite Infantry"))).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, findPermanent(player1, "Goblin Elite Infantry"))).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Grizzly Bears"))).isEqualTo(2);
    }

    @Test
    @DisplayName("Goblin creatures are not boosted while another land is enchanted")
    void nonMountainDoesNotBoostGoblins() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new GoblinEliteInfantry());
        attachShrine(forest);

        Permanent goblin = findPermanent(player1, "Goblin Elite Infantry");
        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(2);
    }

    @Test
    @DisplayName("Goblin creatures are not boosted while a nonbasic Mountain is enchanted")
    void nonbasicMountainDoesNotBoostGoblins() {
        Permanent nonbasicMountain = harness.addToBattlefieldAndReturn(player1, new StompingGround());
        harness.addToBattlefield(player1, new GoblinEliteInfantry());
        attachShrine(nonbasicMountain);

        Permanent goblin = findPermanent(player1, "Goblin Elite Infantry");
        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(2);
    }

    @Test
    @DisplayName("Goblin creatures are boosted when an opponent controls the enchanted basic Mountain")
    void opponentControlledBasicMountainBoostsGoblins() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.addToBattlefield(player1, new GoblinEliteInfantry());
        harness.addToBattlefield(player2, new GoblinEliteInfantry());
        attachShrine(mountain);

        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Goblin Elite Infantry"))).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, findPermanent(player2, "Goblin Elite Infantry"))).isEqualTo(3);
    }

    @Test
    @DisplayName("When Goblin Shrine leaves, it deals 1 damage to each Goblin creature")
    void dealsDamageToEachGoblinWhenLeaving() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent ownGoblin = harness.addToBattlefieldAndReturn(player1, new GoblinEliteInfantry());
        Permanent opposingGoblin = harness.addToBattlefieldAndReturn(player2, new GoblinEliteInfantry());
        Permanent nonGoblin = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent shrine = attachShrine(mountain);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, shrine));
        harness.passBothPriorities();

        assertThat(ownGoblin.getMarkedDamage()).isEqualTo(1);
        assertThat(opposingGoblin.getMarkedDamage()).isEqualTo(1);
        assertThat(nonGoblin.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("When Goblin Shrine is exiled, it deals 1 damage to each Goblin creature")
    void dealsDamageToEachGoblinWhenExiled() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent ownGoblin = harness.addToBattlefieldAndReturn(player1, new GoblinEliteInfantry());
        Permanent opposingGoblin = harness.addToBattlefieldAndReturn(player2, new GoblinEliteInfantry());
        Permanent shrine = attachShrine(mountain);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, shrine));
        harness.passBothPriorities();

        assertThat(ownGoblin.getMarkedDamage()).isEqualTo(1);
        assertThat(opposingGoblin.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Goblin Shrine cannot target a nonland permanent")
    void cannotTargetNonLand() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GoblinShrine()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }
}
