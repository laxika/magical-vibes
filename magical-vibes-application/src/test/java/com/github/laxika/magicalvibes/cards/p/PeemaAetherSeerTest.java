package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PeemaAetherSeer.class, ColossalDreadmaw.class, GrizzlyBears.class, Forest.class})
class PeemaAetherSeerTest extends BaseCardTest {

    @Test
    void entersWithEnergyEqualToGreatestPowerAmongCreaturesYouControl() {
        addCreatureReady(player1, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new PeemaAetherSeer()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(6);
    }

    @Test
    void paysThreeEnergyToRequireTargetCreatureToBlock() {
        addCreatureReady(player1, new PeemaAetherSeer());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        gd.playerEnergyCounters.put(player1.getId(), 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(target.isMustBlockThisTurnIfAble()).isTrue();

        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .doesNotThrowAnyException();
    }

    @Test
    void cannotActivateWithoutThreeEnergyCounters() {
        addCreatureReady(player1, new PeemaAetherSeer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("three energy counters");
    }

    @Test
    void abilityCannotTargetNonCreaturePermanent() {
        addCreatureReady(player1, new PeemaAetherSeer());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        gd.playerEnergyCounters.put(player1.getId(), 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void countsItsOwnPowerAndIgnoresOpponentsCreatures() {
        addCreatureReady(player2, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new PeemaAetherSeer()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
    }

    @Test
    void measuresGreatestPowerWhenTheTriggerResolves() {
        Permanent largest = addCreatureReady(player1, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new PeemaAetherSeer()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(largest);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
    }

    @Test
    void gainsNoEnergyIfNoCreaturesRemainWhenTheTriggerResolves() {
        harness.setHand(player1, List.of(new PeemaAetherSeer()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        gd.playerEnergyCounters.put(player1.getId(), 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).clear();
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void energyIsPaidOnActivationAndIsNotRefundedWhenTargetLeaves() {
        addCreatureReady(player1, new PeemaAetherSeer());
        Permanent target = addCreatureReady(player2, new PeemaAetherSeer());
        gd.playerEnergyCounters.put(player1.getId(), 4);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(target.isMustBlockThisTurnIfAble()).isFalse();
        gd.playerBattlefields.get(player2.getId()).remove(target);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canActivateWhileTappedAndSummoningSickAndTargetItself() {
        Permanent seer = harness.addToBattlefieldAndReturn(player1, new PeemaAetherSeer());
        seer.setSummoningSick(true);
        seer.setTapped(true);
        gd.playerEnergyCounters.put(player1.getId(), 3);

        harness.activateAbility(player1, 0, null, seer.getId());
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(seer.isMustBlockThisTurnIfAble()).isTrue();
        assertThat(seer.isTapped()).isTrue();
    }

    @Test
    void tappedTargetIsNotRequiredToBlockAndIsNotUntapped() {
        addCreatureReady(player1, new PeemaAetherSeer());
        Permanent attacker = addCreatureReady(player1, new PeemaAetherSeer());
        Permanent target = addCreatureReady(player2, new PeemaAetherSeer());
        target.setTapped(true);
        gd.playerEnergyCounters.put(player1.getId(), 3);
        harness.activateAbility(player1, 0, null, target.getId());
        resolveAllTriggers();

        assertThat(target.isTapped()).isTrue();
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of()))
                .doesNotThrowAnyException();
    }
}
