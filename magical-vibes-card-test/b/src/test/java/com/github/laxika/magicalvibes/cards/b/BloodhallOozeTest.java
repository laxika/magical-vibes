package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.n.NacatlOutlander;
import com.github.laxika.magicalvibes.cards.s.SedraxisAlchemist;
import com.github.laxika.magicalvibes.cards.s.ScarlandThrinax;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodhallOoze.class, SedraxisAlchemist.class, NacatlOutlander.class, ScarlandThrinax.class})
class BloodhallOozeTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter when controlling a black permanent and accepting")
    void counterFromBlackPermanent() {
        Permanent ooze = harness.addToBattlefieldAndReturn(player1, new BloodhallOoze());
        harness.addToBattlefield(player1, new SedraxisAlchemist());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger -> queues may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets a +1/+1 counter when controlling a green permanent and accepting")
    void counterFromGreenPermanent() {
        Permanent ooze = harness.addToBattlefieldAndReturn(player1, new BloodhallOoze());
        harness.addToBattlefield(player1, new NacatlOutlander());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger -> queues may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Both triggers fire when controlling a black and a green permanent")
    void twoCountersFromBothColors() {
        Permanent ooze = harness.addToBattlefieldAndReturn(player1, new BloodhallOoze());
        harness.addToBattlefield(player1, new SedraxisAlchemist());
        harness.addToBattlefield(player1, new NacatlOutlander());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve first trigger -> may prompt
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities(); // resolve second trigger -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("No counter when declining the may")
    void noCounterWhenDeclining() {
        Permanent ooze = harness.addToBattlefieldAndReturn(player1, new BloodhallOoze());
        harness.addToBattlefield(player1, new SedraxisAlchemist());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger -> queues may prompt
        harness.handleMayAbilityChosen(player1, false);

        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void oneMulticoloredPermanentEnablesBothIndependentChoices() {
        Permanent ooze = harness.addToBattlefieldAndReturn(player1, new BloodhallOoze());
        harness.addToBattlefield(player1, new ScarlandThrinax());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void blackConditionIsRecheckedOnResolution() {
        Permanent ooze = harness.addToBattlefieldAndReturn(player1, new BloodhallOoze());
        Permanent black = harness.addToBattlefieldAndReturn(player1, new SedraxisAlchemist());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(black);
        gd.playerGraveyards.get(player1.getId()).add(black.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void greenConditionIsRecheckedOnResolution() {
        Permanent ooze = harness.addToBattlefieldAndReturn(player1, new BloodhallOoze());
        Permanent green = harness.addToBattlefieldAndReturn(player1, new NacatlOutlander());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(green);
        gd.playerGraveyards.get(player1.getId()).add(green.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void opponentsPermanentsDoNotEnableTriggers() {
        Permanent ooze = harness.addToBattlefieldAndReturn(player1, new BloodhallOoze());
        harness.addToBattlefield(player2, new ScarlandThrinax());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotTriggerDuringOpponentsUpkeep() {
        Permanent ooze = harness.addToBattlefieldAndReturn(player1, new BloodhallOoze());
        harness.addToBattlefield(player1, new ScarlandThrinax());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger without a black or green permanent (intervening if)")
    void noTriggerWithoutBlackOrGreen() {
        Permanent ooze = harness.addToBattlefieldAndReturn(player1, new BloodhallOoze());
        // Only the red Ooze itself is controlled — neither black nor green.

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
