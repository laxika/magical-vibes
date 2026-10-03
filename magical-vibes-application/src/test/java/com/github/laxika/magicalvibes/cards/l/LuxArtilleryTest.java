package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.s.SteelWall;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LuxArtillery.class, CopperMyr.class, SteelWall.class})
class LuxArtilleryTest extends BaseCardTest {

    @Test
    @DisplayName("Artifact creature spells gain sunburst")
    void artifactCreatureSpellGainsSunburst() {
        harness.addToBattlefield(player1, new LuxArtillery());
        harness.setHand(player1, List.of(new CopperMyr()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent myr = findPermanent(player1, "Copper Myr");
        assertThat(myr.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Deals 10 damage to each opponent with thirty counters among artifacts and creatures")
    void dealsDamageAtThreshold() {
        harness.addToBattlefield(player1, new LuxArtillery());
        Permanent steelWall = harness.addToBattlefieldAndReturn(player1, new SteelWall());
        steelWall.setCounterCount(CounterType.CHARGE, 30);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Does not deal damage below the counter threshold")
    void doesNotDealDamageBelowThreshold() {
        harness.addToBattlefield(player1, new LuxArtillery());
        Permanent steelWall = harness.addToBattlefieldAndReturn(player1, new SteelWall());
        steelWall.setCounterCount(CounterType.CHARGE, 29);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }
}
