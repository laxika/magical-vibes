package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JacquesLeVert.class, GrizzlyBears.class, SavannahLions.class})
class JacquesLeVertTest extends BaseCardTest {

    @Test
    void boostsGreenCreaturesYouControl() {
        harness.addToBattlefield(player1, new JacquesLeVert());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    void boostsItself() {
        harness.addToBattlefield(player1, new JacquesLeVert());

        Permanent jacques = findPermanent(player1, "Jacques le Vert");

        assertThat(gqs.getEffectivePower(gd, jacques)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, jacques)).isEqualTo(4);
    }

    @Test
    void doesNotBoostNongreenCreaturesYouControl() {
        harness.addToBattlefield(player1, new JacquesLeVert());
        harness.addToBattlefield(player1, new SavannahLions());

        Permanent lions = findPermanent(player1, "Savannah Lions");

        assertThat(gqs.getEffectivePower(gd, lions)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lions)).isEqualTo(1);
    }

    @Test
    void doesNotBoostOpponentsGreenCreatures() {
        harness.addToBattlefield(player1, new JacquesLeVert());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent opponentBears = findPermanent(player2, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }
}
