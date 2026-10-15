package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AzureDrake;
import com.github.laxika.magicalvibes.cards.b.BarbaryApes;
import com.github.laxika.magicalvibes.cards.l.Levitation;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GravitySphere.class, AzureDrake.class, BarbaryApes.class, Levitation.class, Opalescence.class})
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

    @Test
    @DisplayName("A later flying grant applies after Gravity Sphere")
    void laterFlyingGrantRestoresFlying() {
        Permanent drake = addCreatureReady(player1, new AzureDrake());
        resolveGravitySphere();

        harness.castFromHand(player1, new Levitation(), "{2}{U}{U}");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, drake, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Gravity Sphere removes an earlier flying grant")
    void earlierFlyingGrantIsRemoved() {
        Permanent ape = addCreatureReady(player1, new BarbaryApes());
        harness.castFromHand(player1, new Levitation(), "{2}{U}{U}");
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, ape, Keyword.FLYING)).isTrue();

        resolveGravitySphere();

        assertThat(gqs.hasKeyword(gd, ape, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Gravity Sphere loses flying itself when it becomes a creature")
    void animatedGravitySphereAlsoLosesFlying() {
        harness.castFromHand(player1, new Opalescence(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.castFromHand(player1, new Levitation(), "{2}{U}{U}");
        harness.passBothPriorities();

        Permanent sphere = resolveGravitySphere();

        assertThat(gqs.isCreature(gd, sphere)).isTrue();
        assertThat(gqs.hasKeyword(gd, sphere, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("A newer Gravity Sphere replaces the older world enchantment")
    void newerWorldReplacesOlderWorld() {
        Permanent older = resolveGravitySphere();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new GravitySphere(), "{2}{R}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(older);
        harness.assertInGraveyard(player1, "Gravity Sphere");
        harness.assertOnBattlefield(player2, "Gravity Sphere");
    }

    private Permanent resolveGravitySphere() {
        harness.castFromHand(player1, new GravitySphere(), "{2}{R}");
        harness.passBothPriorities();

        return findPermanent(player1, "Gravity Sphere");
    }
}
