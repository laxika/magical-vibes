package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AlliedTeamwork.class, GrizzlyBears.class})
class AlliedTeamworkTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a 1/1 white Ally token")
    void createsAllyToken() {
        harness.castFromHand(player1, new AlliedTeamwork(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent ally = findPermanent(player1, "Ally");
        assertThat(ally.getCard().isToken()).isTrue();
        assertThat(ally.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(ally.getCard().getSubtypes()).containsExactly(CardSubtype.ALLY);
        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ally)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gives your creatures +1/+1 but not an opponent's creatures")
    void boostsOwnCreaturesOnly() {
        harness.addToBattlefield(player1, new AlliedTeamwork());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }
}
