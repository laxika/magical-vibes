package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeadlockTrapTest extends BaseCardTest {

    @Test
    void entersTappedAndGivesTwoEnergyCounters() {
        harness.setHand(player1, List.of(new DeadlockTrap()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent trap = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(trap.isTapped()).isTrue();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void tapsCreatureAndLocksItsActivatedAbilitiesUntilEndOfTurn() {
        addReadyTrap();
        Permanent elves = addCreatureReady(player1, new LlanowarElves());
        gd.playerEnergyCounters.put(player1.getId(), 1);

        harness.activateAbility(player1, 0, null, elves.getId());
        harness.passBothPriorities();

        assertThat(elves.isTapped()).isTrue();
        elves.untap();
        assertThatThrownBy(() -> harness.tapPermanent(player1, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    void canTargetPlaneswalker() {
        addReadyTrap();
        Permanent planeswalker = new Permanent(new GarrukWildspeaker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        planeswalker.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(planeswalker);
        gd.playerEnergyCounters.put(player1.getId(), 1);

        harness.activateAbility(player1, 0, null, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.isTapped()).isTrue();
    }

    @Test
    void cannotTargetLand() {
        addReadyTrap();
        Permanent land = new Permanent(new Plains());
        gd.playerBattlefields.get(player1.getId()).add(land);
        gd.playerEnergyCounters.put(player1.getId(), 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or planeswalker");
    }

    @Test
    void lockWearsOffAtEndOfTurn() {
        addReadyTrap();
        Permanent elves = addCreatureReady(player1, new LlanowarElves());
        gd.playerEnergyCounters.put(player1.getId(), 1);

        harness.activateAbility(player1, 0, null, elves.getId());
        harness.passBothPriorities();
        gd.expireEndOfTurnFloatingEffects();
        elves.untap();

        assertThatCode(() -> harness.tapPermanent(player1, 1)).doesNotThrowAnyException();
    }

    private Permanent addReadyTrap() {
        Permanent trap = new Permanent(new DeadlockTrap());
        trap.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(trap);
        return trap;
    }
}
