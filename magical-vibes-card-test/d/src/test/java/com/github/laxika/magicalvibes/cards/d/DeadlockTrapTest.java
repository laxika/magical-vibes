package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.ChandraTorchOfDefiance;
import com.github.laxika.magicalvibes.cards.a.AetherTheorist;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.ServantOfTheConduit;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeadlockTrap.class, AetherTheorist.class, ChandraTorchOfDefiance.class, Plains.class,
        ServantOfTheConduit.class})
class DeadlockTrapTest extends BaseCardTest {

    @Test
    void entersTappedAndGivesTwoEnergyCounters() {
        harness.setHand(player1, List.of(new DeadlockTrap()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent trap = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(trap.isTapped()).isTrue();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void tapsCreatureAndLocksItsActivatedAbilitiesUntilEndOfTurn() {
        addReadyTrap();
        Permanent theorist = addCreatureReady(player1, new AetherTheorist());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        harness.activateAbility(player1, 0, null, theorist.getId());
        harness.passBothPriorities();

        assertThat(theorist.isTapped()).isTrue();
        theorist.untap();
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    void canTargetPlaneswalker() {
        addReadyTrap();
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new ChandraTorchOfDefiance());
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);
        planeswalker.setSummoningSick(false);
        gd.playerEnergyCounters.put(player1.getId(), 2);

        harness.activateAbility(player1, 0, null, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    void cannotTargetLand() {
        addReadyTrap();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or planeswalker");
    }

    @Test
    void lockWearsOffAtEndOfTurn() {
        addReadyTrap();
        Permanent theorist = addCreatureReady(player1, new AetherTheorist());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        harness.activateAbility(player1, 0, null, theorist.getId());
        harness.passBothPriorities();
        gd.expireEndOfTurnFloatingEffects();
        theorist.untap();

        assertThatCode(() -> harness.activateAbility(player1, 1, null, null)).doesNotThrowAnyException();
    }

    @Test
    void paysEnergyAndTapsSourceBeforeResolution() {
        Permanent trap = addReadyTrap();
        Permanent target = addCreatureReady(player2, new AetherTheorist());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(trap.isTapped()).isTrue();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(target.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void cannotActivateWithoutEnergy() {
        Permanent trap = addReadyTrap();
        Permanent target = addCreatureReady(player2, new AetherTheorist());
        gd.playerEnergyCounters.put(player1.getId(), 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(trap.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileSourceIsTapped() {
        Permanent trap = addReadyTrap();
        trap.tap();
        Permanent target = addCreatureReady(player2, new AetherTheorist());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void locksAlreadyTappedCreatureEvenWhenSourceLeavesBeforeResolution() {
        Permanent trap = addReadyTrap();
        Permanent target = addCreatureReady(player1, new AetherTheorist());
        target.tap();
        gd.playerEnergyCounters.put(player1.getId(), 2);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(trap);
        harness.passBothPriorities();
        target.untap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    void lockAlsoPreventsManaAbilities() {
        addReadyTrap();
        Permanent servant = addCreatureReady(player1, new ServantOfTheConduit());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        harness.activateAbility(player1, 0, null, servant.getId());
        harness.passBothPriorities();
        servant.untap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    void logDoesNotClaimThatTargetCannotAttackOrBlock() {
        addReadyTrap();
        Permanent target = addCreatureReady(player2, new AetherTheorist());
        gd.playerEnergyCounters.put(player1.getId(), 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.gameLog).noneMatch(entry -> entry.plainText().contains("can't attack or block"));
    }

    private Permanent addReadyTrap() {
        return harness.addToBattlefieldAndReturn(player1, new DeadlockTrap());
    }
}
