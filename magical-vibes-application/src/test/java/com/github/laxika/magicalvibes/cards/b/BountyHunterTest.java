package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BountyHunterTest extends BaseCardTest {

    @Test
    @DisplayName("First ability puts a bounty counter on a nonblack creature")
    void putsBountyCounter() {
        addCreatureReady(player1, new BountyHunter());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.BOUNTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("First ability cannot target a black creature")
    void cannotTargetBlackCreature() {
        addCreatureReady(player1, new BountyHunter());
        Permanent imp = addCreatureReady(player2, new BogImp());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, imp.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Second ability destroys a creature that has a bounty counter")
    void destroysCreatureWithBountyCounter() {
        addCreatureReady(player1, new BountyHunter());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.BOUNTY, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
        assertThat(countPermanents(player2, "Grizzly Bears")).isZero();
    }

    @Test
    @DisplayName("Second ability cannot target a creature without a bounty counter")
    void cannotDestroyCreatureWithoutBountyCounter() {
        addCreatureReady(player1, new BountyHunter());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Target must be a creature with a bounty counter on it");
    }
}
