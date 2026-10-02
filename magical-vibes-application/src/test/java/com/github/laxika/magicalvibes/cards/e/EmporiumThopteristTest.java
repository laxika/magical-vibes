package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmporiumThopterist.class, Ornithopter.class, GrizzlyBears.class})
class EmporiumThopteristTest extends BaseCardTest {

    @Test
    void boostsOnlyThoptersYouControl() {
        harness.addToBattlefield(player1, new EmporiumThopterist());
        Permanent ownThopter = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentThopter = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        assertThat(gqs.getEffectivePower(gd, ownThopter)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentThopter)).isZero();
    }

    @Test
    void conjuresAnOrnithopterAtTheBeginningOfYourUpkeep() {
        harness.addToBattlefield(player1, new EmporiumThopterist());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Ornithopter"))
                .hasSize(1);
    }

    @Test
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new EmporiumThopterist());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Ornithopter"))
                .isEmpty();
    }
}
