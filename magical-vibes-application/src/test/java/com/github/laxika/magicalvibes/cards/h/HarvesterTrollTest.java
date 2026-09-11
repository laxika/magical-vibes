package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({HarvesterTroll.class, GrizzlyBears.class, Forest.class})
class HarvesterTrollTest extends BaseCardTest {

    @Test
    @DisplayName("ETB sacrifice of a creature puts two +1/+1 counters on Harvester Troll")
    void sacrificesCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castHarvesterTroll();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        Permanent troll = findPermanent(player1, "Harvester Troll");
        assertThat(troll.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB sacrifice of a land puts two +1/+1 counters on Harvester Troll")
    void sacrificesLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        castHarvesterTroll();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        Permanent troll = findPermanent(player1, "Harvester Troll");
        assertThat(troll.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Declining the ETB sacrifice does nothing")
    void declinesSacrifice() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castHarvesterTroll();

        harness.handleMayAbilityChosen(player1, false);

        Permanent troll = findPermanent(player1, "Harvester Troll");
        assertThat(troll.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }

    private void castHarvesterTroll() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new HarvesterTroll()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
