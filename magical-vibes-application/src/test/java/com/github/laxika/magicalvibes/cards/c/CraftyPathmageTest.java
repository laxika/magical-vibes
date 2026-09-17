package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FleetingAven;
import com.github.laxika.magicalvibes.cards.g.Graxiplon;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CraftyPathmage.class, FleetingAven.class, Graxiplon.class, Island.class})
class CraftyPathmageTest extends BaseCardTest {

    @Test
    @DisplayName("Makes an opponent's 2/2 creature unblockable until end of turn")
    void makesOpponentPowerTwoCreatureUnblockableUntilEndOfTurn() {
        Permanent pathmage = addCreatureReady(player1, new CraftyPathmage());
        Permanent target = addCreatureReady(player2, new FleetingAven());

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(pathmage.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("A creature made unblockable cannot be declared as blocked")
    void preventsBlockingAnAffectedCreature() {
        addCreatureReady(player1, new CraftyPathmage());
        addCreatureReady(player1, new FleetingAven());
        Permanent target = addCreatureReady(player2, new FleetingAven());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        target.setAttacking(true);
        prepareDeclareBlockers(player2);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Cannot target a creature with power greater than 2")
    void cannotTargetLargeCreature() {
        Permanent pathmage = addCreatureReady(player1, new CraftyPathmage());
        Permanent target = addCreatureReady(player2, new Graxiplon());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power must be 2 or less");
        assertThat(pathmage.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent pathmage = addCreatureReady(player1, new CraftyPathmage());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, island.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(pathmage.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Fizzling target that grows above power 2 does not become unblockable")
    void targetThatGrowsBeforeResolutionBecomesIllegal() {
        Permanent pathmage = addCreatureReady(player1, new CraftyPathmage());
        Permanent target = addCreatureReady(player2, new FleetingAven());

        harness.activateAbility(player1, 0, null, target.getId());
        TestCards.mutableCard(target).setPower(3);
        harness.passBothPriorities();

        assertThat(pathmage.isTapped()).isTrue();
        assertThat(target.isCantBeBlocked()).isFalse();
    }
}
