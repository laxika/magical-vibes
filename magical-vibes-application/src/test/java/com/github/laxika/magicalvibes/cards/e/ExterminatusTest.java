package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Exterminatus.class, GrizzlyBears.class, DuskImp.class, Forest.class})
class ExterminatusTest extends BaseCardTest {

    @Test
    @DisplayName("Removes opponents' indestructible before destroying all nonland permanents")
    void removesOpponentsIndestructibleBeforeWipe() {
        Permanent ownIndestructible = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        ownIndestructible.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        Permanent ownOrdinary = harness.addToBattlefieldAndReturn(player1, new DuskImp());
        Permanent opponentIndestructible = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opponentIndestructible.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        Permanent opponentOrdinary = harness.addToBattlefieldAndReturn(player2, new DuskImp());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.castFromHand(player1, new Exterminatus(), "{5}{W}{B}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownIndestructible, ownLand)
                .doesNotContain(ownOrdinary);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentLand)
                .doesNotContain(opponentIndestructible, opponentOrdinary);
    }
}
