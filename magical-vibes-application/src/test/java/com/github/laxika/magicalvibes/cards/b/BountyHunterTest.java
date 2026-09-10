package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.PitImp;
import com.github.laxika.magicalvibes.cards.t.TrainedArmodon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BountyHunter.class, Mountain.class, PitImp.class, TrainedArmodon.class})
class BountyHunterTest extends BaseCardTest {

    @Test
    @DisplayName("First ability puts a bounty counter on a nonblack creature")
    void putsBountyCounter() {
        addCreatureReady(player1, new BountyHunter());
        Permanent armodon = addCreatureReady(player2, new TrainedArmodon());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, armodon.getId());
        harness.passBothPriorities();

        assertThat(armodon.getCounterCount(CounterType.BOUNTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("First ability can target a creature controlled by its controller")
    void putsBountyCounterOnOwnCreature() {
        addCreatureReady(player1, new BountyHunter());
        Permanent armodon = addCreatureReady(player1, new TrainedArmodon());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, armodon.getId());
        harness.passBothPriorities();

        assertThat(armodon.getCounterCount(CounterType.BOUNTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("First ability cannot target a black creature")
    void cannotTargetBlackCreature() {
        addCreatureReady(player1, new BountyHunter());
        Permanent imp = addCreatureReady(player2, new PitImp());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, imp.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("First ability cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        addCreatureReady(player1, new BountyHunter());
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Target must be a nonblack creature");
    }

    @Test
    @DisplayName("Second ability destroys a creature that has a bounty counter")
    void destroysCreatureWithBountyCounter() {
        addCreatureReady(player1, new BountyHunter());
        Permanent armodon = addCreatureReady(player2, new TrainedArmodon());
        armodon.setCounterCount(CounterType.BOUNTY, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, armodon.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(armodon);
    }

    @Test
    @DisplayName("Second ability can destroy a black creature with a bounty counter")
    void destroysBlackCreatureWithBountyCounter() {
        addCreatureReady(player1, new BountyHunter());
        Permanent imp = addCreatureReady(player2, new PitImp());
        imp.setCounterCount(CounterType.BOUNTY, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, imp.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(imp);
    }

    @Test
    @DisplayName("Second ability cannot target a noncreature permanent with a bounty counter")
    void cannotDestroyNonCreaturePermanentWithBountyCounter() {
        addCreatureReady(player1, new BountyHunter());
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        mountain.setCounterCount(CounterType.BOUNTY, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Target must be a creature with a bounty counter on it");
    }

    @Test
    @DisplayName("Second ability cannot target a creature without a bounty counter")
    void cannotDestroyCreatureWithoutBountyCounter() {
        addCreatureReady(player1, new BountyHunter());
        Permanent armodon = addCreatureReady(player2, new TrainedArmodon());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, armodon.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Target must be a creature with a bounty counter on it");
    }
}
