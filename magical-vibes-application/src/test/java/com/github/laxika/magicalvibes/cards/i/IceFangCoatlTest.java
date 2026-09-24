package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredPlains;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IceFangCoatl.class, SnowCoveredForest.class, GrizzlyBears.class, SnowCoveredPlains.class})
class IceFangCoatlTest extends BaseCardTest {

    @Test
    @DisplayName("ETB draws a card")
    void etbDrawsACard() {
        harness.setLibrary(player1, List.of(new SnowCoveredPlains()));
        harness.setHand(player1, List.of(new IceFangCoatl()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        int handAfterCast = gd.playerHands.get(player1.getId()).size();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handAfterCast + 1);
    }

    @Test
    @DisplayName("Has deathtouch with at least three other snow permanents")
    void hasDeathtouchWithThreeOtherSnowPermanents() {
        Permanent coatl = addCoatl();
        addSnowPermanents(3);

        assertThat(gqs.hasKeyword(gd, coatl, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Does not count itself or nonsnow permanents toward deathtouch")
    void doesNotCountItselfOrNonsnowPermanents() {
        Permanent coatl = addCoatl();
        addSnowPermanents(2);
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, coatl, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Opponent's snow permanents do not enable deathtouch")
    void opponentSnowPermanentsDoNotCount() {
        Permanent coatl = addCoatl();
        addSnowPermanents(player2, 3);

        assertThat(gqs.hasKeyword(gd, coatl, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Deathtouch is removed when the snow permanent threshold is no longer met")
    void deathtouchTracksSnowPermanents() {
        Permanent coatl = addCoatl();
        addSnowPermanents(3);

        assertThat(gqs.hasKeyword(gd, coatl, Keyword.DEATHTOUCH)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(1);

        assertThat(gqs.hasKeyword(gd, coatl, Keyword.DEATHTOUCH)).isFalse();
    }

    private Permanent addCoatl() {
        return addCreatureReady(player1, new IceFangCoatl());
    }

    private void addSnowPermanents(int count) {
        addSnowPermanents(player1, count);
    }

    private void addSnowPermanents(com.github.laxika.magicalvibes.model.Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new SnowCoveredPlains());
        }
    }
}
