package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FungusSliver;
import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
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

@CardUsed({SporesowerThallid.class, FungusSliver.class, AshcoatBear.class})
class SporesowerThallidTest extends BaseCardTest {

    @Test
    @DisplayName("At upkeep, puts a spore counter on each Fungus the controller controls")
    void putsSporeCountersOnControlledFungi() {
        Permanent sporesower = harness.addToBattlefieldAndReturn(player1, new SporesowerThallid());
        Permanent ownFungus = harness.addToBattlefieldAndReturn(player1, new FungusSliver());
        Permanent opponentFungus = harness.addToBattlefieldAndReturn(player2, new FungusSliver());
        Permanent ownNonFungus = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());

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
        assertThat(findPermanents(player1, "Saproling")).singleElement().satisfies(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SAPROLING);
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        });
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

    @Test
    void doesNotTriggerDuringOpponentsUpkeep() {
        Permanent sporesower = harness.addToBattlefieldAndReturn(player1, new SporesowerThallid());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(sporesower.getCounterCount(CounterType.FUNGUS)).isZero();
    }

    @Test
    void multipleSporesowersEachPutCountersOnAllControlledFungi() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SporesowerThallid());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SporesowerThallid());
        Permanent fungus = harness.addToBattlefieldAndReturn(player1, new FungusSliver());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.FUNGUS)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.FUNGUS)).isEqualTo(2);
        assertThat(fungus.getCounterCount(CounterType.FUNGUS)).isEqualTo(2);
    }

    @Test
    void upkeepUsesFungiPresentAtResolutionEvenAfterSourceLeaves() {
        Permanent sporesower = harness.addToBattlefieldAndReturn(player1, new SporesowerThallid());
        advanceToUpkeep(player1);

        gd.playerBattlefields.get(player1.getId()).remove(sporesower);
        gd.playerGraveyards.get(player1.getId()).add(sporesower.getCard());
        Permanent fungus = harness.addToBattlefieldAndReturn(player1, new FungusSliver());
        harness.passBothPriorities();

        assertThat(fungus.getCounterCount(CounterType.FUNGUS)).isEqualTo(1);
    }

    @Test
    void paysCountersImmediatelyAndCanActivateWhileTappedAndSummoningSick() {
        Permanent sporesower = harness.addToBattlefieldAndReturn(player1, new SporesowerThallid());
        sporesower.setCounterCount(CounterType.FUNGUS, 5);
        sporesower.tap();
        sporesower.setSummoningSick(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, battlefieldIndex(sporesower), 0, null, null);

        assertThat(sporesower.getCounterCount(CounterType.FUNGUS)).isEqualTo(2);
        assertThat(countPermanents(player1, "Saproling")).isZero();
        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(sporesower), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(sporesower.getCounterCount(CounterType.FUNGUS)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(sporesower);
        gd.playerGraveyards.get(player1.getId()).add(sporesower.getCard());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Saproling")).isEqualTo(1);
        assertThat(countPermanents(player2, "Saproling")).isZero();
    }

    private void addSporeCounterAtUpkeep() {
        advanceToUpkeep(player1);
        harness.passBothPriorities();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
