package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BirdMaiden;
import com.github.laxika.magicalvibes.cards.s.ScrybSprites;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrapeshotCatapult.class, BirdMaiden.class, GrizzlyBears.class, ScrybSprites.class})
class GrapeshotCatapultTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to a target creature with flying")
    void dealsDamageToFlyingCreature() {
        Permanent catapult = addCreatureReady(player1, new GrapeshotCatapult());

        Permanent sprites = harness.addToBattlefieldAndReturn(player2, new ScrybSprites());

        harness.activateAbility(player1, 0, null, sprites.getId());
        harness.passBothPriorities();

        // 1 damage kills a 1/1 flier
        harness.assertInGraveyard(player2, "Scryb Sprites");
        assertThat(catapult.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can target a flying creature controlled by its controller")
    void canTargetOwnFlyingCreature() {
        Permanent catapult = addCreatureReady(player1, new GrapeshotCatapult());
        Permanent birdMaiden = addCreatureReady(player1, new BirdMaiden());

        harness.activateAbility(player1, 0, null, birdMaiden.getId());
        harness.passBothPriorities();

        assertThat(birdMaiden.getMarkedDamage()).isEqualTo(1);
        assertThat(catapult.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Deals 1 damage without destroying a flying creature with 2 toughness")
    void dealsNonlethalDamageToFlyingCreature() {
        addCreatureReady(player1, new GrapeshotCatapult());

        Permanent birdMaiden = addCreatureReady(player2, new BirdMaiden());

        harness.activateAbility(player1, 0, null, birdMaiden.getId());
        harness.passBothPriorities();

        assertThat(birdMaiden.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Bird Maiden");
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetNonFlyingCreature() {
        addCreatureReady(player1, new GrapeshotCatapult());

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new GrapeshotCatapult());

        Permanent sprites = addCreatureReady(player2, new ScrybSprites());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, sprites.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

    @Test
    @DisplayName("Cannot pay the tap cost while already tapped")
    void cannotActivateWhileTapped() {
        Permanent catapult = addCreatureReady(player1, new GrapeshotCatapult());
        catapult.tap();
        Permanent sprites = harness.addToBattlefieldAndReturn(player2, new ScrybSprites());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, sprites.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(sprites.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        Permanent catapult = addCreatureReady(player1, new GrapeshotCatapult());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(catapult.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The activated ability resolves after its source leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent catapult = addCreatureReady(player1, new GrapeshotCatapult());
        Permanent sprites = harness.addToBattlefieldAndReturn(player2, new ScrybSprites());

        harness.activateAbility(player1, 0, null, sprites.getId());
        gd.playerBattlefields.get(player1.getId()).remove(catapult);
        gd.playerGraveyards.get(player1.getId()).add(catapult.getCard());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Scryb Sprites");
        assertThat(gd.stack).isEmpty();
    }
}
