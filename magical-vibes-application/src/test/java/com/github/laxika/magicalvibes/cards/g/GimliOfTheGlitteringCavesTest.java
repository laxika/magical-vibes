package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.t.TymaretTheMurderKing;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GimliOfTheGlitteringCaves.class, TymaretTheMurderKing.class, GrizzlyBears.class})
class GimliOfTheGlitteringCavesTest extends BaseCardTest {

    @Test
    void putsCounterOnGimliForAnotherLegendaryCreatureYouControl() {
        Permanent gimli = harness.addToBattlefieldAndReturn(player1, new GimliOfTheGlitteringCaves());

        harness.enterBattlefieldAndReturn(player1, new TymaretTheMurderKing());
        harness.passBothPriorities();

        assertThat(gimli.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void ignoresNonlegendaryAndOpponentCreatureEntries() {
        Permanent gimli = harness.addToBattlefieldAndReturn(player1, new GimliOfTheGlitteringCaves());

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();
        harness.enterBattlefieldAndReturn(player2, new TymaretTheMurderKing());
        harness.passBothPriorities();

        assertThat(gimli.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void createsTreasureWhenDealingCombatDamage() {
        Permanent gimli = addCreatureReady(player1, new GimliOfTheGlitteringCaves());
        gimli.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }
}
