package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RatColony;
import com.github.laxika.magicalvibes.cards.u.UnyaroBees;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Swarmyard.class, GiantSpider.class, RatColony.class, SquirrelMob.class, GrizzlyBears.class,
        UnyaroBees.class, StranglingSoot.class})
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

    @Test
    @DisplayName("Regeneration creates a shield without tapping the target or adding mana")
    void regeneratesTargetInsect() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Swarmyard());
        Permanent bees = harness.addToBattlefieldAndReturn(player1, new UnyaroBees());

        harness.activateAbility(player1, 0, 1, null, bees.getId());

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(bees.getRegenerationShield()).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.passBothPriorities();

        assertThat(bees.getRegenerationShield()).isEqualTo(1);
        assertThat(bees.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A regeneration shield prevents only the next destruction")
    void regenerationPreventsOneDestruction() {
        harness.addToBattlefield(player1, new Swarmyard());
        Permanent bees = harness.addToBattlefieldAndReturn(player2, new UnyaroBees());
        activateRegeneration(0, bees.getId());

        harness.setHand(player1, List.of(new StranglingSoot(), new StranglingSoot()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castInstant(player1, 0, bees.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Unyaro Bees");
        harness.assertNotInGraveyard(player2, "Unyaro Bees");
        assertThat(bees.isTapped()).isTrue();
        assertThat(bees.getRegenerationShield()).isZero();

        harness.castInstant(player1, 0, bees.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Unyaro Bees");
        harness.assertInGraveyard(player2, "Unyaro Bees");
    }

    @Test
    @DisplayName("Regeneration does not protect an Insect from its sacrifice cost")
    void regenerationDoesNotPreventSacrifice() {
        harness.addToBattlefield(player1, new Swarmyard());
        Permanent bees = harness.addToBattlefieldAndReturn(player1, new UnyaroBees());
        activateRegeneration(0, bees.getId());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 1, 1, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Unyaro Bees");
        harness.assertInGraveyard(player1, "Unyaro Bees");
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A target leaving the battlefield in response receives no shield")
    void targetLeavingBattlefieldMakesRegenerationFizzle() {
        harness.addToBattlefield(player1, new Swarmyard());
        Permanent bees = harness.addToBattlefieldAndReturn(player2, new UnyaroBees());
        harness.activateAbility(player1, 0, 1, null, bees.getId());
        harness.setHand(player2, List.of(new StranglingSoot()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0, bees.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Unyaro Bees");
        assertThat(bees.getRegenerationShield()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void activateRegeneration(int sourceIndex, UUID targetId) {
        harness.activateAbility(player1, sourceIndex, 1, null, targetId);
        harness.passBothPriorities();
    }
}
