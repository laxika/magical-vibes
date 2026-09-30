package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AetherRefinery;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlasterHulk.class, AetherRefinery.class, GrizzlyBears.class})
class BlasterHulkTest extends BaseCardTest {

    @Test
    void energyPaidThisTurnReducesGenericCastCost() {
        harness.addToBattlefield(player1, new AetherRefinery());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 1);

        assertThat(gd.energyCountersPaidOrLostThisTurn.get(player1.getId())).isEqualTo(1);

        harness.setHand(player1, List.of(new BlasterHulk()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof BlasterHulk);
    }

    @Test
    void attackingGainsEnergyAndPaidTriggerDealsDividedDamage() {
        Permanent hulk = addCreatureReady(player1, new BlasterHulk());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        gd.playerEnergyCounters.put(player1.getId(), 8);
        gd.pendingETBDamageAssignments = Map.of(target.getId(), 8);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(hulk)));
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
