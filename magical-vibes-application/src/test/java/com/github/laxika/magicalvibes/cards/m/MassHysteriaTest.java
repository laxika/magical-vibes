package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MassHysteria.class, AlphaMyr.class})
class MassHysteriaTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control have haste")
    void ownCreaturesHaveHaste() {
        Permanent myr = harness.addToBattlefieldAndReturn(player1, new AlphaMyr());
        harness.addToBattlefield(player1, new MassHysteria());

        assertThat(gqs.hasKeyword(gd, myr, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Opponent creatures have haste")
    void opponentCreaturesHaveHaste() {
        Permanent opponentMyr = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());
        harness.addToBattlefield(player1, new MassHysteria());

        assertThat(gqs.hasKeyword(gd, opponentMyr, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Creatures entering after Mass Hysteria has entered have haste")
    void creaturesEnteringAfterSourceHaveHaste() {
        harness.addToBattlefield(player1, new MassHysteria());
        Permanent myr = harness.addToBattlefieldAndReturn(player1, new AlphaMyr());

        assertThat(gqs.hasKeyword(gd, myr, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Haste is removed when Mass Hysteria leaves the battlefield")
    void hasteRemovedWhenSourceLeaves() {
        Permanent myr = harness.addToBattlefieldAndReturn(player1, new AlphaMyr());
        Permanent opponentMyr = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());
        Permanent massHysteria = harness.addToBattlefieldAndReturn(player1, new MassHysteria());
        assertThat(gqs.hasKeyword(gd, myr, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentMyr, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(massHysteria);

        assertThat(gqs.hasKeyword(gd, myr, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentMyr, Keyword.HASTE)).isFalse();
    }
}
