package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AetherRefinery;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlasterHulk.class, AetherRefinery.class})
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

        harness.assertOnBattlefield(player1, "Blaster Hulk");
    }

    @Test
    void attackingGainsEnergyAndPaidTriggerDealsDividedDamage() {
        Permanent hulk = addCreatureReady(player1, new BlasterHulk());
        Permanent target = addCreatureReady(player2, new BlasterHulk());
        gd.playerEnergyCounters.put(player1.getId(), 8);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(hulk))));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(10);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));
        harness.handleXValueChosen(player1, 8);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Blaster Hulk");
    }

    @Test
    void energyGainedByAttackCanPayForReflexiveTrigger() {
        Permanent hulk = addCreatureReady(player1, new BlasterHulk());
        gd.playerEnergyCounters.put(player1.getId(), 6);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(hulk))));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(8);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gd.energyCountersPaidOrLostThisTurn.get(player1.getId())).isEqualTo(8);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    void decliningPaymentKeepsGainedEnergyAndDoesNotChooseTargets() {
        Permanent hulk = addCreatureReady(player1, new BlasterHulk());
        gd.playerEnergyCounters.put(player1.getId(), 8);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(hulk))));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(10);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.energyCountersPaidOrLostThisTurn.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    void hasteAllowsAttackImmediatelyAndInsufficientEnergyDoesNotRequireTargets() {
        Permanent hulk = harness.addToBattlefieldAndReturn(player1, new BlasterHulk());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(hulk))));
        assertThat(hulk.isAttacking()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void energyLostThisTurnAlsoReducesCost() {
        gd.playerEnergyCounters.put(player1.getId(), 3);
        gd.setPlayerEnergyCounters(player1.getId(), 0);
        harness.setHand(player1, List.of(new BlasterHulk()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Blaster Hulk");
    }

    @Test
    void energyOwnedButNotPaidOrLostDoesNotReduceCost() {
        gd.playerEnergyCounters.put(player1.getId(), 20);
        harness.setHand(player1, List.of(new BlasterHulk()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void largeEnergyLossCannotReduceColoredManaRequirements() {
        gd.playerEnergyCounters.put(player1.getId(), 20);
        gd.setPlayerEnergyCounters(player1.getId(), 0);
        harness.setHand(player1, List.of(new BlasterHulk()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Blaster Hulk");
    }
}