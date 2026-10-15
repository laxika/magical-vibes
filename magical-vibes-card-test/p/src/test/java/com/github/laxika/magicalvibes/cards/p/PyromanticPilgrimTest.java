package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PyromanticPilgrim.class})
class PyromanticPilgrimTest extends BaseCardTest {

    @Test
    void canAttackTheTurnItEnters() {
        PyromanticPilgrim card = new PyromanticPilgrim();
        harness.castFromHand(player1, card, "{2}{R}");
        harness.passBothPriorities();

        Permanent pilgrim = findPermanent(player1, "Pyromantic Pilgrim");
        assertThat(pilgrim.isSummoningSick()).isTrue();

        declareAttackers(List.of(0));

        assertThat(pilgrim.getOriginalCard()).isSameAs(card);
        assertThat(pilgrim.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }
}
