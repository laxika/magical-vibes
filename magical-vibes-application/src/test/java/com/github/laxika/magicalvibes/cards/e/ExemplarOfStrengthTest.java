package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExemplarOfStrength.class, AirElemental.class, GrizzlyBears.class})
class ExemplarOfStrengthTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts three -1/-1 counters on a creature you control")
    void etbPutsThreeCountersOnOwnCreature() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());

        harness.setHand(player1, List.of(new ExemplarOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.getGameService().playCard(gd, player1, 0, 0, elemental.getId(), null);
        harness.passBothPriorities(); // resolve creature spell → ETB on stack
        harness.passBothPriorities(); // resolve ETB trigger

        // Air Elemental (4/4) with three -1/-1 counters → 1/1.
        assertThat(elemental.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        assertThat(elemental.getEffectivePower()).isEqualTo(1);
        assertThat(elemental.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB cannot target a creature you don't control")
    void etbCannotTargetOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID opponentCreature = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new ExemplarOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.getGameService().playCard(gd, player1, 0, 0, opponentCreature, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Attacking removes a -1/-1 counter and gains 1 life")
    void attackRemovesCounterAndGainsLife() {
        Permanent exemplar = addCreatureReady(player1, new ExemplarOfStrength());
        exemplar.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 3);

        harness.setLife(player1, 20);
        declareAttackers(List.of(0));
        harness.passBothPriorities(); // resolve attack trigger

        assertThat(exemplar.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("The attack trigger takes the counter off the Exemplar and nothing else")
    void attackTriggerLeavesOtherCreaturesCountersAlone() {
        Permanent exemplar = addCreatureReady(player1, new ExemplarOfStrength());
        exemplar.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 3);

        // A second creature carrying -1/-1 counters: the non-targeting SOURCE form must not reach it.
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        elemental.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);

        declareAttackers(List.of(0));
        harness.passBothPriorities(); // resolve attack trigger

        assertThat(exemplar.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(elemental.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking with no -1/-1 counters gains no life")
    void attackWithNoCountersGainsNoLife() {
        Permanent exemplar = addCreatureReady(player1, new ExemplarOfStrength());

        harness.setLife(player1, 20);
        declareAttackers(List.of(0));
        harness.passBothPriorities(); // resolve attack trigger

        assertThat(exemplar.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(0);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The entering Exemplar can put all three counters on itself")
    void enteringAsOnlyCreatureTargetsItself() {
        Permanent exemplar = harness.enterBattlefieldAndReturn(player1, new ExemplarOfStrength());

        harness.handlePermanentChosen(player1, exemplar.getId());
        harness.passBothPriorities();

        assertThat(exemplar.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, exemplar)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, exemplar)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Removing the last counter before the attack trigger resolves prevents life gain")
    void counterMustStillExistAtResolution() {
        Permanent exemplar = addCreatureReady(player1, new ExemplarOfStrength());
        exemplar.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.setLife(player1, 20);

        declareAttackers(List.of(0));
        assertThat(gd.stack).hasSize(1);
        exemplar.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(exemplar.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An attack trigger cannot remove counters from a source that has left the battlefield")
    void absentSourceDoesNotGainLife() {
        Permanent exemplar = addCreatureReady(player1, new ExemplarOfStrength());
        exemplar.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.setLife(player1, 20);

        declareAttackers(List.of(0));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(exemplar);
        gd.playerGraveyards.get(player1.getId()).add(exemplar.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(exemplar.getCard());
    }

    @Test
    @DisplayName("Removing the final counter gains life for the attacking Exemplar's controller")
    void finalCounterGainsLifeForController() {
        Permanent exemplar = addCreatureReady(player2, new ExemplarOfStrength());
        exemplar.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 10);

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        assertThat(exemplar.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(11);
    }
}
