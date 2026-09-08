package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FungusSliver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SporesowerThallid.class, FungusSliver.class, GrizzlyBears.class})
class SporesowerThallidTest extends BaseCardTest {

    @Test
    @DisplayName("At upkeep, puts a spore counter on each Fungus the controller controls")
    void putsSporeCountersOnControlledFungi() {
        Permanent sporesower = harness.addToBattlefieldAndReturn(player1, new SporesowerThallid());
        Permanent ownFungus = harness.addToBattlefieldAndReturn(player1, new FungusSliver());
        Permanent opponentFungus = harness.addToBattlefieldAndReturn(player2, new FungusSliver());
        Permanent ownNonFungus = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(sporesower.getCounterCount(CounterType.FUNGUS)).isEqualTo(1);
        assertThat(ownFungus.getCounterCount(CounterType.FUNGUS)).isEqualTo(1);
        assertThat(opponentFungus.getCounterCount(CounterType.FUNGUS)).isZero();
        assertThat(ownNonFungus.getCounterCount(CounterType.FUNGUS)).isZero();
    }

    @Test
    @DisplayName("Removing three spore counters creates a Saproling")
    void removesThreeCountersAndCreatesSaproling() {
        Permanent sporesower = harness.addToBattlefieldAndReturn(player1, new SporesowerThallid());
        addSporeCounterAtUpkeep();
        addSporeCounterAtUpkeep();
        addSporeCounterAtUpkeep();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, battlefieldIndex(sporesower), 0, null, null);
        harness.passBothPriorities();

        assertThat(sporesower.getCounterCount(CounterType.FUNGUS)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SAPROLING))
                .filter(permanent -> gqs.getEffectivePower(gd, permanent) == 1)
                .filter(permanent -> gqs.getEffectiveToughness(gd, permanent) == 1)
                .count()).isEqualTo(1);
    }

    @Test
    @DisplayName("The token ability cannot be activated without three spore counters")
    void requiresThreeSporeCounters() {
        Permanent sporesower = harness.addToBattlefieldAndReturn(player1, new SporesowerThallid());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(sporesower), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(sporesower.getCounterCount(CounterType.FUNGUS)).isZero();
    }

    private void addSporeCounterAtUpkeep() {
        advanceToUpkeep(player1);
        harness.passBothPriorities();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
