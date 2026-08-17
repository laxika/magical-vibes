package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EbonPraetorTest extends BaseCardTest {

    @Test
    @DisplayName("Its upkeep trigger puts a -2/-2 counter on it")
    void upkeepPutsMinusTwoMinusTwoCounter() {
        Permanent praetor = addCreatureReady(player1, new EbonPraetor());

        beginUpkeep();

        assertThat(praetor.getCounterCount(CounterType.MINUS_TWO_MINUS_TWO)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrificing a non-Thrull removes a -2/-2 counter without adding a power counter")
    void sacrificingNonThrullRemovesCounter() {
        Permanent praetor = addCreatureReady(player1, new EbonPraetor());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        beginUpkeep();
        activateSacrificeAbility(bears);

        assertThat(praetor.getCounterCount(CounterType.MINUS_TWO_MINUS_TWO)).isZero();
        assertThat(praetor.getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO)).isZero();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sacrificing a Thrull adds a +1/+0 counter")
    void sacrificingThrullAddsPowerCounter() {
        Permanent praetor = addCreatureReady(player1, new EbonPraetor());
        Permanent thrull = addCreatureReady(player1, createThrull());

        beginUpkeep();
        activateSacrificeAbility(thrull);

        assertThat(praetor.getCounterCount(CounterType.MINUS_TWO_MINUS_TWO)).isZero();
        assertThat(praetor.getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Thrull");
    }

    @Test
    @DisplayName("The sacrifice ability can be activated only once each turn and only during upkeep")
    void activationTimingAndFrequencyAreRestricted() {
        addCreatureReady(player1, new EbonPraetor());
        Permanent firstFodder = addCreatureReady(player1, new GrizzlyBears());
        beginUpkeep();
        activateSacrificeAbility(firstFodder);

        addCreatureReady(player1, new GrizzlyBears());
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void activateSacrificeAbility(Permanent sacrificed) {
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, sacrificed.getId());
        harness.passBothPriorities();
    }

    private void beginUpkeep() {
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
    }

    private Card createThrull() {
        Card card = new Card();
        card.setName("Thrull");
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.BLACK);
        card.setPower(1);
        card.setToughness(1);
        card.setSubtypes(List.of(CardSubtype.THRULL));
        return card;
    }
}
