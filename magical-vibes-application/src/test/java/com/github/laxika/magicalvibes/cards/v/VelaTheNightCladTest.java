package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VelaTheNightClad.class, GrizzlyBears.class})
class VelaTheNightCladTest extends BaseCardTest {

    @Test
    @DisplayName("Other creatures you control have intimidate")
    void grantsIntimidateToOtherCreaturesYouControl() {
        harness.addToBattlefield(player1, new VelaTheNightClad());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, ally, Keyword.INTIMIDATE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.INTIMIDATE)).isFalse();
    }

    @Test
    @DisplayName("Each opponent loses 1 life when another creature you control leaves")
    void anotherCreatureLeavingCausesEachOpponentToLoseLife() {
        harness.addToBattlefield(player1, new VelaTheNightClad());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        int opponentLifeBefore = gd.getLife(player2.getId());

        leaveBattlefield(ally);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 1);
    }

    @Test
    @DisplayName("Each opponent loses 1 life when Vela leaves")
    void sourceLeavingCausesEachOpponentToLoseLife() {
        Permanent vela = harness.addToBattlefieldAndReturn(player1, new VelaTheNightClad());
        int opponentLifeBefore = gd.getLife(player2.getId());

        leaveBattlefield(vela);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 1);
    }

    private void leaveBattlefield(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, permanent));
    }
}
