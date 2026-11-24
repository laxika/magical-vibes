package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThornmantleStriker.class, ElvishWarrior.class, HillGiant.class})
class ThornmantleStrikerTest extends BaseCardTest {

    @Test
    @DisplayName("The debuff uses the number of Elves at resolution, Thornmantle Striker included")
    void debuffUsesElfCountAtResolutionIncludingThornmantleStriker() {
        harness.addToBattlefield(player1, new ElvishWarrior());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        harness.enterBattlefieldAndReturn(player1, new ThornmantleStriker());
        harness.handleListChoice(player1,
                "Target creature an opponent controls gets -X/-X until end of turn, where X is the number of Elves you control");
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("The counter mode removes exactly as many counters as Elves, one kind asking nothing")
    void counterModeRemovesExactlyElfCountOrAllRemaining() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        target.setCounterCount(CounterType.CHARGE, 3);
        harness.addToBattlefield(player1, new ElvishWarrior());

        harness.enterBattlefieldAndReturn(player1, new ThornmantleStriker());
        harness.handleListChoice(player1,
                "Remove X counters from target permanent, where X is the number of Elves you control");
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The counter mode removes every counter when there are fewer than Elves")
    void counterModeRemovesAllCountersWhenFewerThanElfCount() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        target.setCounterCount(CounterType.CHARGE, 1);
        harness.addToBattlefield(player1, new ElvishWarrior());
        harness.addToBattlefield(player1, new ElvishWarrior());

        harness.enterBattlefieldAndReturn(player1, new ThornmantleStriker());
        harness.handleListChoice(player1,
                "Remove X counters from target permanent, where X is the number of Elves you control");
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The debuff mode rejects the controller's own creature")
    void debuffModeRejectsOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.addToBattlefield(player2, new HillGiant());

        harness.enterBattlefieldAndReturn(player1, new ThornmantleStriker());
        harness.handleListChoice(player1,
                "Target creature an opponent controls gets -X/-X until end of turn, where X is the number of Elves you control");

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
