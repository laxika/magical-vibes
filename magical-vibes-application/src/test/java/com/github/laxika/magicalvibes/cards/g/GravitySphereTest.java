package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GravitySphere.class, AirElemental.class})
class GravitySphereTest extends BaseCardTest {

    @Test
    @DisplayName("All creatures lose flying regardless of controller")
    void allCreaturesLoseFlying() {
        Permanent ownElemental = addCreatureReady(player1, new AirElemental());
        Permanent opponentElemental = addCreatureReady(player2, new AirElemental());
        resolveGravitySphere();

        assertThat(gqs.hasKeyword(gd, ownElemental, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentElemental, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Creatures entering after Gravity Sphere resolves also lose flying")
    void laterCreaturesAlsoLoseFlying() {
        resolveGravitySphere();

        Permanent elemental = addCreatureReady(player2, new AirElemental());

        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Creatures regain flying when Gravity Sphere leaves the battlefield")
    void effectEndsWhenGravitySphereLeaves() {
        Permanent elemental = addCreatureReady(player2, new AirElemental());
        Permanent gravitySphere = resolveGravitySphere();

        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(gravitySphere);

        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isTrue();
    }

    private Permanent resolveGravitySphere() {
        harness.setHand(player1, List.of(new GravitySphere()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        return findPermanent(player1, "Gravity Sphere");
    }
}
