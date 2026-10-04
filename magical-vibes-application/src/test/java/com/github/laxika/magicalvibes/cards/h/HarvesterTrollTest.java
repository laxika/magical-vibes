package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
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

    @Test
    @DisplayName("Counters are placed during the same resolution as the sacrifice")
    void countersArePlacedWithoutAnotherPriorityRound() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        castHarvesterTroll();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, land.getId());

        Permanent troll = findPermanent(player1, "Harvester Troll");
        assertThat(troll.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Harvester Troll can sacrifice itself without another permanent")
    void canSacrificeItself() {
        castHarvesterTroll();
        Permanent troll = findPermanent(player1, "Harvester Troll");

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, troll.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Harvester Troll");
        harness.assertInGraveyard(player1, "Harvester Troll");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only creatures and lands controlled by the ability controller can be sacrificed")
    void excludesOpponentsPermanents() {
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent opposingLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        castHarvesterTroll();
        Permanent troll = findPermanent(player1, "Harvester Troll");

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .containsExactlyInAnyOrder(ownLand.getId(), troll.getId())
                .doesNotContain(opposingCreature.getId(), opposingLand.getId());
        harness.handlePermanentChosen(player1, ownLand.getId());
        harness.passBothPriorities();

        assertThat(troll.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Forest");
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
