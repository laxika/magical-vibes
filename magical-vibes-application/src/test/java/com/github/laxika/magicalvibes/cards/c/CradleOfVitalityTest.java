package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WhitesunsPassage;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CradleOfVitalityTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1}{W} puts +1/+1 counters equal to life gained on target creature")
    void payPutsCountersEqualToLifeGained() {
        harness.addToBattlefield(player1, new CradleOfVitality());
        harness.addToBattlefield(player1, new GrizzlyBears());

        // Angel of Mercy: {4}{W} to cast + {1}{W} for Cradle's payment.
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        UUID bearsId = bears.getId();

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve Angel, ETB gain-life trigger onto stack
        harness.passBothPriorities(); // gain 3 life → Cradle trigger → target choice pending

        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities(); // resolve MayPayManaEffect → may prompt

        harness.handleMayAbilityChosen(player1, true); // pay {1}{W}
        harness.passBothPriorities(); // resolve counter placement

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Declining the payment places no counters")
    void declineNoCounters() {
        harness.addToBattlefield(player1, new CradleOfVitality());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false); // decline
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Accepting without enough mana places no counters")
    void cannotPayNoCounters() {
        harness.addToBattlefield(player1, new CradleOfVitality());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5); // only enough for Angel, not the {1}{W}

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true); // accept but cannot pay → treated as decline
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("No trigger when there is no creature to target")
    void noCreatureTargetSkips() {
        harness.addToBattlefield(player1, new CradleOfVitality());

        // Whitesun's Passage is a sorcery that gains 5 life; no creature enters the battlefield.
        harness.setHand(player1, List.of(new WhitesunsPassage()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setLife(player1, 20);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(25);
        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.LifeGainTriggerAnyTarget.class)).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
