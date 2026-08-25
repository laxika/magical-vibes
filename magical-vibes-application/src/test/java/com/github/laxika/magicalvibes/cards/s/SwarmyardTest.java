package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RatColony;
import com.github.laxika.magicalvibes.cards.s.SquirrelMob;
import com.github.laxika.magicalvibes.cards.z.ZodiacRat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Swarmyard.class, GiantSpider.class, RatColony.class, SquirrelMob.class, ZodiacRat.class,
        GrizzlyBears.class})
class SwarmyardTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Swarmyard adds colorless mana")
    void tapsForColorlessMana() {
        harness.addToBattlefield(player1, new Swarmyard());

        harness.activateAbility(player1, 0, 0, null, null);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Regenerates a target Spider")
    void regeneratesTargetSpider() {
        harness.addToBattlefield(player1, new Swarmyard());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());

        activateRegeneration(0, spider.getId());

        assertThat(spider.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regenerates target Rats and Squirrels")
    void regeneratesTargetRatAndSquirrel() {
        harness.addToBattlefield(player1, new Swarmyard());
        harness.addToBattlefield(player1, new Swarmyard());
        Permanent rat = harness.addToBattlefieldAndReturn(player2, new RatColony());
        Permanent squirrel = harness.addToBattlefieldAndReturn(player2, new SquirrelMob());

        activateRegeneration(0, rat.getId());
        activateRegeneration(1, squirrel.getId());

        assertThat(rat.getRegenerationShield()).isEqualTo(1);
        assertThat(squirrel.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a creature without one of the protected types")
    void cannotTargetOtherCreature() {
        harness.addToBattlefield(player1, new Swarmyard());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an Insect, Rat, Spider, or Squirrel");
    }

    private void activateRegeneration(int sourceIndex, UUID targetId) {
        harness.activateAbility(player1, sourceIndex, 1, null, targetId);
        harness.passBothPriorities();
    }
}
