package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AzureDrake;
import com.github.laxika.magicalvibes.cards.b.BarbaryApes;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GravitySphere.class, AzureDrake.class, BarbaryApes.class})
class GravitySphereTest extends BaseCardTest {

    @Test
    @DisplayName("All creatures lose flying regardless of controller")
    void allCreaturesLoseFlying() {
        Permanent ownDrake = addCreatureReady(player1, new AzureDrake());
        Permanent opponentDrake = addCreatureReady(player2, new AzureDrake());
        resolveGravitySphere();

        assertThat(gqs.hasKeyword(gd, ownDrake, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentDrake, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Creatures without flying are unaffected")
    void nonFlyingCreaturesAreUnaffected() {
        Permanent ape = addCreatureReady(player2, new BarbaryApes());
        resolveGravitySphere();

        assertThat(gqs.hasKeyword(gd, ape, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Creatures entering after Gravity Sphere resolves also lose flying")
    void laterCreaturesAlsoLoseFlying() {
        resolveGravitySphere();

        Permanent drake = addCreatureReady(player2, new AzureDrake());

        assertThat(gqs.hasKeyword(gd, drake, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Creatures regain flying when Gravity Sphere leaves the battlefield")
    void effectEndsWhenGravitySphereLeaves() {
        Permanent drake = addCreatureReady(player2, new AzureDrake());
        Permanent gravitySphere = resolveGravitySphere();

        assertThat(gqs.hasKeyword(gd, drake, Keyword.FLYING)).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(gravitySphere);

        assertThat(gqs.hasKeyword(gd, drake, Keyword.FLYING)).isTrue();
    }

    private Permanent resolveGravitySphere() {
        harness.castFromHand(player1, new GravitySphere(), "{2}{R}");
        harness.passBothPriorities();

        return findPermanent(player1, "Gravity Sphere");
    }
}
