package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.b.BarbaryApes;
import com.github.laxika.magicalvibes.cards.t.TundraWolves;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JacquesLeVert.class, BarbaryApes.class, TundraWolves.class})
class JacquesLeVertTest extends BaseCardTest {

    @Test
    void boostsGreenCreaturesYouControl() {
        harness.addToBattlefield(player1, new JacquesLeVert());
        Permanent apes = harness.addToBattlefieldAndReturn(player1, new BarbaryApes());

        assertThat(gqs.getEffectivePower(gd, apes)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, apes)).isEqualTo(4);
    }

    @Test
    void boostsItself() {
        Permanent jacques = harness.addToBattlefieldAndReturn(player1, new JacquesLeVert());

        assertThat(gqs.getEffectivePower(gd, jacques)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, jacques)).isEqualTo(4);
    }

    @Test
    void doesNotBoostNongreenCreaturesYouControl() {
        harness.addToBattlefield(player1, new JacquesLeVert());
        Permanent wolves = harness.addToBattlefieldAndReturn(player1, new TundraWolves());

        assertThat(gqs.getEffectivePower(gd, wolves)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, wolves)).isEqualTo(1);
    }

    @Test
    void doesNotBoostOpponentsGreenCreatures() {
        harness.addToBattlefield(player1, new JacquesLeVert());
        Permanent opponentApes = harness.addToBattlefieldAndReturn(player2, new BarbaryApes());

        assertThat(gqs.getEffectivePower(gd, opponentApes)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentApes)).isEqualTo(2);
    }
}
