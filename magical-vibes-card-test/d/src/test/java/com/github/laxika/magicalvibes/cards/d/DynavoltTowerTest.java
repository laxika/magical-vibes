package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.ChandraTorchOfDefiance;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DynavoltTower.class, Forest.class, GrizzlyBears.class, Shock.class, Divination.class,
        DhundOperative.class, ChandraTorchOfDefiance.class})
class DynavoltTowerTest extends BaseCardTest {

    @Test
    void gainsEnergyWhenYouCastAnInstantOrSorcery() {
        addReadyTower();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);

        harness.passBothPriorities();

        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.castFromHand(player1, new Divination(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(4);
        harness.passBothPriorities();
    }

    @Test
    void doesNotTriggerForCreatureSpells() {
        addReadyTower();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    void paysEnergyAndTapsToDealThreeDamageToAnyTarget() {
        Permanent tower = addReadyTower();
        gd.playerEnergyCounters.put(player1.getId(), 5);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(tower.isTapped()).isTrue();
        harness.assertLife(player2, 17);
    }

    @Test
    void cannotActivateWithoutFiveEnergyCounters() {
        addReadyTower();
        gd.playerEnergyCounters.put(player1.getId(), 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("five energy counters");
    }

    @Test
    void energyTriggerResolvesBeforeTheSpell() {
        addReadyTower();
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
        harness.passBothPriorities();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    void doesNotTriggerForOpponentsInstant() {
        addReadyTower();
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 18);
        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerEnergyCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    void doesNotTriggerForArtifactSpells() {
        addReadyTower();

        harness.castFromHand(player1, new DynavoltTower(), "{3}");
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    void paysEnergyOnActivationAndOnlyOnce() {
        Permanent tower = addReadyTower();
        gd.playerEnergyCounters.put(player1.getId(), 7);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(tower.isTapped()).isTrue();
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        harness.assertLife(player2, 17);
    }

    @Test
    void dealsLethalDamageToCreature() {
        addReadyTower();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DhundOperative());
        gd.playerEnergyCounters.put(player1.getId(), 5);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Dhund Operative");
        harness.assertInGraveyard(player2, "Dhund Operative");
    }

    @Test
    void dealsDamageToPlaneswalker() {
        addReadyTower();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChandraTorchOfDefiance());
        target.setCounterCount(CounterType.LOYALTY, 4);
        gd.playerEnergyCounters.put(player1.getId(), 5);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Chandra, Torch of Defiance");
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent tower = addReadyTower();
        tower.tap();
        gd.playerEnergyCounters.put(player1.getId(), 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(5);
    }

    private Permanent addReadyTower() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();
        return harness.addToBattlefieldAndReturn(player1, new DynavoltTower());
    }
}
