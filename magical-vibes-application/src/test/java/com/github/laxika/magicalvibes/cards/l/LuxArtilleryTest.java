package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.s.SteelWall;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IronMyr;
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

@CardUsed({LuxArtillery.class, CopperMyr.class, SteelWall.class, IronMyr.class, GrizzlyBears.class})
class LuxArtilleryTest extends BaseCardTest {

    @Test
    @DisplayName("Artifact creature spells gain sunburst")
    void artifactCreatureSpellGainsSunburst() {
        harness.addToBattlefield(player1, new LuxArtillery());
        harness.setHand(player1, List.of(new CopperMyr()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent myr = findPermanent(player1, "Copper Myr");
        assertThat(myr.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Deals 10 damage to each opponent with thirty counters among artifacts and creatures")
    void dealsDamageAtThreshold() {
        harness.addToBattlefield(player1, new LuxArtillery());
        Permanent steelWall = harness.addToBattlefieldAndReturn(player1, new SteelWall());
        steelWall.setCounterCount(CounterType.CHARGE, 30);

        reachEndStep();

        assertThat(gd.getLife(player2.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Does not deal damage below the counter threshold")
    void doesNotDealDamageBelowArtifactCreatureThreshold() {
        harness.addToBattlefield(player1, new LuxArtillery());
        Permanent steelWall = harness.addToBattlefieldAndReturn(player1, new SteelWall());
        steelWall.setCounterCount(CounterType.CHARGE, 29);

        reachEndStep();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Each Lux Artillery supplies its own sunburst counters")
    void multipleArtilleriesAddCountersIndependently() {
        harness.addToBattlefield(player1, new LuxArtillery());
        harness.addToBattlefield(player1, new LuxArtillery());
        harness.setHand(player1, List.of(new CopperMyr()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Copper Myr")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Gives artifact creature spells sunburst")
    void givesArtifactCreatureSpellsSunburst() {
        castLuxArtillery();

        harness.setHand(player1, List.of(new IronMyr()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Iron Myr")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Deals 10 damage to each opponent with 30 qualifying counters")
    void dealsDamageAtThirtyCounters() {
        Permanent artillery = harness.addToBattlefieldAndReturn(player1, new LuxArtillery());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        artillery.setCounterCount(CounterType.CHARGE, 20);
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 10);

        reachEndStep();

        harness.assertLife(player2, 10);
    }

    @Test
    @DisplayName("Does not deal damage below 30 qualifying counters")
    void doesNotDealDamageBelowThreshold() {
        Permanent artillery = harness.addToBattlefieldAndReturn(player1, new LuxArtillery());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        artillery.setCounterCount(CounterType.CHARGE, 20);
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 9);

        reachEndStep();

        harness.assertLife(player2, 20);
    }

    private void castLuxArtillery() {
        harness.setHand(player1, List.of(new LuxArtillery()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void reachEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.withAutoStop(TurnStep.END_STEP, () -> {
            gs.advanceStep(gd);
            resolveAllTriggers();
        });
    }
}
